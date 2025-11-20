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
import com.proyecto.autoapp.general.maps.MapViewModel
import com.proyecto.autoapp.general.Rutas
import com.proyecto.autoapp.general.funcionesComunes.isEdadValida
import com.proyecto.autoapp.general.modelo.dataClass.ViajeUi
import com.proyecto.autoapp.general.modelo.peticiones.Peticion
import com.proyecto.autoapp.inicio.login.LoginVM
import com.proyecto.autoapp.ui.theme.*
import com.proyecto.autoapp.viewUsuario.PerfilMenu
import com.proyecto.autoapp.viewUsuario.perfilVM.PerfilVM
import com.proyecto.autoapp.viewUsuario.viajero.EstadoSolicitud.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViewInicialUsuario(mapViewModel: MapViewModel, loginVM: LoginVM, navController: NavController, perfilVM: PerfilVM) {
    val context = LocalContext.current
    val TAG = "jose"
    val uiState by perfilVM.uiState.collectAsState()

    // Ya no usas estas dos, pero las dejo porque tú las tenías
    var inicio by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var estadoSolicitud by remember { mutableStateOf<EstadoSolicitud?>(null) }

    // UID del usuario actual
    val usuarioActual = loginVM.uidActual
    var fotoPerfilUrl by remember { mutableStateOf<String?>(null) }
    val miPeticionState by mapViewModel.miPeticion.collectAsState()

    /**
     * Variables para los diálogos
     */
    var showDialogAceptar by remember { mutableStateOf(false) }
    var showDialogRechazar by remember { mutableStateOf(false) }
    var showDialogCancelarViaje by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    // Sincronizar el estado de la petición con lo que venga de Firestore
    LaunchedEffect(miPeticionState) {
        estadoSolicitud = when (val pet = miPeticionState) {
            null -> null
            else -> when (pet.estado) {
                "pendiente" -> Pendiente
                "aceptada" -> OfertaConductor(pet)
                "confirmadaPorViajero" -> {
                    // De momento seguimos mostrándolo como oferta confirmada,
                    // más adelante lo podremos mapear a Confirmada(ViajeUi)
                    OfertaConductor(pet)
                }
                else -> null
            }
        }
    }

    // Cargar datos de usuario
    LaunchedEffect(usuarioActual) {
        if (usuarioActual.isNotBlank()) {
            perfilVM.cargarUsuario(usuarioActual)
            mapViewModel.observarMiPeticion(usuarioActual)
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
                                Toast.makeText(
                                    context,
                                    "Hubo algún error a la hora de añadirte como conductor",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                // Menú perfil
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

                // Mapa
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.30f)
                        .border(
                            width = 1.dp,
                            color = ThumbUpMustard,
                            shape = RoundedCornerShape(16.dp)
                        ),
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
                        MapScreen(mapViewModel)
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
                                                val secondary = pred.getSecondaryText(null).toString()
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
                                                val secondary = pred.getSecondaryText(null).toString()
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

                Spacer(Modifier.height(24.dp))

                // Botón realizar petición
                ThumbUpPrimaryButton(
                    text = "Realizar petición",
                    enabled = true,
                    onClick = {
                        mapViewModel.enviarPeticion(usuarioActual) { exito ->
                            if (exito) {
                                estadoSolicitud = Pendiente
                                mapViewModel.onInicioChange("")
                                mapViewModel.onDestinoChange("")
                            } else {
                                Toast.makeText(context, "Error al enviar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                )

                Spacer(Modifier.height(12.dp))

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
                            val pet = estado.peticion

                            // Aquí podrás sacar nombre/foto/rating del conductor
                            // desde PerfilVM o desde donde quieras.
                            val nombreConductor = "Nombre del conductor"   // TODO: sustituir
                            // val fotoConductor: String? = ...            // TODO: cuando uses AsyncImage

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF1A1A1A)
                                ),
                                border = BorderStroke(1.dp, ThumbUpMustard.copy(alpha = 0.6f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {

                                    Text(
                                        text = "Conductores disponibles",
                                        color = ThumbUpTextPrimary,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )

                                    // Tarjeta del conductor (ahora 1, luego podrás convertirlo en lista)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = nombreConductor,
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                            Text(
                                                text = "Quiere recogerte",
                                                color = ThumbUpTextPrimary.copy(alpha = 0.8f),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }

                                        // TODO: aquí meterás la foto cuando quieras
                                        // AsyncImage(model = fotoConductor, ...)
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Botón ACEPTAR → solo lanza callback al ViewModel
                                        Button(
                                            onClick = { showDialogAceptar = true },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ThumbUpMustard,
                                                contentColor = ThumbUpSurfaceDark
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                "Aceptar viaje",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }

                                        // Botón RECHAZAR → solo lanza callback al ViewModel
                                        OutlinedButton(
                                            onClick = { showDialogRechazar = true },
                                            modifier = Modifier.weight(1f),
                                            border = BorderStroke(1.dp, ThumbUpMustard),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = ThumbUpMustard
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                "Rechazar",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // Diálogo ACEPTAR
                            ThumbUpAceptarRechazarViaje(
                                visible = showDialogAceptar,
                                title = "Aceptar viaje",
                                message = "El conductor quiere recogerte. ¿Quieres confirmar este viaje?",
                                confirmText = "Aceptar",
                                dismissText = "Cancelar",
                                onConfirm = {
                                    showDialogAceptar = false
                                    // Aquí solo delegamos en el ViewModel; él ya hará lo que toque en Firestore
                                    mapViewModel.aceptarOfertaViajero(pet) { ok ->
                                        if (!ok) {
                                            Toast.makeText(
                                                context,
                                                "No se pudo confirmar el viaje",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        // El cambio real de estado lo actualizará observarMiPeticion()
                                    }
                                },
                                onDismiss = { showDialogAceptar = false }
                            )

                            // Diálogo RECHAZAR
                            ThumbUpAceptarRechazarViaje(
                                visible = showDialogRechazar,
                                title = "Rechazar viaje",
                                message = "¿Seguro que deseas rechazar esta oferta del conductor?",
                                confirmText = "Sí, rechazar",
                                dismissText = "Cancelar",
                                onConfirm = {
                                    showDialogRechazar = false
                                    mapViewModel.rechazarOfertaViajero(pet) { ok ->
                                        if (!ok) {
                                            Toast.makeText(
                                                context,
                                                "No se pudo rechazar la oferta",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        // Igual que antes: el estado se refresca desde el ViewModel
                                    }
                                },
                                onDismiss = { showDialogRechazar = false }
                            )
                        }
                        is Confirmada -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { /* futuro: ver ruta/info */ },
                                        onLongClick = { showDialogCancelarViaje = true }
                                    )
                            ) {
                                PanelInfoViaje(
                                    viaje = estado.viaje,
                                    onVerRuta = { /* futuro */ },
                                    onContactar = { /* futuro */ },
                                    onCancelar = { showDialogCancelarViaje = true }
                                )
                            }

                            ThumbUpAceptarRechazarViaje(
                                visible = showDialogCancelarViaje,
                                title = "Cancelar viaje",
                                message = "Si cancelas este viaje, quedará anulado y el conductor será notificado.",
                                confirmText = "Sí, cancelar",
                                dismissText = "Seguir",
                                onConfirm = {
                                    showDialogCancelarViaje = false
                                    estadoSolicitud = Rechazada("Cancelado por el usuario")
                                    // De momento solo cambiamos el estado local.
                                    // Cuando tengas claro cómo guardar el viaje confirmado en Firestore,
                                    // aquí podrás llamar a un método del ViewModel.
                                },
                                onDismiss = { showDialogCancelarViaje = false }
                            )
                        }
                        is Rechazada -> { }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón modo conductor
                Button(
                    onClick = {
                        if (uiState.isConductorSelected) {
                            navController.navigate(Rutas.ViewConductor)
                        } else if (!isEdadValida(uiState.edad)) {
                            Toast.makeText(context,"No puedes ser conductor. Eres menor de edad",Toast.LENGTH_SHORT).show()
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
 * Solo funcionará en esta clase
 */
sealed interface EstadoSolicitud {
    data object Pendiente : EstadoSolicitud
    data class OfertaConductor(val peticion: Peticion) : EstadoSolicitud
    data class Confirmada(val viaje: ViajeUi) : EstadoSolicitud
    data class Rechazada(val motivo: String) : EstadoSolicitud
}