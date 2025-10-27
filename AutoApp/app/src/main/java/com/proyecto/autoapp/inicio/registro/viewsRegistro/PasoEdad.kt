package com.proyecto.autoapp.inicio.registro.viewsRegistro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.proyecto.autoapp.ui.theme.ThumbUpPrimaryButton
import com.proyecto.autoapp.ui.theme.ThumbUpTextFieldColors
import com.proyecto.autoapp.ui.theme.TitulosRegistro

@Composable
fun PasoEdad(edad: String, onEdadChange: (String) -> Unit, onNext: () -> Unit, onBack: () -> Unit) {
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.padding(16.dp)) {
        TitulosRegistro("Edad")
        OutlinedTextField(
            value = edad,
            onValueChange = {
                if (it.all { c -> c.isDigit() }) onEdadChange(it)
                error = null
            },
            label = { Text("Edad") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = error != null,
            colors = ThumbUpTextFieldColors()
        )

        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Atrás") }
            ThumbUpPrimaryButton(
                text = "Siguiente",
                enabled = edad.isNotBlank(),
                onClick = {
                    error = null
                    onNext()
                },
                modifier = Modifier
            )
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
