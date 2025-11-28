package com.proyecto.autoapp.general.maps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
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
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.*
import com.proyecto.autoapp.R
import com.proyecto.autoapp.general.funcionesComunes.IconoMapsViajero
import com.proyecto.autoapp.general.maps.viewModels.MapViewModel
import com.proyecto.autoapp.general.peticiones.PeticionesVM
import com.proyecto.autoapp.ui.theme.ThumbUpPurple


@Composable
fun MapScreen(mapViewModel: MapViewModel, esViajero: Boolean, peticionesVM: PeticionesVM) {
    val TAG = "Jose"
    val context = LocalContext.current

    val miPeticion by peticionesVM.miPeticion.collectAsState()
    val posicionViajero by peticionesVM.posicionViajero.collectAsState()
    val markers by mapViewModel.markers.collectAsState()
    val cameraPosition by mapViewModel.cameraPosition.collectAsState()
    val ruta by mapViewModel.ruta.collectAsState()

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
    val location = remember { mutableStateOf<Location?>(null) }
    val cameraPositionState = rememberCameraPositionState { position = cameraPosition }

    /**     PERMISOS UBICACIÓN     */
    //Solicitamos permisos de ubicación al iniciar.
    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    //Obtenemos la ubicación actual si esta está ya en caché.
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            // 1. Comprobar que la ubicación del sistema está encendida
            val req = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10_000
            ).build()
            val settingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(req).build()

            settingsClient.checkLocationSettings(settingsRequest)
                .addOnFailureListener { ex ->
                    if (ex is ResolvableApiException) {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(ex.resolution.intentSender).build()
                        )
                    }
                }

            // 2. Obtener lastLocation de forma segura. Por si aún no está en caché
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { loc ->
                            /**
                             *  Al recoger loc y guardarlo, desde el móvil no se rompe si no tiene memoria caché.
                             * Así solucionamos el problema del usuario que recién se registra y quiere ubicarse en el punto donde se encuentra
                             * y la app se rompe.
                             */
                            location.value = loc
                            if (loc != null) {
                                val pos = LatLng(loc.latitude, loc.longitude)

                                // 1) siempre movemos la cámara al usuario
                                mapViewModel.updateCameraPosition(pos)

                                // 2) si es viajero, además enviamos tracking a Firestore
                                if (esViajero) {
                                    peticionesVM.onLocationChangedViajero(pos)
                                }
                            } else {
                                val cts = CancellationTokenSource()
                                fusedLocationClient.getCurrentLocation(
                                    Priority.PRIORITY_HIGH_ACCURACY,
                                    cts.token
                                ).addOnSuccessListener { fresh ->
                                    location.value = fresh
                                    if (fresh != null) {
                                        val pos = LatLng(fresh.latitude, fresh.longitude)

                                        mapViewModel.updateCameraPosition(pos)

                                        if (esViajero) {
                                            peticionesVM.onLocationChangedViajero(pos)
                                        }
                                    } else {
                                        Toast.makeText(context,"Algo ha sucedido. Vuelve a pulsar",Toast.LENGTH_SHORT).show()
                                    }
                                }.addOnFailureListener { e ->
                                    Log.e(TAG, "Algo ha ocurrido dentro del else => $e")
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Algo ha ocurrido al recoger lastLocation => $e")
                        }
                } catch (_: SecurityException) {
                }
            }
        }
    }

    //Si tenemos la ubicación, mover la cámara a la ubicación del usuario.
    // Garantizamos que la cámara se centre automáticamente en la ubicación del usuario cuando se obtenga o actualice.
    LaunchedEffect(location.value) {
        location.value?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
        }
    }

    //Detecta cuando cambia el estado de la cámara por la acción del usuario y actualiza el ViewModel.
    //Cuando el usuario deja de mover la cámara (es decir, isMoving cambia de true a false), actualiza el estado de la cámara en el ViewModel llamando a updateCameraPosition
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val newPosition = cameraPositionState.position
            mapViewModel.updateCameraPosition(newPosition.target, newPosition.zoom)
        }
    }

    //Actualizamos la posición de la cámara cuando cambia el valor en el ViewModel.
    //Esto garantiza que cualquier cambio en _cameraPosition del ViewModel se propague al estado de la cámara en la UI.
    LaunchedEffect(cameraPosition) {
        cameraPositionState.position = cameraPosition
    }

    // Cuando el viajero tiene una petición aceptada y ya sabemos su ubicación,
    // enviamos una PRIMERA posición automática al conductor.
    LaunchedEffect(esViajero, miPeticion, location.value) {
        if (esViajero) {
            val pet = miPeticion
            val loc = location.value

            if (pet != null && pet.estado == "aceptada" && loc != null) {
                val pos = LatLng(loc.latitude, loc.longitude)
                peticionesVM.onLocationChangedViajero(pos)
            }
        }
    }

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
                ) //Para mantener el zoom actual.
            },
            onMapLoaded = {
                cameraPositionState.position = mapViewModel.cameraPosition.value
                if (locationPermissionGranted) {
                    location.value?.let { loc ->
                        circleCenter.value = LatLng(loc.latitude, loc.longitude)
                    }
                }
            },
            onMyLocationButtonClick = {
                Toast.makeText(context, "Ubicación actual", Toast.LENGTH_SHORT).show()

                val locActual = location.value
                if (locActual != null) {
                    val ubiActual = LatLng(locActual.latitude, locActual.longitude)

                    if (esViajero) {
                        mapViewModel.updateCameraPosition(ubiActual, 17f)
                        peticionesVM.onLocationChangedViajero(ubiActual)
                    } else {
                        mapViewModel.updateCameraPosition(ubiActual, 17f)
                    }
                    Log.e(TAG,"Ubicación a donde me manda ${ubiActual.latitude}, ${ubiActual.longitude}. Tiene valores")
                } else {
                    Log.e(TAG,"Ubicación a donde me manda es un valor nulo porque location.value es null")
                }
                true
            },
            onMyLocationClick = {
                longitude = TextFieldValue(it.longitude.toString())
                latitude = TextFieldValue(it.latitude.toString())
                Toast.makeText(context, "Estoy aquí", Toast.LENGTH_SHORT).show()
            }
        ) {
            /**     USO DE MARCADORES Y LOCALIZACIÓN     */
            //En la ubicación actual
            location.value?.let {
                //Dibuja un círculo alrededor del punto azul de la ubicación.
                Circle(
                    center = LatLng(it.latitude, it.longitude),
                    radius = 25.0,
                    strokeColor = Color.Blue,
                    strokeWidth = 3f,
                    fillColor = Color.Blue.copy(alpha = 0.1f)
                )
            }

            //Calculamos la distancia entre la ubicación del usuario y el marcador.
            location.value?.let { loc ->
                val userLocation = Location("")
                userLocation.latitude = mapViewModel.cameraPosition.value.target.latitude
                userLocation.longitude = mapViewModel.cameraPosition.value.target.longitude

                val markerLocation = Location("")
                markerLocation.latitude = mapViewModel.home.latitude
                markerLocation.longitude = mapViewModel.home.longitude

                val distanceInMeters = userLocation.distanceTo(markerLocation)
                Log.e(TAG, "Distancia: $distanceInMeters metros")
            }

            //Dibuja los marcadores almacenados en el viewmodel, usando MarkerInfoWindow.
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

            // Marcador del viajero (solo se verá en el mapa del CONDUCTOR, porque ahí se observa posicionViajero)
            posicionViajero?.let { pos ->
                Marker(
                    state = MarkerState(position = pos),
                    title = "Viajero",
                    icon = IconoMapsViajero(context, R.mipmap.inoco_maps)
                )
            }

            // Ruta hacia el viajero (si existe)
            if (ruta.isNotEmpty()) {
                Polyline(
                    points = ruta,
                    width = 12f,
                    color = ThumbUpPurple,
                    geodesic = true
                )
            }

            /**     -------------------------------      */
        }
    }
}
