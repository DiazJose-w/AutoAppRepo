package com.proyecto.autoapp.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.proyecto.autoapp.general.modelo.dataClass.ViajeUi
import com.proyecto.autoapp.general.modelo.enumClass.AccionDialogo
import com.proyecto.autoapp.general.modelo.enumClass.EstadoPeticion
import com.proyecto.autoapp.general.modelo.peticiones.Peticion

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
fun FotoPerfilUsuario(fotoPerfilUrl: String?, onChangeFotoPerfil: () -> Unit, onManageGaleria: () -> Unit,
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
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3A3A3A))
                        .clickable { onChangeFotoPerfil() },
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoPerfilUrl.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Foto de perfil",
                            tint = ThumbUpMustard,
                            modifier = Modifier.size(80.dp)
                        )
                    } else {
                        AsyncImage(
                            model = fotoPerfilUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Botón flotante pequeño "cambiar"
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)        // un pelín hacia fuera para que no se corte
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(ThumbUpMustard)
                        .border(2.dp, Color(0xFF1A1A1A), CircleShape)
                        .clickable { onChangeFotoPerfil() },
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
 * Panel que muestra la información del viaje.
 * */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun PanelEstadoPeticion(fotoConductor: String?, nombreConductor: String, estado: EstadoPeticion, onAccionSeleccionada: (AccionDialogo) -> Unit,
    onMostrarInfoViaje: () -> Unit, onCancelarViaje: () -> Unit, modifier: Modifier = Modifier, contentDescription: String? = null) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .border(1.dp, ThumbUpMustard, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { /* click normal en la card, lo dejamos vacío */ },
                onLongClick = {
                    // Long click en la card → cancelar viaje
                    onCancelarViaje()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Columna con foto + nombre
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = fotoConductor,
                        contentDescription = contentDescription,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, ThumbUpMustard, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = nombreConductor,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Spacer(Modifier.width(16.dp))
                // Columna de acciones, depende del estado
                when (estado) {
                    EstadoPeticion.OFERTA_CONDUCTOR -> {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onAccionSeleccionada(AccionDialogo.ACEPTAR) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ThumbUpMustard,
                                    contentColor = ThumbUpSurfaceDark
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Aceptar viaje",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            OutlinedButton(
                                onClick = { onAccionSeleccionada(AccionDialogo.RECHAZAR) },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, ThumbUpMustard),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = ThumbUpMustard
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Rechazar",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                    EstadoPeticion.ACEPTADA -> {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Viaje concertado",
                                color = ThumbUpMustard,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Button(
                                onClick = { onMostrarInfoViaje() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ThumbUpMustard,
                                    contentColor = ThumbUpSurfaceDark
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Ver información del viaje",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            Text(
                                text = "Mantén pulsado para cancelar el viaje",
                                color = Color.Gray,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    EstadoPeticion.PENDIENTE -> { }
                }
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

/**
 * Dialogo de confirmación común para ThumbsUp
 * */
@Composable
fun DialogoConfirmacionThumbsUp(visible: Boolean, onGuardarYSalir: () -> Unit, onSalirSinGuardar: () -> Unit, onDismiss: () -> Unit) {

    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFF1A1A1A),
            tonalElevation = 8.dp,
            title = {
                Text(
                    text = "Cambios sin guardar",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Has modificado imágenes y aún no han sido guardadas. ¿Quieres guardar antes de salir?",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { onGuardarYSalir() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThumbUpMustard,
                        contentColor = Color(0xFF1A1A1A)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF1A1A1A)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Guardar y salir",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { onSalirSinGuardar() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ThumbUpMustard),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = ThumbUpMustard
                    )
                ) {
                    Text(
                        "Salir sin guardar",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            },
            modifier = Modifier
                .border(1.dp, ThumbUpMustard, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        )
    }
}@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun DialogoSalirThumbsUp(visible: Boolean, onSalirIgualmente: () -> Unit, onCancelar: () -> Unit, onDismiss: () -> Unit) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFF1A1A1A),
            tonalElevation = 8.dp,

            title = {
                Text(
                    text = "Cambios sin guardar",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },

            text = {
                Text(
                    text = "Si sales ahora, los cambios no guardados se perderán. ¿Deseas salir igualmente?",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },

            confirmButton = {
                Button(
                    onClick = { onSalirIgualmente() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThumbUpMustard,
                        contentColor = Color(0xFF1A1A1A)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF1A1A1A)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Salir igualmente",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            },

            dismissButton = {
                OutlinedButton(
                    onClick = { onCancelar() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ThumbUpMustard),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = ThumbUpMustard
                    )
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            },

            modifier = Modifier
                .border(1.dp, ThumbUpMustard, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun ThumbUpAceptarRechazarViaje(visible: Boolean, title: String, message: String, confirmText: String, dismissText: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFF1A1A1A),
            tonalElevation = 8.dp,
            title = {
                Text(
                    text = title,
                    color = ThumbUpTextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            text = {
                Text(
                    text = message,
                    color = ThumbUpTextPrimary.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThumbUpMustard,
                        contentColor = ThumbUpSurfaceDark
                    )
                ) {
                    Text(
                        text = confirmText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ThumbUpMustard),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = ThumbUpMustard
                    )
                ) {
                    Text(
                        text = dismissText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        )
    }
}

/**
 * Diálogo para cerrar sesión
 * */
@Composable
fun CerrarSesion(showDialog: Boolean, onDismiss: () -> Unit, onConfirmCerrarSesion: () -> Unit) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFF1A1A1A),
        tonalElevation = 8.dp,
        title = {
            Text(
                text = "Fin de sesión",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = "¿Deseas cerrar la sesión actual?",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirmCerrarSesion() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ThumbUpMustard,
                    contentColor = Color(0xFF1A1A1A)
                )
            ) {
                Text(
                    "Sí",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ThumbUpMustard),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = ThumbUpMustard
                )
            ) {
                Text(
                    "No",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        },
        modifier = Modifier
            .border(1.dp, ThumbUpMustard, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    )
}
