package com.proyecto.autoapp.inicio.registro.ventanas

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.proyecto.autoapp.general.Rutas
import com.proyecto.autoapp.general.TopBarGeneral
import com.proyecto.autoapp.inicio.login.Login
import com.proyecto.autoapp.inicio.login.LoginVM
import com.proyecto.autoapp.inicio.registro.RegistroVM

@Composable
fun Registro(navController: NavController ,registroVM: RegistroVM, loginVM: LoginVM) {
    var context = LocalContext.current

    val isLoading by registroVM.isLoading.collectAsState()

    // Variables de registro
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var edad by rememberSaveable { mutableStateOf("") }
    var confirmEmail by rememberSaveable { mutableStateOf("") }
    var tokenServidor by rememberSaveable { mutableStateOf<String?>(null) }

    var cont by rememberSaveable { mutableIntStateOf(1) }

    Scaffold (
        topBar = {
            TopBarGeneral(
                "Registro",
                onAccion = {
                    when (it) {
                        1 -> {
                            navController.popBackStack()
                            loginVM.signOut(context)
                            navController.navigate(Rutas.ViewInicial){
                                popUpTo(Rutas.Login){ inclusive = true }
                            }
                            // CUANDO HAGA LA PÁGINA PRINCIPAL Y TENGA EL ESTILO CREADO, BORRAR ESTA TOPBAR
                        }
                    }
                }
            )
        }
    )
    { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center

        ){
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
                    edad = edad,
                    onEdadChange = { edad = it },
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
                        SI EL TOKEN COINCIDE, PASAR A LA SIGUIENTE PÁGINA QUE SERÍA LA PÁGINA DE INICIO DE LA APP
                         * */
                    },
                    onVerifyToken = {
                        it -> tokenServidor != null && tokenServidor == it
                    },
                    onFinish = { cont = 4 },
                    onBack = { cont = 2 }
                )
            }

            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}

/**
 * Funciones:
 * - Titulo para cada campo del registro
 * - Expresión regular para el email
 * - Comprobación de campos de registro
 * - Método comprobar edad del usuario
 * - Expresión regular para seguridad de contraseña
 * - Función para formato E164
 * */
@Composable
fun TitulosRegistro(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

// Composable para la imagen según paso del registro
fun imagenRegistro(){
    /**     FALTA POR IMPLEMENTAR. BUSCAR IMÁGENES PARA ELLO     */
}

// Función para castear el número de teléfono
fun formatE164(phoneRaw: String, defaultRegion: String = "ES"): String? {
    val util = PhoneNumberUtil.getInstance()

    return try {
        val proto = util.parse(phoneRaw, defaultRegion)
        if (util.isValidNumber(proto)) util.format(proto, PhoneNumberFormat.E164)
        else
            null
    } catch (_: NumberParseException) {
        null
    }
}

// Expresión para comprobar si el formato del email es correcto
fun validarEmail(email: String): Boolean {
    val emailPermitido = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
    return email.matches(emailPermitido.toRegex())
}

// Función para comprobar que el usuario cumple con la edad mínima
fun edadUsuario(edad: Int): Boolean{
    var flag = true

    if (edad < 16 || edad > 80){
        flag = false
    }
    return flag
}