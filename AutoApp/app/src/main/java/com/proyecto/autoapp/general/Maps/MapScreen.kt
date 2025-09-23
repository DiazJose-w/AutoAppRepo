package com.proyecto.autoapp.general.Maps

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import androidx.activity.result.IntentSenderRequest


@Composable
fun MapScreen(mapViewModel: MapViewModel){
    val context = LocalContext.current

    val markers by mapViewModel.markers.collectAsState()
    val cameraPosition by mapViewModel.cameraPosition.collectAsState()

    val selectedCoordinates by mapViewModel.selectedCoordinates.collectAsState()
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val settingsClient = LocationServices.getSettingsClient(context)
    val resolutionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {  }

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
    val cameraPositionState = rememberCameraPositionState{position = cameraPosition}

    //Solicitamos permisos de ubicación al iniciar.
    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    //Obtenemos la ubicación actual si está permitida.
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            // 1. Comprobar que la ubicación del sistema está encendida
            val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000).build()
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

            // 2. Obtener lastLocation de forma segura
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                        location.value = loc
                    }
                } catch (_: SecurityException) { }
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


    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxWidth()
                .height(300.dp)
            //.width(200.dp)
//                .weight(1f)
            ,
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = MapType.HYBRID, isMyLocationEnabled = locationPermissionGranted),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true),
            onMapLongClick = { latLng ->
                mapViewModel.addMarker(latLng)
                longitude = TextFieldValue(latLng.longitude.toString())
                latitude = TextFieldValue(latLng.latitude.toString())
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
                mapViewModel.updateCameraPosition(latLng, cameraPositionState.position.zoom, cameraPositionState.position.tilt, cameraPositionState.position.bearing) //Para mantener el zoom actual.
            },
            onMapLoaded = {
                cameraPositionState.position = mapViewModel.cameraPosition.value
                if (locationPermissionGranted) {
                    if (locationPermissionGranted) {
                        // Si el permiso es concedido, activa el punto azul
                        location.value?.let { loc ->
                            //Aquí no es necesario hacer nada extra para mostrar el punto azul,
                            //solo asegurarte de que la ubicación está permitida.
                            //Esto es para dibujar un círculo en la posición actual del usuario.
                            circleCenter.value = LatLng(loc.latitude, loc.longitude)
                        }
                    }
                }
            },
            //Estp se lanza cuando pulsamos en la diana de arriba.
            onMyLocationButtonClick = {
                Toast.makeText(context, "Volviendo a casa", Toast.LENGTH_SHORT).show()
                mapViewModel.irAHome()
                Log.d(TAG, "Cámara actualizada: $cameraPosition")
                true
            },
            //Se lanza cuando pulsamos en el punto azul de mi localización en tiempo real.
            onMyLocationClick = {
                longitude = TextFieldValue(it.longitude.toString())
                latitude = TextFieldValue(it.latitude.toString())
                Toast.makeText(context, "Estoy aquí", Toast.LENGTH_SHORT).show()
            }
        ) {

            //En la ubicación actual
            location.value?.let {

//                Polyline(
//                    points = listOf(viewModel.cameraPosition.value.target, viewModel.home),
//                    color = Color.Blue,
//                    width = 9f
//                )

                //Dibuja un círculo alrededor del punto azul de la ubicación.
                Circle(
                    center = LatLng(it.latitude, it.longitude),
                    radius = 50.0, //Ajusta el tamaño del radio del círculo (en metros). el círculo rodea lo posición actual.
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

            //Usando Marker.
//            markers.forEach { marker ->
//                Marker(
//                    state = MarkerState(position = marker.position),
//                    title = marker.title,
//                    snippet = marker.snippet,
//                    onClick = {
//                        viewModel.removeMarker(marker)
//                        Toast.makeText(context, "Marcador eliminado", Toast.LENGTH_SHORT).show()
//                        true //Retorna true para consumir el evento y evitar abrir la info window.
//                    }
//                )
//            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitude") },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitude") },
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
        }
    }
}