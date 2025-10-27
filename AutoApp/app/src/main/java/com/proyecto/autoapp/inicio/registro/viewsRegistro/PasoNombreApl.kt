package com.proyecto.autoapp.inicio.registro.viewsRegistro

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.saveable.rememberSaveable
import com.proyecto.autoapp.ui.theme.*
import com.proyecto.autoapp.ui.theme.TitulosRegistro


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoNombreApl(nombre: String, apellidos: String, onNombreChange: (String) -> Unit, onApellidosChange: (String) -> Unit,
                  onNext: () -> Unit, onBack: () -> Unit){
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    Column(Modifier.padding(16.dp)) {
        TitulosRegistro("Nombre y apellidos")

        OutlinedTextField(
            value = nombre,
            onValueChange = { onNombreChange(it); error = null },
            label = { Text("Nombre") },
            singleLine = true,
            isError = error != null && nombre.isBlank(),
            textStyle = LocalTextStyle.current.copy(
                color = ThumbUpTextPrimary
            ),
            colors = ThumbUpTextFieldColors()
        )




        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = apellidos,
            onValueChange = { onApellidosChange(it); error = null },
            label = { Text("Apellidos") },
            singleLine = true,
            isError = error != null && apellidos.isBlank(),
            colors = ThumbUpTextFieldColors()
        )

        Spacer(Modifier.height(24.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) { Text("Cancelar") }
            ThumbUpPrimaryButton(
                text = "Siguiente",
                enabled = nombre.isNotBlank() && apellidos.isNotBlank(),
                onClick = {
                    error = null
                    onNext()
                }
            )
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }

}