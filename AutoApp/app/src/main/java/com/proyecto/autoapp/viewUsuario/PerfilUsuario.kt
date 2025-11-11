package com.proyecto.autoapp.viewUsuario

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.proyecto.autoapp.general.modelo.enumClass.Estado
import com.proyecto.autoapp.ui.theme.*
import com.proyecto.autoapp.viewUsuario.perfilVM.PerfilVM
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilRoute(perfilVM: PerfilVM, navController: NavController) {
    var context = LocalContext.current
    val uiState by perfilVM.uiState.collectAsState()

    // Datos del vehículo.
    var modelo by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }

    perfilVM.cargarUsuario()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ThumbUpPurple,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mi perfil",
                        color = ThumbUpTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ThumbUpPurple
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
                        perfilVM.modPerfilUsuario { success ->
                            if (success) {
                                Toast.makeText(context, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                            } else {
                                if(uiState.edad.isBlank()){
                                    Toast.makeText(context, "Debes añadir tu edad", Toast.LENGTH_SHORT).show()
                                }else{
                                    Toast.makeText(context, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
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
                        enabled = false,
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
                        enabled = false,
                        textStyle = TextStyle(color = ThumbUpTextPrimary),
                        colors = ThumbUpTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.edad,
                            onValueChange = {
                                perfilVM.onEdadChange(it)
                            },
                            label = { Text("Edad", color = ThumbUpTextSecondary) },
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
                                            contentDescription = "Añadir vehículo",
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
                                        text = if (uiState.vehiculoFotoUrl != null) "añadir vehículo" else "Sin foto",
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
                                        perfilVM.onShowEditorVehiculo()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ThumbUpMustard,
                                        contentColor = ThumbUpSurfaceDark
                                    )
                                ) {
                                    Text(
                                        text = if (uiState.showVehiculoEditor) "Cerrar" else "Añadir",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            /**
                             * BLOQUE DESPLEGABLE DEL VEHÍCULO
                             */
                            if (uiState.showVehiculoEditor) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = ThumbUpSurfaceDark.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {

                                        // MODELO
                                        OutlinedTextField(
                                            value = uiState.vehiculoModelo,
                                            onValueChange = {
                                                perfilVM.onModeloChange(it)
                                            },
                                            label = {
                                                Text(
                                                    "Modelo",
                                                    color = ThumbUpTextSecondary
                                                )
                                            },
                                            singleLine = true,
                                            textStyle = TextStyle(color = ThumbUpTextPrimary),
                                            colors = ThumbUpTextFieldColors(),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        // MATRÍCULA
                                        OutlinedTextField(
                                            value = uiState.vehiculoMatricula,
                                            onValueChange = {
                                                perfilVM.onMatriculaChange(it)
                                            },
                                            label = {
                                                Text(
                                                    "Matrícula",
                                                    color = ThumbUpTextSecondary
                                                )
                                            },
                                            singleLine = true,
                                            textStyle = TextStyle(color = ThumbUpTextPrimary),
                                            colors = ThumbUpTextFieldColors(),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        // COLOR
                                        OutlinedTextField(
                                            value = uiState.vehiculoColor,
                                            onValueChange = {
                                                perfilVM.onColorChange(it)
                                            },
                                            label = { Text("Color", color = ThumbUpTextSecondary) },
                                            singleLine = true,
                                            textStyle = TextStyle(color = ThumbUpTextPrimary),
                                            colors = ThumbUpTextFieldColors(),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        // SUBIR FOTO DEL VEHÍCULO
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
                                                text = "Subir foto del vehículo",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            )
                                        }

                                        // BOTÓN GUARDAR VEHÍCULO
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Button(
                                                onClick = {
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                enabled = uiState.isSaveEnableCar,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ThumbUpMustard,
                                                    contentColor = ThumbUpSurfaceDark
                                                )
                                            ) {
                                                Text(
                                                    text = "Guardar vehículo",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // LISTADO DE VEHÍCULOS YA VINCULADOS AL USUARIO, SI LOS HUBIERA
                            if (uiState.vehiculosGuardados.isNotEmpty()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Tus vehículos",
                                        color = ThumbUpTextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )

                                    uiState.vehiculosGuardados.forEach { coche ->
                                        // coche sería algo tipo "Seat Ibiza • 1234-ABC • Rojo • 2015"
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(ThumbUpSurfaceDark.copy(alpha = 0.4f))
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsCar,
                                                contentDescription = null,
                                                tint = ThumbUpMustard,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .padding(end = 8.dp)
                                            )

                                            Text(
                                                text = coche.modelo,
                                                color = ThumbUpTextPrimary,
                                                fontSize = 14.sp
                                            )
                                        }
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
}