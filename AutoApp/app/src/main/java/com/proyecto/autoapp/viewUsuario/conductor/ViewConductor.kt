package com.proyecto.autoapp.viewUsuario.conductor

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.autoapp.general.Rutas
import com.proyecto.autoapp.general.maps.MapScreen
import com.proyecto.autoapp.general.maps.MapViewModel
import com.proyecto.autoapp.inicio.login.LoginVM
import com.proyecto.autoapp.ui.theme.*
import com.proyecto.autoapp.viewUsuario.PerfilMenu
import com.proyecto.autoapp.viewUsuario.perfilVM.PerfilVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewConductor(mapViewModel: MapViewModel, navController: NavHostController, loginVM: LoginVM, perfilVM: PerfilVM) {
    val TAG = "jose"
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    // UID del usuario actual
    val usuarioActual = loginVM.uidActual
    var fotoPerfil by remember { mutableStateOf<String?>(null) }

    val peticionesPendientes = mapViewModel.peticionesPendientes

    // Launcher para poder escuchar las peticiones
    LaunchedEffect(Unit) {
        mapViewModel.observarPeticionesPendientes(usuarioActual)
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

        // 🔹 CONTENEDOR PRINCIPAL, IGUAL QUE EN ViewInicialUsuario
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(ThumbUpPurple)
        ) {
            // ICONO DE ACCESO A MENSAJERÍA CON NOTIFICACIONES DE NÚMERO DE MENSAJES POR LEER
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // MENÚ DE PERFIL ARRIBA A LA IZQUIERDA
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    PerfilMenu(
                        fotoPerfil = fotoPerfil,
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

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                        MapScreen(mapViewModel)
                    }
                }

                // Lista de peticiones pendientes
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
                }
                else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Viajeros solicitantes...",
                            color = ThumbUpTextPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(peticionesPendientes) { cont, _ ->
                                val etiquetaViajero = "Viajero ${cont + 1}"
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(8.dp, RoundedCornerShape(14.dp)),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = CardDefaults.cardElevation(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF1A1A1A)
                                    ),
                                    border = BorderStroke(1.dp, ThumbUpMustard.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = etiquetaViajero,
                                                color = ThumbUpTextPrimary,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                            Text(
                                                text = "Solicitud pendiente",
                                                color = ThumbUpTextPrimary.copy(alpha = 0.7f),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }

                                        // Aquí más adelante irán los botones Aceptar / Rechazar
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        navController.navigate(Rutas.ViewUsuario) {
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


