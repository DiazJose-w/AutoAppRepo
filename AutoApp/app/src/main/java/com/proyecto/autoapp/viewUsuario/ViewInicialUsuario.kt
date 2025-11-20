package com.proyecto.autoapp.viewUsuario

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
import com.proyecto.autoapp.inicio.login.LoginVM
import com.proyecto.autoapp.ui.theme.*
import com.proyecto.autoapp.viewUsuario.perfilVM.PerfilVM

@Composable
fun ViewInicialUsuario(mapViewModel: MapViewModel, loginVM: LoginVM, navController: NavController, perfilVM: PerfilVM) {
    var context = LocalContext.current
    var TAG = "jose"
    val uiState by perfilVM.uiState.collectAsState()

    var inicio by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var estadoSolicitud by remember { mutableStateOf<EstadoSolicitud>(EstadoSolicitud.Pendiente) }

    // UID del usuario actual
    val usuarioActual = loginVM.uidActual
    var fotoPerfilUrl by remember { mutableStateOf<String?>(null) }

    // Launcher que se asegura de que no haya errores si no se carga la imagen
    LaunchedEffect(usuarioActual) {
        if (usuarioActual.isNotBlank()) {
            perfilVM.cargarUsuario(usuarioActual)
        }else{
            Log.e(TAG, "Hubo un problema al cargar el usuario actual")
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    var mostrarDialogo by remember { mutableStateOf(false) }

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

            /**
             * Con este contenedor puedo mostrar las notificaciones de los mensajes pendientes
             * */
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BadgedBox(
                    badge = {
                        val not = 0
                        // Lo pongo >= a cero para comprobar que se muestra. Después quiera el =
                        if (not >= 0) Badge {
                            Text(not.coerceAtMost(99).toString())
                        }
                    }
                ) {
                    /**
                     * Es un botón flotante
                     * */
                    SmallFloatingActionButton(
                        onClick = {

                            //navController.navigate(Rutas.Mensajeria)
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
                        onHistorial = {
                            /**
                             * Crear ViewHistorial. Una lista con su historial de vaijes y su información
                             * */
                        },
                        onFavoritos = {
                            /**
                             * Crear ViewFavoritos. Una lista con sus perfiles favoritos
                             * */
                        },
                        onConfiguracion = {
                            /**
                             *  Crear ViewConfiguración. Donde podrá elegir el idioma, el tema de la app
                             * y más opciones (De momento pensar en cuales).
                             * */
                        },
                        onLogout = {
                            showDialog = true
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f)
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

                    /**
                     * Campos de texto Inicio/Destino
                     * */
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
                            modifier = Modifier
                                .fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = ThumbUpTextFieldColors()
                        )
                        if (sugerenciasInicio.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(),
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
                            modifier = Modifier
                                .fillMaxWidth(),
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

                /**
                 * Lanzar petición. Crear ViewModelSolicitudViajes
                 * */
                ThumbUpPrimaryButton(
                    text = "Realizar petición",
                    enabled = true,
                    onClick = {
                        /**
                         * Lanzar petición. Crear ViewModelSolicitudViajes
                         * */
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                )

                Spacer(Modifier.height(12.dp))

                /**
                 * A este apartado hay que darle una vuelta. Ver que partes van al conductor y cuales no.
                 * */
                when (estadoSolicitud) {
                    is EstadoSolicitud.Pendiente -> {
                        /**
                         * Esto solo se realizará desde la vista del conductor.
                         * */
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Esperando confirmación",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    is EstadoSolicitud.Rechazada -> {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Petición rechazada: ${(estadoSolicitud as EstadoSolicitud.Rechazada).motivo}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    is EstadoSolicitud.Confirmada -> {
                        PanelInfoViaje(
                            viaje = (estadoSolicitud as EstadoSolicitud.Confirmada).viaje,
                            onVerRuta = {
                                /**
                                 * Funcionalidad para el futuro.
                                 * */
                            },
                            onContactar = {
                                /**
                                 * Esta funcionalidad abre el chat del conductor.
                                 * */
                            },
                            onCancelar = {
                                estadoSolicitud =
                                    EstadoSolicitud.Rechazada("Cancelado por el usuario")
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if(uiState.isConductorSelected){
                            navController.navigate(Rutas.ViewConductor)
                        }else if(!isEdadValida(uiState.edad)){
                            Toast.makeText(context, "No puedes ser conductor. Eres menor de edad", Toast.LENGTH_SHORT).show()
                        }else{
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
 * Así evitamos las clases enumeradas. Es otra manera de poder crear subtipos. Solo funcionará en esta clase
 */
sealed interface EstadoSolicitud {
    data object Pendiente : EstadoSolicitud
    data class Confirmada(val viaje: ViajeUi) : EstadoSolicitud
    data class Rechazada(val motivo: String) : EstadoSolicitud
}
