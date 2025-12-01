package com.proyecto.autoapp.inicio.registro.viewsRegistro

import PasoEdad
import android.util.Log
import android.widget.Toast
import com.proyecto.autoapp.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.proyecto.autoapp.general.Rutas
import com.proyecto.autoapp.inicio.login.LoginVM
import com.proyecto.autoapp.inicio.registro.RegistroVM
import com.proyecto.autoapp.ui.theme.ThumbsUpCard
import com.proyecto.autoapp.ui.theme.ThumbsUpMustard
import com.proyecto.autoapp.ui.theme.ThumbsUpPurple
import java.util.Calendar

@Composable
fun Registro(navController: NavController, registroVM: RegistroVM, loginVM: LoginVM) {
    var TAG = "Jose"
    val context = LocalContext.current
    val isLoading by registroVM.isLoading.collectAsState()

    // Variables de registro
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var edad by rememberSaveable { mutableStateOf("") }
    var fechaNacimiento by rememberSaveable { mutableStateOf<Long?>(null) }
    var confirmEmail by rememberSaveable { mutableStateOf("") }
    var tokenServidor by rememberSaveable { mutableStateOf<String?>(null) }

    // Control de pasos
    var cont by rememberSaveable { mutableIntStateOf(1) }

    Scaffold(
        containerColor = ThumbsUpPurple
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(ThumbsUpPurple),
            contentAlignment = Alignment.Center
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Crea tu cuenta",
                    color = ThumbsUpMustard,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ThumbsUpCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (cont) {
                            1 -> PasoNombreApl(
                                nombre = nombre,
                                apellidos = apellidos,
                                onNombreChange = { nombre = it },
                                onApellidosChange = { apellidos = it },
                                onNext = { cont = 2 },
                                onBack = { navController.popBackStack() }
                            )

                            2 -> PasoEdad(
                                fechaNacimiento = fechaNacimiento,
                                onFechaNacimientoSeleccionada = { fechaNacimiento = it },
                                onNext = { cont = 3 },
                                onBack = { cont = 1 }
                            )

                            3 -> PasoPassword(
                                password = password,
                                onPasswordChange = { password = it },
                                onBack = { cont = 2 },
                                onNext = { cont = 4 }
                            )

                            4 -> PasoEmail(
                                email = email,
                                onEmailChange = { email = it },
                                confirmEmail = confirmEmail,
                                onConfirmEmailChange = { confirmEmail = it },
                                onRequestToken = { correo ->
                                    // Aquí implementar mi propio método el cual va a incluir letras may y min y números
                                    val nuevoToken = (100000..999999).random().toString()
                                    tokenServidor = nuevoToken

                                    // 2) Envíalo por correo (tu implementación real aquí)
                                    // registroVM.enviarTokenEmail(correo, nuevoToken)
                                    /** EN ESTE PASO ES DONDE DEBO ENVIAR EL TOKEN DE VERIFICACIÓN
                                     *  SI EL TOKEN COINCIDE, PASAR A LA SIGUIENTE PÁGINA.
                                     *  ESA PÁGINA ES LA VIEW DEL PERFIL DE LA PERSONA USUARIA PARA QUE CONTINÚE SI QUIERE VERIFICANDO
                                     *  SU IDENTIDAD COMO PERSONA USUARIA
                                     */
                                },
                                onVerifyToken = { introducido ->
                                    tokenServidor != null && tokenServidor == introducido
                                },
                                onFinish = { ok ->
                                    cont = 4
                                    if(ok){
                                        registroVM.registroWhitEmail(nombre, apellidos, fechaNacimiento, password, email,
                                            { exito ->
                                                if (exito) {
                                                    navController.navigate(Rutas.Perfil)
                                                } else {
                                                    Toast.makeText(context, "Error en el registro", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) { uid ->
                                            loginVM.uidActual = uid
                                            Log.e(TAG, "Usuario registrado. UID => $uid")
                                        }
                                    }else{
                                        Toast.makeText(context, "Algo ocurre", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onBack = { cont = 2 }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Indica el paso del registro
                Text(
                    text = "Paso $cont de 4",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Loading centrado
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = ThumbsUpMustard
                )
            }
        }
    }
}

private fun calcularEdadDesdeMillis(fechaNacimiento: Long): Int {
    val nacimiento = Calendar.getInstance().apply {
        timeInMillis = fechaNacimiento
    }
    val hoy = Calendar.getInstance()

    var edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)

    val mesHoy = hoy.get(Calendar.MONTH)
    val diaHoy = hoy.get(Calendar.DAY_OF_MONTH)
    val mesNac = nacimiento.get(Calendar.MONTH)
    val diaNac = nacimiento.get(Calendar.DAY_OF_MONTH)

    if (mesHoy < mesNac || (mesHoy == mesNac && diaHoy < diaNac)) {
        edad--
    }

    return edad
}
