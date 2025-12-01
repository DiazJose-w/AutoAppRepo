package com.proyecto.autoapp.inicio.registro.viewsRegistro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.proyecto.autoapp.ui.theme.ThumbsUpPrimaryButton
import com.proyecto.autoapp.ui.theme.ThumbsUpTextFieldColors
import com.proyecto.autoapp.ui.theme.TitulosRegistro

@Composable
fun PasoPassword(password: String, onPasswordChange: (String) -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
    var show by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showConfirm by remember { mutableStateOf(false) }
    var pass by rememberSaveable { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        TitulosRegistro("Contraseña")
        OutlinedTextField(
            value = password,
            onValueChange = { onPasswordChange(it); error = null },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { show = !show }) {
//                    Icon(
//                        imageVector = if (show) Icons.Default.VisibilityOff else Icons.Default.Visibility,
//                        contentDescription = null
//                    )
                }
            },
            supportingText = { Text("Mínimo 8 caracteres, 1 número") },
            isError = error != null,
            colors = ThumbsUpTextFieldColors()
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it; error = null },
            label = { Text("Confirmar contraseña") },
            singleLine = true,
            visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
//                IconButton(onClick = { showConfirm = !showConfirm }) {
//
//                }
            },
            supportingText = { Text("Vuelve a escribir la contraseña") },
            isError = error != null,
            colors = ThumbsUpTextFieldColors()
        )

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Atrás") }
            ThumbsUpPrimaryButton(
                text = "Siguiente",
                enabled = pass.isNotBlank() && password.isNotBlank(),
                onClick = {
                        error = when{
                            password.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
                            !password.any { it.isDigit() } -> "Incluye al menos un número"
                            password != pass -> "Deben coincidir las contraseñas"
                            else -> null
                        }
                        if (error == null) onNext()
                },
                modifier = Modifier
            )
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
