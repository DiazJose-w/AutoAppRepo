package com.proyecto.autoapp.general.maps.viewModels


import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.proyecto.autoapp.general.maps.MapMarker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.Manifest
import com.google.android.gms.tasks.CancellationTokenSource
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

    // Funciones para actualizar desde la UI
    fun onInicioChange(inicio: String) {
        inicioTexto = inicio

        inicioPlaceId = null
        inicioLatLng = null

        lanzarSugerencias(inicio, true)
    }

    fun onDestinoChange(destino: String) {
        destinoTexto = destino

        destinoPlaceId = null
        destinoLatLng = null

        lanzarSugerencias(destino, false)
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
            _markers.value = _markers.value + MapMarker(position = latLng, title = title, snippet = snippet)
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

    fun usarMiUbicacionComoInicio(): Boolean {
        val actual = ubicacionActual
        Log.e(TAG, "Valor de la ubicación 'mi ubicación' $actual")
        return if (actual != null) {
            inicioTexto = "Mi ubicación actual"
            inicioPlaceId = null
            inicioLatLng = actual
            sugerenciasInicio = emptyList()
            true
        } else {
            Log.e(TAG, "No hay ubicación actual disponible para usar como inicio")
            false
        }
    }

    fun prepararUbicacionInicial(context: Context, fusedLocationClient: FusedLocationProviderClient, settingsClient: SettingsClient,
        resolutionLauncher: ActivityResultLauncher<IntentSenderRequest>, esViajero: Boolean, onViajeroLocation: (LatLng) -> Unit) {
        // 1. Comprobar que la ubicación del sistema está encendida
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000).build()

        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(req)
            .build()

        settingsClient.checkLocationSettings(settingsRequest)
            .addOnFailureListener { ex ->
                if (ex is ResolvableApiException) {
                    resolutionLauncher.launch(
                        IntentSenderRequest.Builder(ex.resolution.intentSender).build()
                    )
                }
            }

        // 2. Obtener lastLocation de forma segura
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            val pos = LatLng(loc.latitude, loc.longitude)

                            // 1) movemos cámara y actualizamos ubicacionActual
                            updateCameraPosition(pos)

                            // 2) si es viajero, avisamos al callback
                            if (esViajero) {
                                onViajeroLocation(pos)
                            }
                        } else {
                            // Si lastLocation viene null, pedimos una ubicación fresca
                            val cts = CancellationTokenSource()
                            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                                .addOnSuccessListener { fresh ->
                                    if (fresh != null) {
                                        val pos = LatLng(fresh.latitude, fresh.longitude)

                                        updateCameraPosition(pos)

                                        if (esViajero) {
                                            onViajeroLocation(pos)
                                        }
                                    } else {
                                        Log.e(TAG, "No se pudo obtener ubicación actual")
                                    }
                            }.addOnFailureListener { e ->
                                Log.e(TAG, "Error al obtener ubicación actual => $e")
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error al recoger lastLocation => $e")
                    }
            } catch (_: SecurityException) {
            }
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

}