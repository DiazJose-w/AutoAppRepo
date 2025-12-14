package com.proyecto.autoapp.general.maps

import android.Manifest
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.*
import com.google.maps.android.compose.*
import com.proyecto.autoapp.R
import com.proyecto.autoapp.general.funcionesComunes.IconoMapsViajero
import com.proyecto.autoapp.general.maps.viewModels.MapViewModel
import com.proyecto.autoapp.viewUsuario.peticiones.PeticionesVM


@Composable
fun MapScreen(mapViewModel: MapViewModel, esViajero: Boolean, peticionesVM: PeticionesVM) {
    val TAG = "Jose"
    val context = LocalContext.current

    val posicionViajero by peticionesVM.posicionViajero.collectAsState()
    val markers by mapViewModel.markers.collectAsState()
    val cameraPosition by mapViewModel.cameraPosition.collectAsState()

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val settingsClient = LocationServices.getSettingsClient(context)

    val resolutionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { }

    var longitude by remember { mutableStateOf(TextFieldValue("")) }
    var latitude by remember { mutableStateOf(TextFieldValue("")) }

    Log.d(TAG, "Posición de cámara: $cameraPosition")

    var locationPermissionGranted by remember { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            locationPermissionGranted = granted
        }
    )

    val circleCenter = remember { mutableStateOf(mapViewModel.home) }
    val cameraPositionState = rememberCameraPositionState { position = cameraPosition }

    /**     PERMISOS UBICACIÓN     */
    // Solicitar permisos al iniciar
    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Obtener ubicación inicial y centrar cámara
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            mapViewModel.prepararUbicacionInicial(
                context = context,
                fusedLocationClient = fusedLocationClient,
                settingsClient = settingsClient,
                resolutionLauncher = resolutionLauncher,
                esViajero = esViajero
            ) { pos ->
                // Solo aquí sabes qué hacer con la ubicación del viajero
                if (esViajero) {
                    peticionesVM.onLocationChangedViajero(pos)
                }
            }
        }
    }

    // Cuando deja de moverse la cámara (gesto del usuario), actualizamos en el VM
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val newPosition = cameraPositionState.position
            mapViewModel.updateCameraPosition(newPosition.target, newPosition.zoom)
        }
    }

    // Cuando cambia _cameraPosition en el VM, lo reflejamos en el mapa
    LaunchedEffect(cameraPosition) {
        cameraPositionState.position = cameraPosition
    }

    // (El LaunchedEffect con location.value lo quitamos, ya no existe 'location')

    /** -------------------------- */

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.HYBRID,
                isMyLocationEnabled = locationPermissionGranted
            ),
            uiSettings = MapUiSettings(zoomControlsEnabled = false),
            onMapLongClick = { latLng ->
                mapViewModel.addMarker(latLng)
                // Centra la cámara en el marcador.
                mapViewModel.updateCameraPosition(latLng)
            },
            onPOIClick = { poi ->
                Toast.makeText(context, "POI: ${poi.name}", Toast.LENGTH_SHORT).show()
                mapViewModel.selectCoordinates(poi.latLng)
                longitude = TextFieldValue(poi.latLng.longitude.toString())
                latitude = TextFieldValue(poi.latLng.latitude.toString())
                mapViewModel.updateCameraPosition(poi.latLng)
            },
            onMapClick = { latLng ->
                mapViewModel.selectCoordinates(latLng)
                longitude = TextFieldValue(latLng.longitude.toString())
                latitude = TextFieldValue(latLng.latitude.toString())
                mapViewModel.updateCameraPosition(
                    latLng,
                    cameraPositionState.position.zoom,
                    cameraPositionState.position.tilt,
                    cameraPositionState.position.bearing
                ) // Para mantener el zoom actual.
            },
            onMapLoaded = {
                cameraPositionState.position = mapViewModel.cameraPosition.value
                if (locationPermissionGranted) {
                    mapViewModel.ubicacionActual?.let { pos ->
                        circleCenter.value = pos
                    }
                }
            },
            onMyLocationButtonClick = {
                val ubiActual = mapViewModel.ubicacionActual
                if (ubiActual != null) {
                    if (esViajero) {
                        mapViewModel.updateCameraPosition(ubiActual, 17f)
                        peticionesVM.onLocationChangedViajero(ubiActual)
                    } else {
                        mapViewModel.updateCameraPosition(ubiActual, 17f)
                    }
                    Log.e(TAG, "Ubicación a donde me manda ${ubiActual.latitude}, ${ubiActual.longitude}. Tiene valores")
                } else {
                    Log.e(TAG, "Ubicación a donde me manda es null porque no hay ubicacionActual")
                }
                Toast.makeText(context, "Ubicación actual", Toast.LENGTH_SHORT).show()
                true
            },
            onMyLocationClick = {
                longitude = TextFieldValue(it.longitude.toString())
                latitude = TextFieldValue(it.latitude.toString())
                Toast.makeText(context, "Estoy aquí", Toast.LENGTH_SHORT).show()
            }
        ) {
            /**     USO DE MARCADORES Y LOCALIZACIÓN     */

            // Círculo alrededor de la ubicación actual (si la tenemos)
            mapViewModel.ubicacionActual?.let { pos ->
                Circle(
                    center = pos,
                    radius = 25.0,
                    strokeColor = Color.Blue,
                    strokeWidth = 3f,
                    fillColor = Color.Blue.copy(alpha = 0.1f)
                )
            }

            // Distancia entre la cámara y el "home" (no depende ya de 'location')
            run {
                val userLocation = Location("")
                userLocation.latitude = mapViewModel.cameraPosition.value.target.latitude
                userLocation.longitude = mapViewModel.cameraPosition.value.target.longitude

                val markerLocation = Location("")
                markerLocation.latitude = mapViewModel.home.latitude
                markerLocation.longitude = mapViewModel.home.longitude

                val distanceInMeters = userLocation.distanceTo(markerLocation)
                Log.e(TAG, "Distancia: $distanceInMeters metros")
            }

            // Dibuja los marcadores almacenados en el ViewModel
            markers.forEach { marker ->
                MarkerInfoWindow(
                    state = MarkerState(position = marker.position),
                    title = marker.title,
                    snippet = marker.snippet,
                    onInfoWindowClick = {
                        mapViewModel.removeMarker(marker)
                        Toast.makeText(context, "Marcador eliminado", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Log.e(TAG, "posicionViajero = $posicionViajero")
            // Marcador del viajero (solo se verá en el mapa del CONDUCTOR)
            posicionViajero?.let { pos ->
                Marker(
                    state = MarkerState(position = pos),
                    title = "Viajero",
                    icon = IconoMapsViajero(context, R.mipmap.inoco_maps)
                )
            }
            /**     -------------------------------      */
        }
    }
}
