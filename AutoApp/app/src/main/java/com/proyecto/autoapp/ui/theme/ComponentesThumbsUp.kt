package com.proyecto.autoapp.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.autoapp.general.modelo.dataClass.ViajeUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarGeneral(titulo: String, onAccion: (Int) -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ThumbUpPurple,
            titleContentColor = ThumbUpMustard
        ),
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = titulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        actions = {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "cerrar página actual",
                tint = Color.White,
                modifier = Modifier
                    .width(24.dp)
                    .clickable { onAccion(1) }
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
    )
}

// =====================================================
// Componentes para la vista de perfil. EN UN FUTURO UNIFICARLO EN UNA CLASE COMÚN DE ESTILOS
// =====================================================

@Composable
fun PerfilFotoSection(fotoPerfilUrl: String?, onChangeFotoPerfil: () -> Unit, onManageGaleria: () -> Unit,
                      ThumbUpCard: Color, ThumbUpTextPrimary: Color, ThumbUpTextSecondary: Color, ThumbUpMustard: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ThumbUpCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Avatar principal
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3A3A)),
                contentAlignment = Alignment.Center
            ) {
                if (fotoPerfilUrl == null) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Foto de perfil",
                        tint = ThumbUpMustard,
                        modifier = Modifier.size(80.dp)
                    )
                } else {
                    // Aquí podrías usar AsyncImage de coil
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Foto de perfil",
                        tint = ThumbUpMustard,
                        modifier = Modifier.size(80.dp)
                    )
                }

                // Botón flotante pequeño "cambiar"
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(ThumbUpMustard)
                        .clickable { onChangeFotoPerfil() }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        tint = Color(0xFF1A1A1A),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Foto de perfil",
                    color = ThumbUpTextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Esta imagen será visible para otros usuarios",
                    color = ThumbUpTextSecondary,
                    fontSize = 12.sp
                )
            }

            // Botón galería
            OutlinedButton(
                onClick = { onManageGaleria() },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ThumbUpMustard),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = ThumbUpMustard
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Collections,
                    contentDescription = null,
                    tint = ThumbUpMustard,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gestionar galería",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ThumbUpMustard
                )
            }
        }
    }
}

// Filas estado reputación del usuario
@Composable
fun InfoRowLabelValue(label: String, value: String, textColor: Color, subColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = subColor,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Colores personalizados de los TextField oscuros
@Composable
fun ThumbUpTextFieldColors(disabled: Boolean = false): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFFE09810),
        unfocusedBorderColor = Color(0xFF555555),
        disabledBorderColor = Color(0xFF444444),
        cursorColor = Color(0xFFE09810),
        focusedLabelColor = Color(0xFFE09810),
        unfocusedLabelColor = Color(0xFFB3B3B3),
        disabledLabelColor = Color(0xFF777777),
        disabledTextColor = Color(0xFFB3B3B3),
        disabledPlaceholderColor = Color(0xFF777777),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White
    )
}

@Composable
fun ThumbUpPrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit, modifier: Modifier = Modifier ) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ThumbUpMustard,
            contentColor = Color(0xFF1A1A1A),
            disabledContainerColor = ThumbUpMustard.copy(alpha = 0.5f),
            disabledContentColor = Color(0xFF1A1A1A).copy(alpha = 0.7f)
        )
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TitulosRegistro(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        color = ThumbUpMustard,
        modifier = Modifier.padding(bottom = 8.dp)
    )
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