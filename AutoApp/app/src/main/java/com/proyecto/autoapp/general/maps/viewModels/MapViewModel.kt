package com.proyecto.autoapp.general.maps.viewModels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.proyecto.autoapp.R
import com.proyecto.autoapp.general.maps.MapMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.collections.plus

class MapViewModel : ViewModel() {
    val TAG = "Jose"

    val home = LatLng(
        38.693245786259595,
        -4.108508457997148
    ) //CIFP Virgen de Gracia: 38.693245786259595, -4.108508457997148
    private val _markers = MutableStateFlow<List<MapMarker>>(emptyList())
    val markers: StateFlow<List<MapMarker>> = _markers

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
            _markers.value + MapMarker(position = latLng, title = title, snippet = snippet)
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

}