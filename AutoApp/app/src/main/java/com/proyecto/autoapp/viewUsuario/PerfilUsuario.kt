package com.proyecto.autoapp.viewUsuario

import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.proyecto.autoapp.BuildConfig
import com.proyecto.autoapp.general.Rutas
import com.proyecto.autoapp.general.modelo.enumClass.Estado
import com.proyecto.autoapp.inicio.login.LoginVM
import com.proyecto.autoapp.ui.theme.*
import com.proyecto.autoapp.viewUsuario.perfilVM.PerfilVM
import java.io.File
import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import java.util.Calendar


/**
 * Muestra la view de perfil del usuario.
 * Se muestran los datos del usuario, si es conductor, pasajero o ninguna.
 * Permite subir los datos del vehículo asociado/s
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuario(perfilVM: PerfilVM, navController: NavController, loginVM: LoginVM) {
    var TAG = "Jose"
    var context = LocalContext.current
    val uiState by perfilVM.uiState.collectAsState()
    var usuario = loginVM.uidActual
    var mostrarDialogoElegirFuente by remember { mutableStateOf(false) }
    var tempFile by remember { mutableStateOf<File?>(null) }

    val fechaNacimiento = uiState.fechaNacimiento

    val fechaNacimientoTexto = remember(fechaNacimiento) {
        if (fechaNacimiento == null) {
            ""
        } else {
            val cal = Calendar.getInstance().apply {
                timeInMillis = fechaNacimiento
            }

            val dia = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
            val mes = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
            val anio = cal.get(Calendar.YEAR).toString()

            "$dia/$mes/$anio"
        }
    }

    val primerAcceso = perfilVM.usuarioNuevo

    /**
     * Permisos de los launcher de la galería y la cámada para poder trabajar con storage
     * */
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val file = tempFile
            if (file != null) {
                val uri = Uri.fromFile(file)

                // Guardamos el file en el VM para usarlo luego al guardar
                perfilVM.setNuevaFotoFile(file)

                // Actualizamos la UI (se verá en el perfil y habilita "Guardar cambios")
                perfilVM.onFotoPerfilSeleccionadaLocal(uri.toString())
            } else {
                Toast.makeText(context, "No se pudo acceder al archivo de la foto", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "No se tomó ninguna foto", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val directorio = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile("pfp", ".jpg", directorio)
            tempFile = file

            val uri = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            )
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val temp = File.createTempFile("GAL_", ".jpg", context.cacheDir)
            temp.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }

            val localUri = Uri.fromFile(temp)

            perfilVM.setNuevaFotoFile(temp)
            perfilVM.onFotoPerfilSeleccionadaLocal(localUri.toString())
        }
    }
    /** =================================================== */

    var showExitDialog by remember { mutableStateOf(false) }
    var showDialogPassword by remember { mutableStateOf(false) }

    if (mostrarDialogoElegirFuente) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoElegirFuente = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFF1A1A1A),
            tonalElevation = 8.dp,
            title = {
                Text(
                    text = "Seleccionar origen",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "¿Cómo quieres establecer tu foto de perfil?",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoElegirFuente = false
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThumbsUpMustard,
                        contentColor = Color(0xFF1A1A1A)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF1A1A1A)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Usar cámara",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        mostrarDialogoElegirFuente = false
                        galleryLauncher.launch("image/*")
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ThumbsUpMustard),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = ThumbsUpMustard
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = ThumbsUpMustard
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Elegir de galería",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            },
            modifier = Modifier
                .border(1.dp, ThumbsUpMustard, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        )
    }

    if (showDialogPassword) {
        CambiarPasswordDialog(
            onDismiss = { showDialogPassword = false },
            onGuardar = { actual, nueva, confirmar ->
                perfilVM.cambiarPassword(actual, nueva, confirmar) { ok, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (ok){
                        showDialogPassword = false
                    }
                }
            }
        )
    }

    LaunchedEffect(usuario) {
        if (usuario.isNotBlank()) {
            perfilVM.cargarUsuario(usuario)
            Log.e(TAG, "Este es el estado del usuario al registrar $primerAcceso")
        }
    }

    DialogoSalirThumbsUp(
        visible = showExitDialog,
        onSalirIgualmente = {
            showExitDialog = false
            if (primerAcceso) {
                navController.navigate(Rutas.ViewViajero) {
                    popUpTo(Rutas.ViewInicial) { inclusive = false }
                    launchSingleTop = true
                }
            } else {
                navController.popBackStack()
            }
        },
        onCancelar = {
            showExitDialog = false
        },
        onDismiss = {
            showExitDialog = false
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ThumbsUpPurple,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mi perfil",
                        color = ThumbsUpTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.isSaveEnabled) {
                                showExitDialog = true
                            } else {
                                if (primerAcceso) {
                                    navController.navigate(Rutas.ViewViajero) {
                                        popUpTo(Rutas.ViewInicial) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.popBackStack()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = ThumbsUpMustard
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ThumbsUpPurple
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ThumbsUpPurple)
                    .padding(horizontal = 24.dp, vertical = 30.dp)
            ) {
                Button(
                    onClick = {
                        val edadNum = uiState.edad.toIntOrNull()
                        when {
                            edadNum == null -> {
                                Toast.makeText(context, "Introduce una fecha de nacimiento válida", Toast.LENGTH_SHORT).show()
                            }
                            edadNum < 16 -> {
                                Toast.makeText(context, "Debes tener al menos 16 años", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                perfilVM.modPerfilUsuario(usuario) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
                                    }
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
                        containerColor = ThumbsUpMustard,
                        contentColor = ThumbsUpSurfaceDark,
                        disabledContainerColor = ThumbsUpMustard.copy(alpha = 0.5f),
                        disabledContentColor = ThumbsUpSurfaceDark.copy(alpha = 0.5f)
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
            FotoPerfilUsuario(
                fotoPerfilUrl = uiState.fotoPerfilUrl,
                onChangeFotoPerfil = {
                    mostrarDialogoElegirFuente = true
                },
                onManageGaleria = {
                    /**
                     * Más adelante este botón debería gestionar solamente las fotos de la galería del perfil
                     * del usuario.
                     * */
                    navController.navigate(Rutas.Galeria)
                },
                ThumbUpCard = ThumbsUpCard,
                ThumbUpTextPrimary = ThumbsUpTextPrimary,
                ThumbUpTextSecondary = ThumbsUpTextSecondary,
                ThumbUpMustard = ThumbsUpMustard
            )

            // 2. DATOS PERSONALES DEL USUARIO
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ThumbsUpCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = { },
                        label = { Text("Nombre", color = ThumbsUpTextSecondary) },
                        singleLine = true,
                        enabled = false,
                        textStyle = TextStyle(color = ThumbsUpTextPrimary),
                        colors = ThumbsUpTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = uiState.apellidos,
                        onValueChange = { },
                        label = { Text("Apellidos", color = ThumbsUpTextSecondary) },
                        singleLine = true,
                        enabled = false,
                        textStyle = TextStyle(color = ThumbsUpTextPrimary),
                        colors = ThumbsUpTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fechaNacimientoTexto,
                            onValueChange = { },
                            label = { Text("Fecha de nacimiento", color = ThumbsUpTextSecondary) },
                            singleLine = true,
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth(),
                            textStyle = TextStyle(color = ThumbsUpTextPrimary),
                            colors = ThumbsUpTextFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.edad,
                                onValueChange = { },
                                label = { Text("Edad", color = ThumbsUpTextSecondary) },
                                singleLine = true,
                                enabled = false,
                                textStyle = TextStyle(color = ThumbsUpTextPrimary),
                                colors = ThumbsUpTextFieldColors(disabled = true),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (uiState.showEdadWarningConductor) {
                                Text(
                                    text = "Para ser conductor debes ser mayor de edad.",
                                    color = ThumbsUpDanger,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.telefono,
                        onValueChange = { perfilVM.onTelefonoChange(it) },
                        label = { Text("Teléfono", color = ThumbsUpTextSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        textStyle = TextStyle(color = ThumbsUpTextPrimary),
                        colors = ThumbsUpTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = { showDialogPassword = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThumbsUpPurple,
                            contentColor = ThumbsUpTextPrimary    // blanco
                        )
                    ) {
                        Text(
                            text = "Cambiar contraseña",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    /**
                     * Como no puede modificarse, hacer que muestre el email del usuario concreto.
                     */
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { },
                        label = { Text("Email", color = ThumbsUpTextSecondary) },
                        singleLine = true,
                        enabled = false,
                        textStyle = TextStyle(color = ThumbsUpTextPrimary),
                        colors = ThumbsUpTextFieldColors(disabled = true),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 3. ROLES - PASAJERO / CONDUCTOR
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ThumbsUpCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = "¿Cómo quieres usar ThumbsUp?",
                        color = ThumbsUpTextPrimary,
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
                                    ThumbsUpMustard.copy(alpha = 0.12f)
                                else
                                    ThumbsUpSurfaceDark.copy(alpha = 0.3f)
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
                                color = ThumbsUpTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Pide trayectos a otros conductores",
                                color = ThumbsUpTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Switch(
                            checked = uiState.isPasajeroSelected,
                            onCheckedChange = { checked ->
                                perfilVM.onPasajeroToggle(checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ThumbsUpMustard,
                                checkedTrackColor = ThumbsUpMustard.copy(alpha = 0.4f)
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
                                    ThumbsUpMustard.copy(alpha = 0.12f)
                                else
                                    ThumbsUpSurfaceDark.copy(alpha = 0.3f)
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
                                color = ThumbsUpTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Ofrece tu coche y propone rutas",
                                color = ThumbsUpTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Switch(
                            checked = uiState.isConductorSelected,
                            onCheckedChange = { checked ->
                                perfilVM.onConductorToggle(checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ThumbsUpMustard,
                                checkedTrackColor = ThumbsUpMustard.copy(alpha = 0.4f)
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
                    colors = CardDefaults.cardColors(containerColor = ThumbsUpCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            text = "Pasajero",
                            color = ThumbsUpTextPrimary,
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
                            textColor = ThumbsUpTextPrimary,
                            subColor = ThumbsUpTextSecondary
                        )

                        InfoRowLabelValue(
                            label = "Reputación",
                            value = "${uiState.pasajeroRatingAvg} ★ (${uiState.pasajeroRatingCount} valoraciones)",
                            textColor = ThumbsUpTextPrimary,
                            subColor = ThumbsUpTextSecondary
                        )
                    }
                }
            }

            // 5. BLOQUE INFO CONDUCTOR
            if (uiState.isConductorSelected) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ThumbsUpCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        Text(
                            text = "Conductor",
                            color = ThumbsUpTextPrimary,
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
                            textColor = ThumbsUpTextPrimary,
                            subColor = ThumbsUpTextSecondary
                        )

                        InfoRowLabelValue(
                            label = "Reputación",
                            value = "${uiState.conductorRatingAvg} ★ (${uiState.conductorRatingCount} valoraciones)",
                            textColor = ThumbsUpTextPrimary,
                            subColor = ThumbsUpTextSecondary
                        )

                        // Permiso de conducir / verificación conductor
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Permiso de conducir",
                                color = ThumbsUpTextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ThumbsUpSurfaceDark.copy(alpha = 0.4f))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = if (uiState.licenciaSubida) "Documento enviado" else "Pendiente de subir",
                                        color = ThumbsUpTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (uiState.licenciaVerificada) "Verificada" else "En revisión",
                                        color = if (uiState.licenciaVerificada) ThumbsUpMustard else ThumbsUpTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Button(
                                    onClick = { /* subir licencia */ },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ThumbsUpMustard,
                                        contentColor = ThumbsUpSurfaceDark
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
                                color = ThumbsUpTextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )

                            Text(
                                text = "Sube una foto clara del coche donde se vea el color y la matrícula.",
                                color = ThumbsUpTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ThumbsUpSurfaceDark.copy(alpha = 0.4f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // preview imagen vehículo
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ThumbsUpSurfaceDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.vehiculoFotoUrl != null) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = ThumbsUpMustard,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = "Añadir vehículo",
                                            tint = ThumbsUpTextSecondary,
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
                                        color = ThumbsUpTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = uiState.vehiculoDescripcion.ifBlank {
                                            "Marca / modelo / color / matrícula"
                                        },
                                        color = ThumbsUpTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        perfilVM.onShowEditorVehiculo()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ThumbsUpMustard,
                                        contentColor = ThumbsUpSurfaceDark
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
                                        containerColor = ThumbsUpSurfaceDark.copy(alpha = 0.4f)
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
                                                    color = ThumbsUpTextSecondary
                                                )
                                            },
                                            singleLine = true,
                                            textStyle = TextStyle(color = ThumbsUpTextPrimary),
                                            colors = ThumbsUpTextFieldColors(),
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
                                                    color = ThumbsUpTextSecondary
                                                )
                                            },
                                            singleLine = true,
                                            textStyle = TextStyle(color = ThumbsUpTextPrimary),
                                            colors = ThumbsUpTextFieldColors(),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        // COLOR
                                        OutlinedTextField(
                                            value = uiState.vehiculoColor,
                                            onValueChange = {
                                                perfilVM.onColorChange(it)
                                            },
                                            label = { Text("Color", color = ThumbsUpTextSecondary) },
                                            singleLine = true,
                                            textStyle = TextStyle(color = ThumbsUpTextPrimary),
                                            colors = ThumbsUpTextFieldColors(),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        // SUBIR FOTO DEL VEHÍCULO
                                        Button(
                                            onClick = {

                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ThumbsUpMustard,
                                                contentColor = ThumbsUpSurfaceDark
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
                                                    containerColor = ThumbsUpMustard,
                                                    contentColor = ThumbsUpSurfaceDark
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
                                        color = ThumbsUpTextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )

                                    uiState.vehiculosGuardados.forEach { coche ->
                                        // coche sería algo tipo "Seat Ibiza • 1234-ABC • Rojo • 2015"
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(ThumbsUpSurfaceDark.copy(alpha = 0.4f))
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsCar,
                                                contentDescription = null,
                                                tint = ThumbsUpMustard,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .padding(end = 8.dp)
                                            )

                                            Text(
                                                text = coche.modelo,
                                                color = ThumbsUpTextPrimary,
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
