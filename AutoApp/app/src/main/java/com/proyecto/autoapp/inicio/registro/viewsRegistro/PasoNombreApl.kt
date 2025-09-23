package com.proyecto.autoapp.inicio.registro.viewsRegistro

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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoNombreApl(nombre: String, apellidos: String, onNombreChange: (String) -> Unit, onApellidosChange: (String) -> Unit,
                  onNext: () -> Unit, onBack: () -> Unit){
    var context = LocalContext.current

    var error by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ){
        Column(Modifier.padding(16.dp)) {

            OutlinedTextField(
                value = nombre,
                onValueChange = { onNombreChange(it); error = null },
                label = { Text("Nombre") },
                singleLine = true,
                isError = error != null && nombre.isBlank()
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = apellidos,
                onValueChange = { onApellidosChange(it); error = null },
                label = { Text("Apellidos") },
                singleLine = true,
                isError = error != null && apellidos.isBlank()
            )

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBack) { Text("Cancelar") }
                Button(onClick = {
                    if (nombre.isBlank() || apellidos.isBlank()) {
                        error = "Rellena nombre y apellidos"
                    } else onNext()
                })
                { Text("Siguiente") }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}