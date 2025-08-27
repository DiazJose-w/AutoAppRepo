package com.proyecto.autoapp.inicio.registro.ventanas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PasoEmail(
    email: String,
    onEmailChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var error by remember { mutableStateOf<String?>(null) }
    val regexEmail = remember { Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ){
        Column(Modifier.padding(16.dp)) {
            TitulosRegistro("Email")
            OutlinedTextField(
                value = email,
                onValueChange = { onEmailChange(it); error = null },
                label = { Text("Correo electrónico") },
                singleLine = true,
                isError = error != null
            )

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

                TextButton(onClick = onBack) { Text("Atrás") }

                Button(onClick = {
                    error = when {
                        email.isBlank() -> "El email es obligatorio"
                        !regexEmail.matches(email) -> "Formato de email no válido"
                        else -> null
                    }
                    if (error == null) onNext()
                })
                { Text("Siguiente") }
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}