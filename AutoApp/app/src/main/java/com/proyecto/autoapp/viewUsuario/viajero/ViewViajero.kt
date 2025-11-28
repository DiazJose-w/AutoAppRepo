package com.proyecto.autoapp.viewUsuario.viajero

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.autoapp.R
import com.proyecto.autoapp.general.maps.MapScreen
import com.proyecto.autoapp.general.maps.viewModels.MapViewModel
import com.proyecto.autoapp.general.Rutas
import com.proyecto.autoapp.general.funcionesComunes.isEdadValida
import com.proyecto.autoapp.general.modelo.peticiones.Peticion
import com.proyecto.autoapp.inicio.login.LoginVM
import com.proyecto.autoapp.ui.theme.*
import com.proyecto.autoapp.viewUsuario.PerfilMenu
import com.proyecto.autoapp.viewUsuario.perfilVM.PerfilVM
import com.proyecto.autoapp.viewUsuario.viajero.EstadoSolicitud.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.platform.LocalConfiguration
import com.proyecto.autoapp.general.modelo.enumClass.AccionDialogo
import com.proyecto.autoapp.general.modelo.enumClass.EstadoPeticion
import com.proyecto.autoapp.general.peticiones.PeticionesVM

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViewViajero(mapViewModel: MapViewModel,loginVM: LoginVM,navController: NavController, perfilVM: PerfilVM, peticionesVM: PeticionesVM) {
    val context = LocalContext.current
    val TAG = "jose"
    val uiState by perfilVM.uiState.collectAsState()

    var estadoSolicitud by remember { mutableStateOf<EstadoSolicitud?>(null) }

    // UID del usuario actual
    val usuarioActual = loginVM.uidActual
    val miPeticionState by peticionesVM.miPeticion.collectAsState()

    /**
     * Variables para los diálogos
     */
    var accionDialogo by remember { mutableStateOf<AccionDialogo?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var showDialogCancelar by remember { mutableStateOf(false) }

    // Sincronizar el estado de la petición con lo que venga de Firestore
    LaunchedEffect(miPeticionState) {
        estadoSolicitud = when (val pet = miPeticionState) {
            null -> null
            else -> when (pet.estado) {
                "pendiente" -> Pendiente
                "ofertaConductor" -> OfertaConductor(pet)
                "aceptada" -> Confirmada(pet)
                "confirmadaPorViajero" -> OfertaConductor(pet)
                else -> null
            }
        }
    }

    // Cargar datos de usuario
    LaunchedEffect(usuarioActual) {
        if (usuarioActual.isNotBlank()) {
            perfilVM.cargarUsuario(usuarioActual)
            peticionesVM.observarMiPeticion(usuarioActual)
        } else {
            Log.e(TAG, "Hubo un problema al cargar el usuario actual")
        }
    }

    // Diálogo "activar modo conductor"
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFF1A1A1A),
            tonalElevation = 8.dp,
            title = {
                Text(
                    text = "¿Quieres activar el modo conductor?",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "Para continuar deberás registrar tu vehículo. ¿Deseas activar el modo conductor?",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogo = false
                        perfilVM.modEstadoConductor(usuarioActual) { ok ->
                            if (ok) {
                                navController.navigate(Rutas.ViewConductor)
                            } else {
                                Toast.makeText(context,"Hubo algún error a la hora de añadirte como conductor",Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThumbUpMustard,
                        contentColor = Color(0xFF1A1A1A)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF1A1A1A)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Sí, activar",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarDialogo = false },
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

    // Diálogo cerrar sesión
    CerrarSesion(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onConfirmCerrarSesion = {
            showDialog = false
            loginVM.signOut(context) { result ->
                if (result) {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Rutas.ViewInicial) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                    Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error cerrando sesión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ThumbUpPurple),
        containerColor = ThumbUpPurple
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(ThumbUpPurple)
        ) {
            Image(
                painter = painterResource(R.mipmap.camino_central),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .alpha(0.15f),
                contentScale = ContentScale.FillWidth
            )
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp
            val mapHeight = screenHeight * 0.33f

            // Botón flotante de mensajería
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BadgedBox(
                    badge = {
                        val not = 0
                        if (not >= 0) Badge {
                            Text(not.coerceAtMost(99).toString())
                        }
                    }
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            // navController.navigate(Rutas.Mensajeria)
                        },
                        containerColor = ThumbUpMustard,
                        contentColor = ThumbUpSurfaceDark,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(50))
                            .border(
                                1.dp,
                                ThumbUpSurfaceDark.copy(alpha = 0.4f),
                                RoundedCornerShape(50)
                            )
                    ) {
                        Icon(Icons.Filled.ChatBubble, contentDescription = "Mensajes")
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Menú perfil (fijo arriba)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    PerfilMenu(
                        fotoPerfil = uiState.fotoPerfilUrl,
                        onPerfil = {
                            navController.navigate(Rutas.Perfil)
                        },
                        onHistorial = { },
                        onFavoritos = { },
                        onConfiguracion = { },
                        onLogout = {
                            showDialog = true
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Contenido scrolleable (mapa + formulario + estado)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mapa
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(mapHeight),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        )
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            MapScreen(mapViewModel, true, peticionesVM)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Card inicio/destino + sugerencias
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                ThumbUpMustard,
                                RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val sugerenciasInicio = mapViewModel.sugerenciasInicio
                            val sugerenciasDestino = mapViewModel.sugerenciasDestino

                            OutlinedTextField(
                                value = mapViewModel.inicioTexto,
                                onValueChange = { mapViewModel.onInicioChange(it) },
                                label = {
                                    Text(
                                        "Punto de inicio",
                                        color = ThumbUpMustard,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = ThumbUpTextFieldColors()
                            )

                            if (sugerenciasInicio.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF262626)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        sugerenciasInicio.forEach { pred ->
                                            TextButton(
                                                onClick = { mapViewModel.seleccionarSugerenciaInicio(pred) },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Text(
                                                        text = pred.getPrimaryText(null).toString(),
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    )
                                                    val secondary =
                                                        pred.getSecondaryText(null).toString()
                                                    if (secondary.isNotBlank()) {
                                                        Text(
                                                            text = secondary,
                                                            color = Color.LightGray,
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = mapViewModel.destinoTexto,
                                onValueChange = { mapViewModel.onDestinoChange(it) },
                                label = {
                                    Text(
                                        "Destino",
                                        color = ThumbUpMustard,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = ThumbUpTextFieldColors()
                            )
                            if (sugerenciasDestino.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF262626)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                        sugerenciasDestino.forEach { pred ->
                                            TextButton(
                                                onClick = { mapViewModel.seleccionarSugerenciaDestino(pred) },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(Modifier.fillMaxWidth()) {
                                                    Text(
                                                        text = pred.getPrimaryText(null).toString(),
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    )
                                                    val secondary =
                                                        pred.getSecondaryText(null).toString()
                                                    if (secondary.isNotBlank()) {
                                                        Text(
                                                            text = secondary,
                                                            color = Color.LightGray,
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Botón realizar petición
                    ThumbUpPrimaryButton(
                        text = "Realizar petición",
                        enabled = true,
                        onClick = {
                            peticionesVM.enviarPeticion(usuarioActual, mapViewModel.inicioTexto, mapViewModel.inicioLatLng, mapViewModel.inicioPlaceId,
                                mapViewModel.destinoTexto, mapViewModel.destinoLatLng, mapViewModel.destinoPlaceId) { exito ->
                                if (exito) {
                                    estadoSolicitud = Pendiente
                                    mapViewModel.onInicioChange("")
                                    mapViewModel.onDestinoChange("")
                                } else {
                                    Toast.makeText(context,"Error al enviar",Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    /**
                     * Valor de la petición según su estado
                     */
                    estadoSolicitud?.let { estado ->
                        when (estado) {
                            Pendiente -> {
                                Text(
                                    text = "Esperando conductor...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            is OfertaConductor -> {
                                Log.e(TAG, "estado ${estado.peticion}")
                                val pet = estado.peticion
                                val nombreConductor =
                                    pet.infoConductor?.nombre ?: "Conductor"
                                val fotoConductor = pet.infoConductor?.foto

                                PanelEstadoPeticion(
                                    fotoConductor = fotoConductor,
                                    nombreConductor = nombreConductor,
                                    estado = EstadoPeticion.OFERTA_CONDUCTOR,
                                    contentDescription = uiState.fotoPerfilUrl,
                                    onAccionSeleccionada = { accion ->
                                        accionDialogo = accion
                                    },
                                    onMostrarInfoViaje = { },
                                    onCancelarViaje = { }
                                )

                                ThumbUpAceptarRechazarViaje(
                                    visible = accionDialogo != null,
                                    title = when (accionDialogo) {
                                        AccionDialogo.ACEPTAR -> "Aceptar viaje"
                                        AccionDialogo.RECHAZAR -> "Rechazar viaje"
                                        else -> ""
                                    },
                                    message = when (accionDialogo) {
                                        AccionDialogo.ACEPTAR -> "El conductor quiere recogerte. ¿Quieres confirmar este viaje?"
                                        AccionDialogo.RECHAZAR -> "¿Seguro que deseas rechazar esta oferta del conductor?"
                                        else -> ""
                                    },
                                    confirmText = when (accionDialogo) {
                                        AccionDialogo.ACEPTAR -> "Aceptar"
                                        AccionDialogo.RECHAZAR -> "Rechazar"
                                        else -> ""
                                    },
                                    dismissText = "Cancelar",
                                    onConfirm = {
                                        when (accionDialogo) {
                                            AccionDialogo.ACEPTAR -> {
                                                peticionesVM.aceptarOfertaViajero(pet) { ok ->
                                                    if (ok) {
                                                        estadoSolicitud = Confirmada(pet)
                                                    } else {
                                                        Toast.makeText(context,"No se pudo confirmar el viaje",Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                            AccionDialogo.RECHAZAR -> {
                                                peticionesVM.rechazarOfertaViajero(pet) { ok ->
                                                    if (!ok) {
                                                        Toast.makeText(context,"No se pudo rechazar la oferta", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                            else -> {}
                                        }
                                        accionDialogo = null
                                    },
                                    onDismiss = { accionDialogo = null }
                                )
                            }
                            is Confirmada -> {
                                Log.e(TAG, "estado ${estado.peticion}")
                                val pet = estado.peticion
                                val nombreConductor = pet.infoConductor?.nombre ?: "Conductor"
                                val fotoConductor = pet.infoConductor?.foto

                                PanelEstadoPeticion(
                                    fotoConductor = fotoConductor,
                                    nombreConductor = nombreConductor,
                                    estado = EstadoPeticion.ACEPTADA,
                                    contentDescription = uiState.fotoPerfilUrl,
                                    onAccionSeleccionada = { },
                                    onMostrarInfoViaje = { },
                                    onCancelarViaje = {
                                        showDialogCancelar = true
                                    }
                                )

                                // Diálogo de confirmación de CANCELAR viaje
                                ThumbUpAceptarRechazarViaje(
                                    visible = showDialogCancelar,
                                    title = "Cancelar viaje",
                                    message = "¿Seguro que quieres cancelar este viaje? El conductor dejará de ver tu ubicación.",
                                    confirmText = "Cancelar viaje",
                                    dismissText = "Volver",
                                    onConfirm = {
                                        peticionesVM.cancelarViajeViajero(pet) { ok ->
                                            if (ok) {
                                                Toast.makeText(context, "Viaje cancelado", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "No se pudo cancelar el viaje", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        showDialogCancelar = false
                                    },
                                    onDismiss = {
                                        showDialogCancelar = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (uiState.isConductorSelected) {
                            navController.navigate(Rutas.ViewConductor)
                        } else if (!isEdadValida(uiState.edad)) {
                            Toast.makeText(
                                context,
                                "No puedes ser conductor. Eres menor de edad",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            mostrarDialogo = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(
                            1.5.dp,
                            ThumbUpMustard,
                            RoundedCornerShape(14.dp)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = ThumbUpMustard
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        "Modo conductor",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

/*
 * Así evitamos las clases enumeradas. Es otra manera de poder crear subtipos.
 * Solo para esta clase
 */
sealed interface EstadoSolicitud {
    data object Pendiente : EstadoSolicitud
    data class OfertaConductor(val peticion: Peticion) : EstadoSolicitud
    data class Confirmada(val peticion: Peticion) : EstadoSolicitud
}
