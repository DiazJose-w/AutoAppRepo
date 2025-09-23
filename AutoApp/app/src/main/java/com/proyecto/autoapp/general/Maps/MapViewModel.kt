package com.proyecto.autoapp.general.Maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.minus
import kotlin.collections.plus

class MapViewModel : ViewModel() {
    val home = LatLng(38.693245786259595, -4.108508457997148) //CIFP Virgen de Gracia: 38.693245786259595, -4.108508457997148
    private val _markers = MutableStateFlow<List<MapMarker>>(emptyList())
    val markers: StateFlow<List<MapMarker>> = _markers

//    private val _cameraPosition = MutableStateFlow(LatLng(38.69332, -4.10860)) // CIFP Virgen de Gracia
//    val cameraPosition: StateFlow<LatLng> = _cameraPosition

    //    private val _cameraPosition = MutableStateFlow(CameraPosition.fromLatLngZoom(home, 17f)) // Posición inicial con zoom 17.
//    val cameraPosition: StateFlow<CameraPosition> = _cameraPosition
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

    // Add a marker
    fun addMarker(latLng: LatLng, title: String = "Título del marcador", snippet: String = "Contenido del marcador") {
        viewModelScope.launch {
            _markers.value += MapMarker(position = latLng, title = title, snippet = snippet)
        }
    }

    // Remove a marker
    fun removeMarker(marker: MapMarker) {
//        viewModelScope.launch { //Esto no lo pongo con corrutinas porque no sigo hasta que no se borre. Hace un efecto no deseado en caso contrario.
        _markers.value -= marker
//        }
    }

    /**
     * Va a home pero siempre a la misma distancia de zoomm: 17f.
     */
//    fun irAHome() {
//        _cameraPosition.value = CameraPosition.fromLatLngZoom(home, 17f)
//    }

    /**
     * Va a home pero mantiene la posición de la cámara sin cambiar el zoom / tilt? / bearing? actuales.
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
//    fun updateCameraPosition(latLng: LatLng, zoom: Float = 15f) {
//        viewModelScope.launch {
//            _cameraPosition.value = CameraPosition.fromLatLngZoom(latLng, zoom)
//        }
//    }

    /**
     * Actualiza la posición de la cámara sin cambiar el zoom / tilt? / bearing?.
     */
    fun updateCameraPosition(latLng: LatLng, zoom: Float? = null, tilt: Float? = null, bearing: Float? = null) {
        viewModelScope.launch {
            val currentPosition = _cameraPosition.value
            _cameraPosition.value = CameraPosition.fromLatLngZoom(latLng, zoom ?: currentPosition.zoom) //Mantenemos el zoom actual.
//            _cameraPosition.value = CameraPosition.Builder()
//                .target(latLng)
//                .zoom(zoom ?: currentPosition.zoom)
//                .tilt(tilt ?: currentPosition.tilt)
//                .bearing(bearing ?: currentPosition.bearing)
//                .build()
        }
    }


    // Select coordinates
    fun selectCoordinates(latLng: LatLng) {
        viewModelScope.launch {
            _selectedCoordinates.value = latLng
        }
    }
}