package com.proyecto.autoapp.inicio.registro.ventanas

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.proyecto.autoapp.general.TopBarGeneral
import com.proyecto.autoapp.inicio.registro.RegistroVM


@Composable
fun TokenSMS(navController: NavController, registroVM: RegistroVM){
    var context = LocalContext.current

    // Estados para teléfono y código
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    val isLoading by registroVM.isLoading.collectAsState()
    val errorMsg by registroVM.errorMessage.collectAsState()
    val codeSent by registroVM.codeSent.collectAsState()       // <- ver ajuste del VM abajo
    val loginOk by registroVM.loginSuccess.collectAsState()

    // Feedback
    LaunchedEffect(errorMsg) { errorMsg?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() } }
    LaunchedEffect(codeSent) { if (codeSent) Toast.makeText(context, "SMS enviado", Toast.LENGTH_SHORT).show() }
    LaunchedEffect(loginOk) {
        if (loginOk) {
            Toast.makeText(context, "Inicio de sesión correcto", Toast.LENGTH_LONG).show()
            navController.navigate("home") { popUpTo("login") { inclusive = true } }
        }
    }

    Scaffold (
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
    )
    { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center

        ){
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally

            ){
                // Teléfono (E.164)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.trim() },
                    label = { Text("Teléfono (+34...)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Código (OTP de 6 dígitos)
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.filter(Char::isDigit).take(6) },
                    label = { Text("Código (SMS)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Enviar SMS
                Button(
                    onClick = {
                        val e164 = formatE164(phone, defaultRegion = "ES")
                        if (e164 == null) {
                            Toast.makeText(context, "Teléfono inválido", Toast.LENGTH_SHORT).show()
                        } else {
                            registroVM.startPhoneVerification(context as Activity, e164) // ← ya en E.164
                        }
                    },
                    enabled = phone.isNotBlank() && !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303F9F))
                ) {
                    Text(if (isLoading) "Enviando…" else "Solicitar código")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Verificar código
                Button(
                    onClick = { registroVM.verifyCode(code) },
                    enabled = code.length == 6 && !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF303F9F)
                    )
                ) {
                    Text("Verificar código")
                }

                if (isLoading) {
                    Dialog(onDismissRequest = { }) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.White, shape = RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Procesando…")
                        }
                    }
                }
            }
        }
    }
}