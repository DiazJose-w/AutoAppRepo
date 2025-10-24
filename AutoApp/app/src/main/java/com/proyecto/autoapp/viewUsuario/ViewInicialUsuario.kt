package com.proyecto.autoapp.viewUsuario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.proyecto.autoapp.general.Maps.MapScreen
import com.proyecto.autoapp.general.Maps.MapViewModel
import com.proyecto.autoapp.general.modelo.dataClass.ViajeUi

@Composable
fun ViewUsuario(mapViewModel: MapViewModel) {
    var inicio by remember { mutableStateOf("")  }
    var destino by remember { mutableStateOf("")  }
    var estadoSolicitud by remember { mutableStateOf<EstadoSolicitud>(EstadoSolicitud.Pendiente) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopStart
            ) {
                PerfilMenu(
                    onPerfil = {
                        /**
                         *  Crear ViewPerfil. Un perfil donde podrá modificar sus datos.
                         * podrá realizar verificaciones de perfil y podrá ver el porcentaje de fiabilidad
                         * de su perfíl y las valoraciones recibidas por otros usuarios.
                         * */
                    },
                    onHistorial = {
                        /**
                         * Crear ViewHistorial. Una lista con su historial de vaijes y su información
                         * */
                    },
                    onFavoritos = {
                        /**
                         * Crear ViewFavoritos. Una lista con sus perfiles favoritos
                         * */
                    },
                    onConfiguracion = {
                        /**
                         *  Crear ViewConfiguración. Donde podrá elegir el idioma, el tema de la app
                         * y más opciones (De momento pensar en cuales).
                         * */
                    },
                    onLogout = {
                        /**
                         * Cerrar sesión. Mirar del proyecto de paquetería.
                         * */
                    }
                )
            }

            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(Modifier.fillMaxSize()) {
                    MapScreen(mapViewModel)
                }
            }

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inicio,
                        onValueChange = { inicio = it },
                        label = { Text("Punto de inicio") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = destino,
                        onValueChange = { destino = it },
                        label = { Text("Destino") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    /**
                     * Lanzar petición. Crear ViewModelSolicitudViajes
                     * */
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Realizar petición")
            }

            Spacer(Modifier.height(12.dp))

            when (estadoSolicitud) {
                is EstadoSolicitud.Pendiente -> {
                    /**
                     * Esto solo se realizará desde la vista del conductor.
                     * */
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Esperando confirmación",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                is EstadoSolicitud.Rechazada -> {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Petición rechazada: ${(estadoSolicitud as EstadoSolicitud.Rechazada).motivo}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is EstadoSolicitud.Confirmada -> {
                    PanelInfoViaje(
                        viaje = (estadoSolicitud as EstadoSolicitud.Confirmada).viaje,
                        onVerRuta = {
                            /**
                             * Funcionalidad para el futuro.
                             * */
                        },
                        onContactar = {
                            /**
                             * Esta funcionalidad abre el chat del conductor.
                             * */
                        },
                        onCancelar = {
                            estadoSolicitud as EstadoSolicitud.Rechazada
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    /**
                     * Cambiar a vista de conductor
                     * */
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Modo conductor")
            }
        }
    }
}

/*
 * Así evitamos las clases enumeradas. Es otra manera de poder crear subtipos. Solo funcionará en esta clase
 */
sealed interface EstadoSolicitud {
    data object Pendiente : EstadoSolicitud
    data class Confirmada(val viaje: ViajeUi) : EstadoSolicitud
    data class Rechazada(val motivo: String) : EstadoSolicitud
}

/**
 * Panel que mostrará la información del viaje
 * */
@Composable
fun PanelInfoViaje(viaje: ViajeUi, onVerRuta: () -> Unit = {}, onContactar: () -> Unit = {}, onCancelar: () -> Unit = {}) {
    Spacer(Modifier.height(16.dp))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Conductor + valoración
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(viaje.conductor, style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${viaje.valoracion}")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Vehículo y plazas
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EtiquetaDato(Icons.Default.DirectionsCar, "${viaje.vehiculo} · ${viaje.color}")
                EtiquetaDato(Icons.Default.EventSeat, "Plazas: ${viaje.plazasDisponibles}")
            }

            Spacer(Modifier.height(12.dp))

            // Distancia / Duración / Horas
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EtiquetaDato(Icons.Default.Info, viaje.distancia)
                EtiquetaDato(Icons.Default.AccessTime, viaje.duracion)
                EtiquetaDato(Icons.Default.Schedule, "Sale ${viaje.horaRecogida}")
            }

            Spacer(Modifier.height(12.dp))

            // Puntos del viaje
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FilaPunto("Recogida", viaje.puntoRecogida)
                FilaPunto("Destino", viaje.puntoDestino)
            }

            Spacer(Modifier.height(16.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onContactar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Contactar") }

                Button(
                    onClick = onVerRuta,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Ver ruta") }
            }

            TextButton(
                onClick = onCancelar,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Cancelar petición") }
        }
    }
}

@Composable
private fun EtiquetaDato(icono: ImageVector, etiqueta: String) {
    AssistChip(
        onClick = { },
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    )
}

@Composable
private fun FilaPunto(titulo: String, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$titulo: ", style = MaterialTheme.typography.labelMedium)
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}
