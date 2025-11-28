package com.proyecto.autoapp.general.maps


import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.firebase.firestore.FieldValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import com.proyecto.autoapp.R
import com.proyecto.autoapp.general.Coleccion
import com.proyecto.autoapp.general.modelo.peticiones.Peticion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.collections.minus
import kotlin.collections.plus

class MapViewModel : ViewModel() {
    val TAG = "Jose"
    val auth = FirebaseAuth.getInstance()
    var peticion = Coleccion.PeticionViaje
    val home = LatLng(38.693245786259595, -4.108508457997148) //CIFP Virgen de Gracia: 38.693245786259595, -4.108508457997148
    private val _markers = MutableStateFlow<List<MapMarker>>(emptyList())
    val markers: StateFlow<List<MapMarker>> = _markers

    val isLoadingPeticion = mutableStateOf(false)
    val errorPeticion = mutableStateOf<String?>(null)

    // Variables y funciones para GooglePlace
    private lateinit var placesClient: PlacesClient

    fun setPlacesClient(client: PlacesClient) {
        this.placesClient = client
    }

    // Texto de los campos de la vista inicial
    var inicioTexto by mutableStateOf("")
        private set

    var destinoTexto by mutableStateOf("")
        private set

    // Lista de sugerencias para cada campo
    var sugerenciasInicio by mutableStateOf<List<AutocompletePrediction>>(emptyList())
        private set

    var sugerenciasDestino by mutableStateOf<List<AutocompletePrediction>>(emptyList())
        private set

    var ubicacionActual: LatLng? = null
        private set

    // Id de la localización añadida para almacenarla en Firestore. En su propia colección
    var inicioPlaceId: String? = null
        private set

    var destinoPlaceId: String? = null
        private set

    // Coordenadas reales del lugar elegido
    var inicioLatLng by mutableStateOf<LatLng?>(null)
        private set

    var destinoLatLng by mutableStateOf<LatLng?>(null)
        private set

    // Para escuchar las peticiones lanzadas
    var peticionesPendientes by mutableStateOf<List<Peticion>>(emptyList())
        private set

    private var listenerPeticiones: ListenerRegistration? = null
    private var listenerMiPeticion: ListenerRegistration? = null
    private var listenerTracking: ListenerRegistration? = null


    // Tiempo máximo petición
    private val TIEMPO_MAX_PETICION_MS = 30_000L

    private val _miPeticion = MutableStateFlow<Peticion?>(null)
    val miPeticion: StateFlow<Peticion?> = _miPeticion

    // Posición del viajero (vista por el conductor)
    private val _posicionViajero = MutableStateFlow<LatLng?>(null)
    val posicionViajero: StateFlow<LatLng?> = _posicionViajero

    private val _ruta = MutableStateFlow<List<LatLng>>(emptyList())
    val ruta: StateFlow<List<LatLng>> = _ruta

    fun limpiarRuta() {
        _ruta.value = emptyList()
    }

    private fun decodificacionRuta(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            val latLng = LatLng(
                lat / 1E5,
                lng / 1E5
            )
            poly.add(latLng)
        }
        return poly
    }

    /**
     * Traza una ruta desde la posición actual de la cámara hasta el destino.
     * De momento usamos cameraPosition.target como "origen" del conductor.
     */
    fun trazarRutaHasta(destino: LatLng, context: Context) {
        val origen = cameraPosition.value.target
        obtenerRuta(origen, destino, context)
    }

    // Funciones para actualizar desde la UI
    fun onInicioChange(nuevo: String) {
        inicioTexto = nuevo
        lanzarSugerencias(query = nuevo, esInicio = true)
    }

    fun onDestinoChange(nuevo: String) {
        destinoTexto = nuevo
        lanzarSugerencias(query = nuevo, esInicio = false)
    }

    private val _cameraPosition = MutableStateFlow(
        CameraPosition.Builder()
            .target(home) //Coordenadas de la posición inicial
            .zoom(17f)    //Nivel de zoom inicial
            .tilt(45f)    //Inclinación inicial (en grados)
            .bearing(90f) //Orientación inicial (en grados, 0=norte, 90=este, etc.)
            .build()
    )
    val cameraPosition: StateFlow<CameraPosition> = _cameraPosition

    private val _selectedCoordinates = MutableStateFlow<LatLng?>(null)
    val selectedCoordinates: StateFlow<LatLng?> = _selectedCoordinates

    /**
     * Funciones para el punto de interés
     * */
    fun addMarker(latLng: LatLng, title: String = "Título del marcador", snippet: String = "Contenido del marcador") {
        viewModelScope.launch {
            _markers.value += MapMarker(position = latLng, title = title, snippet = snippet)
        }
    }

    fun removeMarker(marker: MapMarker) {
        _markers.value -= marker
    }

    /**
     * Va a la posición que el usuario indique desde la app como Home.
     */
    fun irAHome() {
        val currentPosition = _cameraPosition.value //Obtenemos el zoom actual.
        _cameraPosition.value = CameraPosition.fromLatLngZoom(home, currentPosition.zoom) //Lo mantenemos en la ubicaciçon.
//        _cameraPosition.value = CameraPosition.Builder()
//            .target(home)
//            .zoom(currentPosition.zoom)
//            .tilt(currentPosition.tilt)
//            .bearing(currentPosition.bearing)
//            .build()
    }

    /**
     * Mantiene en zoom a 15 de distancia la posición de la cámara.
     */
    fun updateCameraPosition(latLng: LatLng, zoom: Float = 15f) {
        viewModelScope.launch {
            ubicacionActual = latLng
            _cameraPosition.value = CameraPosition.fromLatLngZoom(latLng, zoom)
        }
    }

    /**
     * Actualiza la posición de la cámara a la ubicación del usuario.
     */
    fun updateCameraPosition(latLng: LatLng, zoom: Float? = null, tilt: Float? = null, bearing: Float? = null) {
        viewModelScope.launch {
            val currentPosition = _cameraPosition.value
            ubicacionActual = latLng
            _cameraPosition.value = CameraPosition.fromLatLngZoom(latLng, zoom ?: currentPosition.zoom)
        }
    }

    // Select coordinates
    fun selectCoordinates(latLng: LatLng) {
        viewModelScope.launch {
            _selectedCoordinates.value = latLng
        }
    }

    /**
     * Métodos para lanzar las sugerencias de google Place
     * */
    private fun lanzarSugerencias(query: String, esInicio: Boolean) {

        val clienteListo = ::placesClient.isInitialized
        val textoValido = query.length >= 2
        val ubicacion = ubicacionActual

        // 1. Si algo no es válido → limpiamos sugerencias
        if (!clienteListo || !textoValido) {
            if (esInicio) {
                sugerenciasInicio = emptyList()
            } else {
                sugerenciasDestino = emptyList()
            }
        }

        // 2. Solo seguimos si el cliente está listo y el texto tiene longitud suficiente
        if (clienteListo && textoValido) {

            val token = AutocompleteSessionToken.newInstance()

            // --- Construimos el builder de la petición ---
            val builder = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(query)
                /**
                 * Esto limita a España. Puede modificarse y añadir más países
                 * .setCountries(listOf("ES", "PT", "FR"))
                 * */
                .setCountries(listOf("ES"))

            // Si conocemos la ubicación, aplicamos un "bias" alrededor (radio ~ 20 km aprox)
            if (ubicacion != null) {
                val lat = ubicacion.latitude
                val lng = ubicacion.longitude
                val delta = 0.2

                val bounds = RectangularBounds.newInstance(
                    LatLng(lat - delta, lng - delta),
                    LatLng(lat + delta, lng + delta)
                )

                builder.setLocationBias(bounds)
            }

            val request = builder.build()

            // 3. Llamada a Places
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    val lista = response.autocompletePredictions

                    if (esInicio) {
                        sugerenciasInicio = lista
                    } else {
                        sugerenciasDestino = lista
                    }
                }
                .addOnFailureListener {
                    if (esInicio) {
                        sugerenciasInicio = emptyList()
                    } else {
                        sugerenciasDestino = emptyList()
                    }
                }
        }
    }

    private fun cargarLatLngDePlace(placeId: String, esInicio: Boolean) {

        val clienteListo = ::placesClient.isInitialized
        val fields: List<Place.Field> = listOf(Place.Field.LAT_LNG)

        // Si el cliente NO está listo, dejamos las coords a null
        if (!clienteListo) {
            if (esInicio) {
                inicioLatLng = null
            } else {
                destinoLatLng = null
            }
        }

        // Solo hacemos la petición si el cliente está listo
        if (clienteListo) {

            val request = FetchPlaceRequest
                .builder(placeId, fields)
                .build()

            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val latLng = response.place.latLng

                    if (esInicio) {
                        inicioLatLng = latLng
                    } else {
                        destinoLatLng = latLng
                    }
                }
                .addOnFailureListener {
                    if (esInicio) {
                        inicioLatLng = null
                    } else {
                        destinoLatLng = null
                    }
                }
        }
    }

    fun seleccionarSugerenciaInicio(prediction: AutocompletePrediction) {
        inicioTexto = prediction.getPrimaryText(null).toString()
        sugerenciasInicio = emptyList()

        inicioPlaceId = prediction.placeId
        cargarLatLngDePlace(prediction.placeId, esInicio = true)
    }

    fun seleccionarSugerenciaDestino(prediction: AutocompletePrediction) {
        destinoTexto = prediction.getPrimaryText(null).toString()
        sugerenciasDestino = emptyList()

        destinoPlaceId = prediction.placeId
        cargarLatLngDePlace(prediction.placeId, esInicio = false)
    }

    fun hayInicioYDestinoValidos(): Boolean {
        return inicioLatLng != null && destinoLatLng != null
    }

    fun centrarEnInicioSeleccionado(zoom: Float = 15f) {
        val latLng = inicioLatLng
        if (latLng != null) {
            updateCameraPosition(latLng, zoom)
            addMarker(latLng, title = "Punto de inicio")
        }
    }

    fun centrarEnDestinoSeleccionado(zoom: Float = 15f) {
        val latLng = destinoLatLng
        if (latLng != null) {
            updateCameraPosition(latLng, zoom)
            addMarker(latLng, title = "Destino")
        }
    }

    /**
     * Funciones para gestionar las peticiones
     * */
    fun enviarPeticion(uidUsuario: String, onResult: (Boolean) -> Unit) {
        isLoadingPeticion.value = true
        errorPeticion.value = null

        val db = FirebaseFirestore.getInstance()
        val idPeticion = db.collection(peticion).document().id

        // Map para el punto de inicio
        val inicioMap = mapOf(
            "texto" to inicioTexto,
            "lat" to inicioLatLng?.latitude,
            "lng" to inicioLatLng?.longitude,
            "placeId" to inicioPlaceId
        )

        // Map para el punto de destino
        val destinoMap = mapOf(
            "texto" to destinoTexto,
            "lat" to destinoLatLng?.latitude,
            "lng" to destinoLatLng?.longitude,
            "placeId" to destinoPlaceId
        )

        val datosPeticion: Map<String, Any?> = mapOf(
            "id" to idPeticion,
            "uidUsuario" to uidUsuario,

            // conductores que han rechazado
            "uidConductorCan" to emptyList<String>(),

            // bloques inicio/destino
            "inicio" to inicioMap,
            "destino" to destinoMap,

            "estado" to "pendiente",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection(peticion)
            .document(idPeticion)
            .set(datosPeticion)
            .addOnSuccessListener {
                Log.e(TAG, "Petición enviada correctamente: $datosPeticion")
                isLoadingPeticion.value = false
                programarCancelacionPorTimeout(idPeticion)
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "ERROR al enviar petición", e)
                errorPeticion.value = e.message ?: "Error desconocido al guardar petición"
                isLoadingPeticion.value = false
                onResult(false)
            }
    }

    fun rechazarPeticionConductor(pet: Peticion, uidConductor: String, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(peticion).document(pet.id)

        /**
         * Una vez el conductor rechaza se almacena en la lista de conductores que rechazaron llevar al viajero.
         * Automáticamente deja de aparecer el viaje en su vista
         * */
        docRef.update(
                mapOf(
                    "estado" to "pendiente",
                    "uidConductorCan" to FieldValue.arrayUnion(uidConductor)
                )
            )
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun aceptarPeticionConductor(pet: Peticion, uidConductor: String, nombreDelConductor: String, fotoConductor: String, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(peticion).document(pet.id)

        Log.e(TAG, "Foto de perfil == $fotoConductor")

        db.runTransaction { tx ->
            val snap = tx.get(docRef)
            val estadoActual = snap.getString("estado")
            val infoConductorActual = snap.get("infoConductor") as? Map<*, *>

            // Solo actualizamos si sigue pendiente y nadie la ha aceptado aún
            if (estadoActual == "pendiente" && infoConductorActual == null) {
                val infoConductorMap = mapOf(
                    "uid" to uidConductor,
                    "nombre" to nombreDelConductor,
                    "foto" to fotoConductor
                )

                tx.update(
                    docRef,
                    mapOf(
                        "estado" to "ofertaConductor",
                        "infoConductor" to infoConductorMap,
                        "timestampAceptacion" to System.currentTimeMillis()
                    )
                )
            }
        }
            .addOnSuccessListener {
                programarTimeoutRespuestaViajero(pet.id)
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error aceptando petición", e)
                onResult(false)
            }
    }

    fun aceptarOfertaViajero(pet: Peticion, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection(peticion)
            .document(pet.id)
            .update(
                mapOf(
                    "estado" to "aceptada",
                    "timestampConfirmacionViajero" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun rechazarOfertaViajero(pet: Peticion, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(peticion).document(pet.id)

        docRef.get()
            .addOnSuccessListener { snap ->
                val estadoActual = snap.getString("estado")
                val infoConductor = snap.get("infoConductor") as? Map<*, *>
                val uidConductor = infoConductor?.get("uid") as? String ?: ""

                if (estadoActual == "ofertaConductor" && uidConductor.isNotEmpty()) {
                    val trackingMap = mapOf(
                        "compartiendo" to false,
                        "lat" to null,
                        "lng" to null,
                        "ultimaActualizacion" to System.currentTimeMillis()
                    )

                    db.runTransaction { tx ->
                        tx.update(
                            docRef,
                            mapOf(
                                "estado" to "pendiente",
                                "infoConductor" to null,
                                "uidConductorCan" to FieldValue.arrayUnion(uidConductor),
                                "trackingViajero" to trackingMap
                            )
                        )
                    }
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                } else {
                    onResult(false)
                }
            }
            .addOnFailureListener { onResult(false) }
    }

    fun cancelarViajeViajero(pet: Peticion, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(peticion).document(pet.id)

        // Al cancelar, dejamos de compartir ubicación
        val trackingMap = mapOf(
            "compartiendo" to false,
            "lat" to null,
            "lng" to null,
            "ultimaActualizacion" to System.currentTimeMillis()
        )

        docRef.update(
            mapOf(
                "estado" to "cancelada",
                "trackingViajero" to trackingMap
            )
        )
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al cancelar viaje", e)
                onResult(false)
            }
    }

    /**
     *  Funciones para escuchar las peticiones por parte del conductor
     */
    fun observarMiPeticion(uidUsuario: String) {
        val db = FirebaseFirestore.getInstance()
        listenerMiPeticion?.remove()

        listenerMiPeticion = db.collection(peticion)
            .whereEqualTo("uidUsuario", uidUsuario)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    Log.e(TAG, "Error escuchando mi petición", e)
                    return@addSnapshotListener
                }

                if (snap != null && !snap.isEmpty) {
                    // Por si hubiera más de una, cogemos la más reciente
                    val peticiones = snap.documents.mapNotNull { it.toObject(Peticion::class.java) }
                    val peticionMasReciente = peticiones.maxByOrNull { it.timestamp }
                    _miPeticion.value = peticionMasReciente
                } else {
                    _miPeticion.value = null
                }
            }
    }

    fun observarPeticionesPendientesAceptadas(uidConductor: String) {
        val db = FirebaseFirestore.getInstance()
        listenerPeticiones?.remove()

        listenerPeticiones = db.collection(peticion)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error escuchando peticiones", e)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val visibles = snapshot.documents
                    .mapNotNull { it.toObject(Peticion::class.java) }
                    .onEach { pet ->
                        Log.d(TAG, "id=${pet.id}, can=${pet.uidConductorCan}")
                    }
                    .filter { pet ->
                        val esMia = pet.uidUsuario == uidConductor
                        val yoLaHeRechazado = pet.uidConductorCan.contains(uidConductor)
                        val esPendiente = pet.estado == "pendiente"
                        val esAceptadaPorViajero = pet.estado == "aceptada"
                        val esAceptadaParaMi = pet.estado == "aceptada" && pet.infoConductor?.uid == uidConductor
                        val esOfertaDelConductor = pet.estado == "ofertaConductor" && pet.infoConductor?.uid == uidConductor

                        Log.d(TAG,"id=${pet.id} - uidConductor=$uidConductor, can=${pet.uidConductorCan}, yoLaHeRechazado=$yoLaHeRechazado, estado=${pet.estado}, infoConductor=${pet.infoConductor}")

                        // Nueva manera de crear condiciones al filter.
                        !esMia && !yoLaHeRechazado && (esPendiente || esOfertaDelConductor || esAceptadaParaMi)
                    }

                peticionesPendientes = visibles
            }
    }

    /**
     * Métodos para la ubicación en tiempo real
     * */
    fun actualizarUbicacionViajero(pet: Peticion, posicion: LatLng) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(peticion).document(pet.id)

        val trackingMap = mapOf(
            "compartiendo" to true,
            "lat" to posicion.latitude,
            "lng" to posicion.longitude,
            "ultimaActualizacion" to System.currentTimeMillis()
        )

        docRef.update(
            mapOf(
                "trackingViajero" to trackingMap
            )
        ).addOnFailureListener { e ->
            Log.e(TAG, "Error actualizando tracking del viajero", e)
        }
    }

    fun observarTrackingPeticion(idPeticion: String) {
        val db = FirebaseFirestore.getInstance()
        listenerTracking?.remove()

        listenerTracking = db.collection(peticion)
            .document(idPeticion)
            .addSnapshotListener { snap, e ->
                if (e != null || snap == null || !snap.exists()) {
                    Log.e(TAG, "Error escuchando tracking de la petición", e)
                    _posicionViajero.value = null
                    return@addSnapshotListener
                }

                val pet = snap.toObject(Peticion::class.java)
                val tracking = pet?.trackingViajero

                if (tracking?.compartiendo == true &&
                    tracking.lat != null &&
                    tracking.lng != null
                ) {
                    _posicionViajero.value = LatLng(tracking.lat, tracking.lng)
                } else {
                    // No está compartiendo o no hay coords válidas
                    _posicionViajero.value = null
                }
            }
    }

    fun onLocationChangedViajero(latLng: LatLng) {
        // Actualizamos la cámara como ya hacías
        updateCameraPosition(latLng)

        // Si tengo una petición mía y está aceptada, envío mi posición
        val pet = _miPeticion.value
        if (pet != null && pet.estado == "aceptada") {
            actualizarUbicacionViajero(pet, latLng)
        }
    }

    private fun obtenerRuta(origen: LatLng, destino: LatLng, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val apiKey = context.getString(R.string.google_maps_key)

                val url = "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=${origen.latitude},${origen.longitude}" +
                        "&destination=${destino.latitude},${destino.longitude}" +
                        "&mode=driving&key=$apiKey"

                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@launch

                Log.d(TAG, "Directions body: $body")

                val json = JSONObject(body)
                val routes = json.getJSONArray("routes")
                if (routes.length() == 0) {
                    Log.w(TAG, "Directions: routes vacío")
                    return@launch
                }

                val route = routes.getJSONObject(0)
                val overviewPolyline = route
                    .getJSONObject("overview_polyline")
                    .getString("points")

                val puntosRuta = decodificacionRuta(overviewPolyline)

                withContext(Dispatchers.Main) {
                    _ruta.value = puntosRuta
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo ruta", e)
            }
        }
    }

    fun finalizarViajeViajero(pet: Peticion, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(peticion).document(pet.id)

        // Marcamos que el viajero ya no comparte ubicación
        val trackingMap = mapOf(
            "compartiendo" to false,
            "lat" to null,
            "lng" to null,
            "ultimaActualizacion" to System.currentTimeMillis()
        )

        docRef.update(
            mapOf(
                "estado" to "finalizada",           // <- nuevo estado de viaje terminado
                "trackingViajero" to trackingMap
            )
        )
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al finalizar viaje", e)
                onResult(false)
            }
    }

    /**
     * Métodos que gestionan el tiempo de cancelación por inactividad.
     * */
    private fun programarCancelacionPorTimeout(idPeticion: String) {
        val db = FirebaseFirestore.getInstance()

        viewModelScope.launch {
            delay(TIEMPO_MAX_PETICION_MS)

            db.collection(peticion)
                .document(idPeticion)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val estadoActual = doc.getString("estado")
                        if (estadoActual == "pendiente") {
                            db.collection(peticion)
                                .document(idPeticion)
                                .update("estado", "cancelada")
                        }
                    }
                }
        }
    }

    private fun programarTimeoutRespuestaViajero(idPeticion: String) {
        val db = FirebaseFirestore.getInstance()

        viewModelScope.launch {
            delay(TIEMPO_MAX_PETICION_MS)
            db.collection(peticion)
                .document(idPeticion)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val estadoActual = doc.getString("estado")
                        if (estadoActual == "pendiente" || estadoActual == "ofertaConductor") {
                            db.collection(peticion)
                                .document(idPeticion)
                                .update("estado", "cancelada")
                        }
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerPeticiones?.remove()
        listenerMiPeticion?.remove()
        listenerTracking?.remove()
    }

}