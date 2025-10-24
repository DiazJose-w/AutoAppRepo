package com.proyecto.autoapp.inicio.registro.viewsRegistro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoNombreApl(nombre: String, apellidos: String, onNombreChange: (String) -> Unit, onApellidosChange: (String) -> Unit,
                  onNext: () -> Unit, onBack: () -> Unit){
    var context = LocalContext.current

    var error by rememberSaveable { mutableStateOf<String?>(null) }

    Column(Modifier.padding(16.dp)) {
        TitulosRegistro("Nombre y apellidos")
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
            ThumbUpPrimaryButton(
                text = "Siguiente",
                enabled = nombre.isNotBlank() && apellidos.isNotBlank(),
                onClick = {
                    error = null // Limpia error anterior
                    onNext()
                },
                modifier = Modifier
            )
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}