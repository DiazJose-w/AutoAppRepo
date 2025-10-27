package com.proyecto.autoapp.viewUsuario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyecto.autoapp.general.Maps.MapScreen
import com.proyecto.autoapp.general.Maps.MapViewModel
import com.proyecto.autoapp.general.modelo.dataClass.ViajeUi
import com.proyecto.autoapp.ui.theme.ThumbUpMustard
import com.proyecto.autoapp.ui.theme.ThumbUpPurple

@Composable
fun ViewInicialUsuario(mapViewModel: MapViewModel) {
    var inicio by remember { mutableStateOf("")  }
    var destino by remember { mutableStateOf("")  }
    var estadoSolicitud by remember { mutableStateOf<EstadoSolicitud>(EstadoSolicitud.Pendiente) }


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ThumbUpPurple),
        containerColor = ThumbUpPurple
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ThumbUpPurple)
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
                    .fillMaxHeight(0.4f)
                    .border(1.dp, ThumbUpMustard, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    MapScreen(mapViewModel)
                }
            }

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .border(1.dp, ThumbUpMustard, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
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
                        label = { Text("Punto de inicio", color = ThumbUpMustard) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ThumbUpMustard,
                            unfocusedBorderColor = ThumbUpMustard.copy(alpha = 0.4f),
                            cursorColor = ThumbUpMustard,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = ThumbUpMustard,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    OutlinedTextField(
                        value = destino,
                        onValueChange = { destino = it },
                        label = { Text("Destino", color = ThumbUpMustard) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ThumbUpMustard,
                            unfocusedBorderColor = ThumbUpMustard.copy(alpha = 0.4f),
                            cursorColor = ThumbUpMustard,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = ThumbUpMustard,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
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
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ThumbUpMustard,
                    contentColor = Color(0xFF1A1A1A)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    "Realizar petición",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
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
                        color = Color.White.copy(alpha = 0.7f)
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
                    .height(54.dp)
                    .border(1.5.dp, ThumbUpMustard, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = ThumbUpMustard
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    "Modo conductor",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
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
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, ThumbUpMustard,
                RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        viaje.conductor,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = ThumbUpMustard
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${viaje.valoracion}",
                            color = ThumbUpMustard,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EtiquetaDato(Icons.Default.DirectionsCar, "${viaje.vehiculo} · ${viaje.color}")
                EtiquetaDato(Icons.Default.EventSeat, "Plazas: ${viaje.plazasDisponibles}")
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EtiquetaDato(Icons.Default.Info, viaje.distancia)
                EtiquetaDato(Icons.Default.AccessTime, viaje.duracion)
                EtiquetaDato(Icons.Default.Schedule, "Sale ${viaje.horaRecogida}")
            }

            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FilaPunto("Recogida", viaje.puntoRecogida)
                FilaPunto("Destino", viaje.puntoDestino)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onContactar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ThumbUpMustard
                    ),
                    border = BorderStroke(1.dp, ThumbUpMustard)
                ) {
                    Text(
                        "Contactar",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = ThumbUpMustard
                    )
                }

                Button(
                    onClick = onVerRuta,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThumbUpMustard,
                        contentColor = Color(0xFF1A1A1A)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        "Ver ruta",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            TextButton(
                onClick = onCancelar,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ThumbUpMustard
                )
            ) {
                Text(
                    "Cancelar petición",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = ThumbUpMustard
                )
            }
        }
    }
}

@Composable
private fun EtiquetaDato(icono: ImageVector, etiqueta: String) {
    AssistChip(
        onClick = { },
        label = {
            Text(
                etiqueta,
                color = ThumbUpMustard,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )
        },
        leadingIcon = {
            Icon(
                icono,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = ThumbUpMustard
            )
        },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, ThumbUpMustard),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color.Transparent,
            labelColor = ThumbUpMustard,
            leadingIconContentColor = ThumbUpMustard
        )
    )
}

@Composable
private fun FilaPunto(titulo: String, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$titulo: ",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = ThumbUpMustard
        )
        Text(
            texto,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}
