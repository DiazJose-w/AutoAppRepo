package com.proyecto.autoapp.inicio.registro.viewsRegistro

import com.proyecto.autoapp.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.proyecto.autoapp.general.TopBarGeneral
import com.proyecto.autoapp.inicio.login.LoginVM
import com.proyecto.autoapp.inicio.registro.RegistroVM

private val ThumbUpPurple = Color(0xFF1D0F2E)
private val ThumbUpMustard = Color(0xFFF1C232)

@Composable
fun Registro(navController: NavController, registroVM: RegistroVM, loginVM: LoginVM) {
    val context = LocalContext.current
    val isLoading by registroVM.isLoading.collectAsState()

    // Variables de registro
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var edad by rememberSaveable { mutableStateOf("") }
    var confirmEmail by rememberSaveable { mutableStateOf("") }
    var tokenServidor by rememberSaveable { mutableStateOf<String?>(null) }

    // Control de pasos
    var cont by rememberSaveable { mutableIntStateOf(1) }

    Scaffold(
        topBar = {

        },
        containerColor = ThumbUpPurple
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(ThumbUpPurple),
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
                    color = ThumbUpMustard,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 90.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x1AFFFFFF)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                     *  SI EL TOKEN COINCIDE, PASAR A LA SIGUIENTE PÁGINA QUE SERÍA LA PÁGINA DE INICIO DE LA APP
                                     */
                                },
                                onVerifyToken = { introducido ->
                                    tokenServidor != null && tokenServidor == introducido
                                },
                                onFinish = {
                                    // Mantienes tu lógica (de momento no cambias el paso)
                                    cont = 4
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
                    color = ThumbUpMustard
                )
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
 */
@Composable
fun TitulosRegistro(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        color = ThumbUpMustard,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

/* =========================
   Helpers visuales ThumbUp
   ========================= */

@Composable
fun ThumbUpPrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit, modifier: Modifier = Modifier ) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ThumbUpMustard,
            contentColor = Color(0xFF1A1A1A),
            disabledContainerColor = ThumbUpMustard.copy(alpha = 0.5f),
            disabledContentColor = Color(0xFF1A1A1A).copy(alpha = 0.7f)
        )
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
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
