package com.proyecto.autoapp.viewUsuario

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.proyecto.autoapp.general.modelo.enumClass.Estado
import com.proyecto.autoapp.ui.theme.*
import com.proyecto.autoapp.viewUsuario.perfilVM.PerfilVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilRoute(perfilVM: PerfilVM, navController: NavController) {
    val uiState by perfilVM.uiState.collectAsState()

    // Datos del vehículo.
    var modelo by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }

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
                        /**
                         * No puede ser navController.popBackStack() porque
                         * cuando terminas de registrar, si le das, vuelve al punto anterior.
                         *
                         * Hay que hacer que vuelva a la página principal o directamente quitarlo y añadir un botón
                         * para ir a la página principal
                         *
                         * */
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
                                            contentDescription = "Añadir vehículo",
                                            tint = ThumbUpTextSecondary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                /**
                                 * Modificar todo el apartado del vehículo.
                                 * No se sube la foto, sino que se abre otro desplegable donde se ve otro bloque
                                 * donde se va a añadir la información del coche modelo, matrícula, color y año
                                 * donde tendrá además su propio botón para añadir el coche a la base de datos
                                 * de la persona.
                                 */
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
                                        /**
                                         * Este botón ya NO sube directamente la foto.
                                         *
                                         * Ahora sirve para DESPLEGAR un bloque adicional en esta misma pantalla
                                         * donde se van a rellenar los datos del coche (modelo, matrícula, año, color),
                                         * se podrá subir la foto del vehículo desde allí,
                                         * y habrá un botón para guardar ese coche en la base de datos
                                         * de la persona usuaria.
                                         *
                                         * El bloque se controla con una bandera tipo uiState.showVehiculoEditor
                                         * en el viewModel.
                                         */
                                        perfilVM.uiState
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ThumbUpMustard,
                                        contentColor = ThumbUpSurfaceDark
                                    )
                                ) {
                                    Text(
                                        text = "Añadir",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            /**
                             * BLOQUE DESPLEGABLE DEL VEHÍCULO
                             * Aquí es donde realmente se añaden los datos del coche.
                             *
                             * IMPORTANTE:
                             *  - Los campos SIEMPRE aparecen vacíos para añadir vehículos nuevos.
                             *  - Debajo de este editor, aparecerá el listado de coches ya añadidos
                             *    y vinculados a la persona usuaria.
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

                                        // AÑO
                                        OutlinedTextField(
                                            value = uiState.vehiculoAnio,
                                            onValueChange = {

                                            },
                                            label = { Text("Año", color = ThumbUpTextSecondary) },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number
                                            ),
                                            textStyle = TextStyle(color = ThumbUpTextPrimary),
                                            colors = ThumbUpTextFieldColors(),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        // COLOR
                                        OutlinedTextField(
                                            value = uiState.vehiculoColor,
                                            onValueChange = {

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
                                                /**
                                                 * Aquí sí se abriría el selector de imagen/foto del coche.
                                                 * Esto asociará la imagen al vehículo que se está creando.
                                                 */
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
                                                    uiState.showVehiculoEditor
                                                    /**
                                                     * Guardar este vehículo en la base de datos
                                                     * de la persona usuaria.
                                                     *
                                                     * Después de guardar:
                                                     *  - Se vacían los campos
                                                     *  - Se podría cerrar el desplegable
                                                     *  - Se actualiza la lista de vehículos guardados
                                                     *
                                                     *  Implementar el viewModel
                                                     */
                                                },
                                                shape = RoundedCornerShape(10.dp),
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