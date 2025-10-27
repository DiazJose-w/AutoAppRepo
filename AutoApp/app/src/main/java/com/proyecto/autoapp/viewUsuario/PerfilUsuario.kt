package com.proyecto.autoapp.viewUsuario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.proyecto.autoapp.general.modelo.enumClass.Estado
import com.proyecto.autoapp.viewUsuario.perfilVM.PerfilVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilRoute(perfilVM: PerfilVM, navController: NavController) {
    val uiState by perfilVM.uiState.collectAsState()

    // Colores base ThumbsUp (hardcodeados, luego migran a Theme)
    val ThumbUpPurple = Color(0xFF2B1D3E)
    val ThumbUpSurfaceDark = Color(0xFF1A1A1A)
    val ThumbUpCard = Color(0xFF2A2A2A)
    val ThumbUpTextPrimary = Color(0xFFFFFFFF)
    val ThumbUpTextSecondary = Color(0xFFB3B3B3)
    val ThumbUpMustard = Color(0xFFE09810)
    val ThumbUpDanger = Color(0xFFFF4D4D)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ThumbUpPurple,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi perfil",
                        color = ThumbUpTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = ThumbUpTextPrimary
                        )
                    }
                },
                colors = topAppBarColors(
                    containerColor = ThumbUpPurple,
                    navigationIconContentColor = ThumbUpTextPrimary,
                    titleContentColor = ThumbUpTextPrimary
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ThumbUpPurple)
                    .padding(horizontal = 24.dp, vertical = 30.dp)
            ) {
                Button(
                    onClick = {
                        /**
                         * Implementar la lógica de guardado de cambios. Llamar aquí al viewModel
                         * */
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = uiState.isSaveEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThumbUpMustard,
                        contentColor = ThumbUpSurfaceDark,
                        disabledContainerColor = ThumbUpMustard.copy(alpha = 0.5f),
                        disabledContentColor = ThumbUpSurfaceDark.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "Guardar cambios",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // 1. FOTOS / AVATAR
            PerfilFotoSection(
                fotoPerfilUrl = uiState.fotoPerfilUrl,
                onChangeFotoPerfil = {
                    /**
                     * Implementar lógica cambio de foto de perfil
                     * */
                },
                onManageGaleria = {
                    /**
                     * Implementar lógica cambio añadir imágenes a la galería
                     * */
                },
                ThumbUpCard = ThumbUpCard,
                ThumbUpTextPrimary = ThumbUpTextPrimary,
                ThumbUpTextSecondary = ThumbUpTextSecondary,
                ThumbUpMustard = ThumbUpMustard
            )

            // 2. DATOS BÁSICOS DEL USUARIO
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ThumbUpCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = {

                        },
                        label = { Text("Nombre", color = ThumbUpTextSecondary) },
                        singleLine = true,
                        textStyle = TextStyle(color = ThumbUpTextPrimary),
                        colors = ThumbUpTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = uiState.apellidos,
                        onValueChange = {

                        },
                        label = { Text("Apellidos", color = ThumbUpTextSecondary) },
                        singleLine = true,
                        textStyle = TextStyle(color = ThumbUpTextPrimary),
                        colors = ThumbUpTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.edad,
                            onValueChange = {

                            },
                            label = { Text("Edad", color = ThumbUpTextSecondary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            textStyle = TextStyle(color = ThumbUpTextPrimary),
                            colors = ThumbUpTextFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (uiState.showEdadWarningConductor) {
                            Text(
                                text = "Para ser conductor debes ser mayor de edad.",
                                color = ThumbUpDanger,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    /**
                     * Como no puede modificarse, hacer que muestre el email del usuario concreto.
                     * */
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { },
                        label = { Text("Email", color = ThumbUpTextSecondary) },
                        singleLine = true,
                        enabled = false,
                        textStyle = TextStyle(color = ThumbUpTextPrimary),
                        colors = ThumbUpTextFieldColors(disabled = true),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 3. ROLES - PASAJERO / CONDUCTOR
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ThumbUpCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = "¿Cómo quieres usar ThumbsUp?",
                        color = ThumbUpTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Toggle Pasajero
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (uiState.isPasajeroSelected)
                                    ThumbUpMustard.copy(alpha = 0.12f)
                                else
                                    ThumbUpSurfaceDark.copy(alpha = 0.3f)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Quiero viajar (Pasajero)",
                                color = ThumbUpTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Pide trayectos a otros conductores",
                                color = ThumbUpTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Switch(
                            checked = uiState.isPasajeroSelected,
                            onCheckedChange = { checked ->
                                perfilVM.onPasajeroToggle(checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ThumbUpMustard,
                                checkedTrackColor = ThumbUpMustard.copy(alpha = 0.4f)
                            )
                        )
                    }

                    // Toggle Conductor
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (uiState.isConductorSelected)
                                    ThumbUpMustard.copy(alpha = 0.12f)
                                else
                                    ThumbUpSurfaceDark.copy(alpha = 0.3f)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Quiero llevar gente (Conductor)",
                                color = ThumbUpTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Ofrece tu coche y propone rutas",
                                color = ThumbUpTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Switch(
                            checked = uiState.isConductorSelected,
                            onCheckedChange = { checked ->
                                perfilVM.onConductorToggle(checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ThumbUpMustard,
                                checkedTrackColor = ThumbUpMustard.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }

            // 4. BLOQUE INFO PASAJERO
            if (uiState.isPasajeroSelected) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ThumbUpCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            text = "Pasajero",
                            color = ThumbUpTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        /**
                         * Mientras el pasajero no esté activo no puede solicitar viajes
                         * Para poder estar activo tiene que cumplir con unos campos mínimos.
                         * */
                        InfoRowLabelValue(
                            label = "Estado",
                            value = when (uiState.pasajeroEnabled) {
                                Estado.ACTIVO -> "Activo"
                                Estado.BLOQUEADO -> "Bloqueado"
                                else -> "Pendiente"
                            },
                            textColor = ThumbUpTextPrimary,
                            subColor = ThumbUpTextSecondary
                        )

                        InfoRowLabelValue(
                            label = "Reputación",
                            value = "${uiState.pasajeroRatingAvg} ★ (${uiState.pasajeroRatingCount} valoraciones)",
                            textColor = ThumbUpTextPrimary,
                            subColor = ThumbUpTextSecondary
                        )
                    }
                }
            }

            // 5. BLOQUE INFO CONDUCTOR
            if (uiState.isConductorSelected) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ThumbUpCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        Text(
                            text = "Conductor",
                            color = ThumbUpTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        /**
                         * Mientras el conductor no esté activo no puede solicitar viajes
                         * Para poder estarlo tiene que cumplir con unos campos mínimos.
                         * */
                        InfoRowLabelValue(
                            label = "Estado",
                            value = when (uiState.conductorEnabled) {
                                Estado.ACTIVO -> "Activo"
                                Estado.BLOQUEADO -> "Bloqueado"
                                else -> "Pendiente verificación"
                            },
                            textColor = ThumbUpTextPrimary,
                            subColor = ThumbUpTextSecondary
                        )

                        InfoRowLabelValue(
                            label = "Reputación",
                            value = "${uiState.conductorRatingAvg} ★ (${uiState.conductorRatingCount} valoraciones)",
                            textColor = ThumbUpTextPrimary,
                            subColor = ThumbUpTextSecondary
                        )

                        // Permiso de conducir / verificación conductor
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Permiso de conducir",
                                color = ThumbUpTextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ThumbUpSurfaceDark.copy(alpha = 0.4f))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = if (uiState.licenciaSubida) "Documento enviado" else "Pendiente de subir",
                                        color = ThumbUpTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (uiState.licenciaVerificada) "Verificada" else "En revisión",
                                        color = if (uiState.licenciaVerificada) ThumbUpMustard else ThumbUpTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Button(
                                    onClick = { /* subir licencia */ },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ThumbUpMustard,
                                        contentColor = ThumbUpSurfaceDark
                                    )
                                ) {
                                    Text(
                                        text = if (uiState.licenciaSubida) "Actualizar" else "Subir",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // Vehículo
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Vehículo",
                                color = ThumbUpTextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )

                            Text(
                                text = "Sube una foto clara del coche donde se vea el color y la matrícula.",
                                color = ThumbUpTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ThumbUpSurfaceDark.copy(alpha = 0.4f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // preview imagen vehículo
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ThumbUpSurfaceDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.vehiculoFotoUrl != null) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = ThumbUpMustard,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = "Subir vehículo",
                                            tint = ThumbUpTextSecondary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = if (uiState.vehiculoFotoUrl != null) "Foto subida" else "Sin foto",
                                        color = ThumbUpTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = uiState.vehiculoDescripcion.ifBlank {
                                            "Marca / modelo / color / matrícula"
                                        },
                                        color = ThumbUpTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Button(
                                    onClick = {

                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ThumbUpMustard,
                                        contentColor = ThumbUpSurfaceDark
                                    )
                                ) {
                                    Text(
                                        text = if (uiState.vehiculoFotoUrl != null) "Actualizar" else "Subir",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

// =====================================================
// Componentes para la vista de perfil. EN UN FUTURO UNIFICARLO EN UNA CLASE COMÚN DE ESTILOS
// =====================================================

@Composable
private fun PerfilFotoSection(fotoPerfilUrl: String?, onChangeFotoPerfil: () -> Unit, onManageGaleria: () -> Unit,
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
private fun InfoRowLabelValue(label: String, value: String, textColor: Color, subColor: Color) {
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
private fun ThumbUpTextFieldColors(disabled: Boolean = false): TextFieldColors {
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
