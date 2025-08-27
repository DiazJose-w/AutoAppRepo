package com.proyecto.autoapp.inicio.registro.ventanas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PasoEmail(email: String, onEmailChange: (String) -> Unit, confirmEmail: String, onConfirmEmailChange: (String) -> Unit,
    onRequestToken: (String) -> Unit, onVerifyToken: (String) -> Boolean, onNext: () -> Unit, onBack: () -> Unit) {
    var errorEmail by remember { mutableStateOf<String?>(null) }
    var errorConfirm by remember { mutableStateOf<String?>(null) }

    var showTokenField by rememberSaveable { mutableStateOf(false) }
    var token by rememberSaveable { mutableStateOf("") }
    var errorToken by remember { mutableStateOf<String?>(null) }
    var tokenRequested by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            TitulosRegistro("Email")

            OutlinedTextField(
                value = email,
                onValueChange = {
                    onEmailChange(it)
                    errorEmail = null
                    if (errorConfirm != null && confirmEmail == it) errorConfirm = null
                    // Si el usuario cambia el email tras haber pedido token, reseteamos token/visibilidad
                    if (showTokenField) {
                        showTokenField = false
                        tokenRequested = false
                        token = ""
                        errorToken = null
                    }
                },
                label = { Text("Correo electrónico") },
                singleLine = true,
                isError = errorEmail != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmEmail,
                onValueChange = {
                    onConfirmEmailChange(it)
                    errorConfirm = null
                    // Si cambia la confirmación y ya se mostró el token, reseteamos
                    if (showTokenField) {
                        showTokenField = false
                        tokenRequested = false
                        token = ""
                        errorToken = null
                    }
                },
                label = { Text("Confirmar correo") },
                singleLine = true,
                isError = errorConfirm != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            errorEmail?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            errorConfirm?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            if (showTokenField) {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = token,
                    onValueChange = {
                        token = it
                        errorToken = null
                    },
                    label = { Text("Código de verificación") },
                    singleLine = true,
                    isError = errorToken != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                errorToken?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Hemos enviado un código a $email. Revísalo y escríbelo aquí.",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onRequestToken(email) }) {
                        Text("Reenviar código")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBack) { Text("Atrás") }

                Button(onClick = {
                    // 1) Validar emails
                    errorEmail = when {
                        email.isBlank() -> "El email es obligatorio"
                        !validarEmail(email) -> "Formato no válido"
                        else -> null
                    }
                    errorConfirm = when {
                        confirmEmail.isBlank() -> "Confirma tu email"
                        confirmEmail != email -> "Los correos no coinciden"
                        else -> null
                    }

                    if (errorEmail == null && errorConfirm == null) {
                        if (!showTokenField) {
                            // 2) Emails OK -> mostrar campo token y enviar código
                            showTokenField = true
                            token = ""
                            errorToken = null
                            if (!tokenRequested) {
                                onRequestToken(email)
                                tokenRequested = true
                            }
                        } else {
                            // 3) Ya visible: verificar token
                            errorToken = when {
                                token.isBlank() -> "Introduce el código de verificación"
                                !onVerifyToken(token) -> "Código incorrecto"
                                else -> null
                            }
                            if (errorToken == null) onNext()
                        }
                    }
                }) {
                    Text(if (showTokenField) "Verificar y continuar" else "Solicitar token")
                }
            }
        }
    }
}
