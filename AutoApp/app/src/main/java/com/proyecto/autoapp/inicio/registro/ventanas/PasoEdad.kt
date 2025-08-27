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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PasoEdad(
    edad: String,
    onEdadChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = edad,
            onValueChange = {
                if (it.all { c -> c.isDigit() }) onEdadChange(it)
                error = null
            },
            label = { Text("Edad") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = error != null
        )

        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Atrás") }
            Button(
                onClick = {
                    error = when {
                        edad.isBlank() -> "La edad es obligatoria"
                        edad.toIntOrNull() == null -> "Introduce una edad válida"
                        !edadUsuario(edad.toInt()) -> "Edad no válida"
                        else -> null
                    }
                    if (error == null) onNext()
                }
            )
            { Text("Siguiente") }
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
