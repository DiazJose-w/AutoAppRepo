package com.proyecto.autoapp.ContenidoAppUsuario.Pasajero

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.proyecto.autoapp.general.Maps.MapScreen
import com.proyecto.autoapp.general.Maps.MapViewModel

@Composable
fun ViewUsPasajero(mapViewModel: MapViewModel){

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            MapScreen(mapViewModel)
        }
    }
}