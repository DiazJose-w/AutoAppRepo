package com.proyecto.autoapp.inicio.login.ViewsLogin

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.proyecto.autoapp.general.funcionesComunes.formatE164
import com.proyecto.autoapp.ui.theme.TopBarGeneral
import com.proyecto.autoapp.inicio.login.LoginVM
import com.proyecto.autoapp.ui.theme.*

@Composable
fun TokenSMS(navController: NavController, loginVM: LoginVM){
    var context = LocalContext.current

    // Estados para teléfono y código
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    val isLoading by loginVM.isLoading.collectAsState()
    val errorMsg by loginVM.errorMessage.collectAsState()
    val codeSent by loginVM.codeSent.collectAsState()       // <- ver ajuste del VM abajo
    val loginOk by loginVM.loginSuccess.collectAsState()

    /**
     * Disparadores de efectos relacionados con el proceso de autenticación por SMS:
     * - Muestra el error si lo hay.
     * - Notifica el envío del código SMS.
     * - Confirma el inicio de sesión y navega al home.
     */
    LaunchedEffect(errorMsg) { errorMsg?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() } }
    LaunchedEffect(codeSent) { if (codeSent) Toast.makeText(context, "SMS enviado", Toast.LENGTH_SHORT).show() }
    LaunchedEffect(loginOk) {
        if (loginOk) {
            Toast.makeText(context, "Inicio de sesión correcto", Toast.LENGTH_LONG).show()
            navController.navigate("home") { popUpTo("login") { inclusive = true } }
        }
    }
    /** --------------------------------------  */

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBarGeneral(
                "Registro",
                onAccion = {
                    when (it) {
                        1 -> {
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(ThumbUpPurple),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(com.proyecto.autoapp.R.mipmap.camino_central),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .alpha(0.15f),
                contentScale = ContentScale.FillWidth
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Teléfono (E.164)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.trim() },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = ThumbUpMustard,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 1f),
                        focusedLabelColor = ThumbUpMustard,
                        unfocusedLabelColor = Color.White.copy(alpha = 1f),
                        cursorColor = ThumbUpMustard,
                        focusedTextColor = Color(0xFF111111),
                        unfocusedTextColor = Color(0xFF111111)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Código (OTP de 6 dígitos)
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.filter(Char::isDigit).take(6) },
                    label = { Text("Código (SMS)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = ThumbUpMustard,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 1f),
                        focusedLabelColor = ThumbUpMustard,
                        unfocusedLabelColor = Color.White.copy(alpha = 1f),
                        cursorColor = ThumbUpMustard,
                        focusedTextColor = Color(0xFF111111),
                        unfocusedTextColor = Color(0xFF111111)
                    )
                )

                Spacer(modifier = Modifier.height(30.dp))
                var codeRequested by remember { mutableStateOf(false) }

                Button(
                    onClick = {
                        if (!codeRequested) {
                            val formato = formatE164(phone, defaultRegion = "ES")
                            if (formato == null) {
                                Toast.makeText(context, "Formáto incorrecto", Toast.LENGTH_SHORT).show()
                            } else {
                                loginVM.startPhoneVerification(context as Activity, formato)
                                codeRequested = true
                            }
                        } else {
                            if (code.length == 6) {
                                loginVM.verifyCode(code)
                            } else {
                                Toast.makeText(context, "Introduce el código de 6 dígitos", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isLoading && (
                            if (!codeRequested) phone.isNotBlank() else code.length == 6
                            ),
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .align(Alignment.CenterHorizontally)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThumbUpMustard,
                        contentColor = Color(0xFF1A1A1A),
                        disabledContainerColor = ThumbUpMustard.copy(alpha = 0.40f),
                        disabledContentColor = Color(0xFF1A1A1A).copy(alpha = 0.60f)
                    )
                ) {
                    val text = when {
                        isLoading -> "Procesando…"
                        !codeRequested -> "Solicitar código"
                        else -> "Verificar código"
                    }
                    Text(text, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Dialog(onDismissRequest = { }) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(Color.White, shape = RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ThumbUpMustard)
                        }
                    }
                }
            }
        }
    }
}