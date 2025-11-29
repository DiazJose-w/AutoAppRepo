package com.proyecto.autoapp.viewUsuario.conductor

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.autoapp.general.Rutas
import com.proyecto.autoapp.general.maps.MapScreen
import com.proyecto.autoapp.general.maps.viewModels.MapViewModel
import com.proyecto.autoapp.general.modelo.enumClass.AccionDialogo
import com.proyecto.autoapp.general.modelo.peticiones.Peticion
import com.proyecto.autoapp.viewUsuario.peticiones.PeticionesVM
import com.proyecto.autoapp.inicio.login.LoginVM
import com.proyecto.autoapp.ui.theme.*
import com.proyecto.autoapp.viewUsuario.PerfilMenu
import com.proyecto.autoapp.viewUsuario.perfilVM.PerfilVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewConductor(mapViewModel: MapViewModel, navController: NavHostController, loginVM: LoginVM, perfilVM: PerfilVM, peticionesVM: PeticionesVM) {
    val TAG = "jose"
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    val usuarioActual = loginVM.uidActual
    var fotoPerfil by remember { mutableStateOf<String?>(null) }
    val uiState by perfilVM.uiState.collectAsState()

    val peticionesPendientes = peticionesVM.peticionesPendientes
    val posicionViajero by peticionesVM.posicionViajero.collectAsState()

    var showDialogAccion by remember { mutableStateOf(false) }
    var peticionSeleccionada by remember { mutableStateOf<Peticion?>(null) }
    var accionDialogo by remember { mutableStateOf<AccionDialogo?>(null) }

    // Launcher para poder escuchar las peticiones
    LaunchedEffect(Unit) {
        peticionesVM.observarPeticionesPendientesAceptadas(usuarioActual)
    }

    // Si ya no tengo ningún viaje aceptado, dejo de escuchar tracking
    LaunchedEffect(peticionesPendientes, usuarioActual) {
        val hayAceptadaParaMi = peticionesPendientes.any { pet ->
            pet.estado == "aceptada" && pet.infoConductor?.uid == usuarioActual
        }
        if (!hayAceptadaParaMi) {
            peticionesVM.detenerTracking()
        }
    }

    // Cargar foto de perfil
    LaunchedEffect(usuarioActual) {
        if (usuarioActual.isNotBlank()) {
            perfilVM.cargarFotoPerfil(usuarioActual) {
                fotoPerfil = it
            }
        } else {
            Log.e(TAG, "Hubo un problema al cargar el usuario actual")
        }
    }

    // Diálogo reutilizable de cerrar sesión
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
        modifier = Modifier.fillMaxSize(),
        containerColor = ThumbUpPurple,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Modo conductor",
                        color = ThumbUpTextPrimary,
                        fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                    )
                },
                navigationIcon = { },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ThumbUpPurple
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(ThumbUpPurple)
        ) {
            // ICONO MENSAJERÍA
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
                        onClick = { /* navController.navigate(Rutas.Mensajeria) */ },
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
                        Icon(
                            Icons.Filled.ChatBubble,
                            contentDescription = "Mensajes"
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.Top
            ) {
                // MENÚ PERFIL ARRIBA IZQUIERDA
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    PerfilMenu(
                        fotoPerfil = fotoPerfil,
                        onPerfil = { navController.navigate(Rutas.Perfil) },
                        onHistorial = { },
                        onFavoritos = { },
                        onConfiguracion = { },
                        onLogout = { showDialog = true }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // CONTENIDO SCROLLEABLE (mapa + lista)
                val configuration = LocalConfiguration.current
                val screenHeight = configuration.screenHeightDp.dp
                val mapHeight = screenHeight * 0.50F    // un poco más de la mitad

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // MAPA
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(mapHeight)
                            .shadow(12.dp, RoundedCornerShape(20.dp))
                            .border(
                                2.dp,
                                ThumbUpMustard,
                                RoundedCornerShape(20.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = ThumbUpMustard)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            MapScreen(mapViewModel, false, peticionesVM)
                        }
                    }

                    // LISTA DE PETICIONES
                    if (peticionesPendientes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay viajeros en el camino",
                                color = ThumbUpTextPrimary.copy(alpha = 0.55f),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Peticiones cercanas",
                                color = ThumbUpTextPrimary,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )

                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(peticionesPendientes) { index, peticion ->
                                    val esAceptadaParaMi =
                                        peticion.estado == "aceptada" && peticion.infoConductor?.uid == usuarioActual

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                            .shadow(8.dp, RoundedCornerShape(16.dp))
                                            .border(
                                                1.dp,
                                                ThumbUpMustard,
                                                RoundedCornerShape(16.dp)
                                            ),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF1A1A1A)
                                        ),
                                        elevation = CardDefaults.cardElevation(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    horizontal = 16.dp,
                                                    vertical = 14.dp
                                                ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "Viajero ${index + 1}",
                                                    color = ThumbUpTextPrimary,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                            }

                                            if (esAceptadaParaMi) {
                                                Button(
                                                    onClick = {
                                                        val posTracking = posicionViajero
                                                        val posInicio: LatLng? =
                                                            if (peticion.inicio.lat != null &&
                                                                peticion.inicio.lng != null
                                                            ) {
                                                                LatLng(
                                                                    peticion.inicio.lat,
                                                                    peticion.inicio.lng
                                                                )
                                                            } else null

                                                        val destino = posTracking ?: posInicio

                                                        if (destino != null) {
                                                            mapViewModel.updateCameraPosition(
                                                                destino,
                                                                15f
                                                            )
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                "Aún no hay ubicación del viajero",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = ThumbUpMustard,
                                                        contentColor = ThumbUpSurfaceDark
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        text = "Ir a por el viajero",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    )
                                                }
                                            } else {
                                                OutlinedButton(
                                                    onClick = {
                                                        peticionSeleccionada = peticion
                                                        accionDialogo = AccionDialogo.RECHAZAR
                                                        showDialogAccion = true
                                                    },
                                                    border = BorderStroke(
                                                        1.dp,
                                                        ThumbUpMustard
                                                    ),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        containerColor = Color.Transparent,
                                                        contentColor = ThumbUpMustard
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.padding(start = 12.dp)
                                                ) {
                                                    Text(
                                                        text = "Rechazar",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        peticionSeleccionada = peticion
                                                        accionDialogo = AccionDialogo.ACEPTAR
                                                        showDialogAccion = true
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = ThumbUpMustard,
                                                        contentColor = ThumbUpSurfaceDark
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.padding(start = 8.dp)
                                                ) {
                                                    Text(
                                                        text = "Aceptar",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    ThumbUpAceptarRechazarViaje(
                                        visible = showDialogAccion,
                                        title = when (accionDialogo) {
                                            AccionDialogo.ACEPTAR -> "Aceptar petición"
                                            AccionDialogo.RECHAZAR -> "Rechazar petición"
                                            else -> ""
                                        },
                                        message = when (accionDialogo) {
                                            AccionDialogo.ACEPTAR ->
                                                "¿Quieres ofrecerte para llevar a este viajero?"
                                            AccionDialogo.RECHAZAR ->
                                                "¿Seguro que quieres rechazar a este viajero?"
                                            else -> ""
                                        },
                                        confirmText = when (accionDialogo) {
                                            AccionDialogo.ACEPTAR -> "Sí, aceptar"
                                            AccionDialogo.RECHAZAR -> "Sí, rechazar"
                                            else -> ""
                                        },
                                        dismissText = "Cancelar",
                                        onConfirm = {
                                            val pet = peticionSeleccionada
                                            val accion = accionDialogo

                                            if (pet != null && accion != null) {
                                                when (accion) {
                                                    AccionDialogo.ACEPTAR -> {
                                                        val nombreConductor =
                                                            listOf(
                                                                uiState.nombre,
                                                                uiState.apellidos
                                                            )
                                                                .filter { it.isNotBlank() }
                                                                .joinToString(" ")

                                                        peticionesVM.aceptarPeticionConductor(
                                                            pet,
                                                            usuarioActual.toString(),
                                                            nombreConductor,
                                                            uiState.fotoPerfilUrl.toString()
                                                        ) { ok ->
                                                            if (ok) {
                                                                peticionesVM.observarTrackingPeticion(
                                                                    pet.id
                                                                )
                                                                Toast.makeText(
                                                                    context,
                                                                    "Petición aceptada",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            } else {
                                                                Toast.makeText(
                                                                    context,
                                                                    "La petición ya fue atendida",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                    }

                                                    AccionDialogo.RECHAZAR -> {
                                                        peticionesVM.rechazarPeticionConductor(
                                                            pet,
                                                            usuarioActual
                                                        ) { ok ->
                                                            if (ok) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Error al rechazar petición",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            showDialogAccion = false
                                            accionDialogo = null
                                            peticionSeleccionada = null
                                        },
                                        onDismiss = {
                                            showDialogAccion = false
                                            accionDialogo = null
                                            peticionSeleccionada = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // BOTÓN FIJO ABAJO (fuera del scroll)
                Button(
                    onClick = {
                        navController.navigate(Rutas.ViewViajero) {
                            popUpTo(Rutas.ViewConductor) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThumbUpMustard,
                        contentColor = ThumbUpTextPrimary
                    )
                ) {
                    Text(
                        text = "Modo viajero",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
