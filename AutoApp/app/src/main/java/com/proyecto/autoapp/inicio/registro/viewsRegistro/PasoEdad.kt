import android.app.DatePickerDialog
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.proyecto.autoapp.ui.theme.ThumbsUpMustard
import com.proyecto.autoapp.ui.theme.ThumbsUpPrimaryButton
import com.proyecto.autoapp.ui.theme.ThumbsUpTextFieldColors
import com.proyecto.autoapp.ui.theme.TitulosRegistro
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoEdad(
    fechaNacimiento: Long?,
    onFechaNacimientoSeleccionada: (Long) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var error by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = fechaNacimiento
    )

    val fechaNacimientoTexto = remember(fechaNacimiento) {
        if (fechaNacimiento == null) {
            ""
        } else {
            val cal = Calendar.getInstance().apply {
                timeInMillis = fechaNacimiento
            }

            val dia = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
            val mes = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
            val anio = cal.get(Calendar.YEAR).toString()

            "$dia/$mes/$anio"
        }
    }

    // ---------- DIALOGO DE FECHA (COMPOSABLE) ----------
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val seleccion = datePickerState.selectedDateMillis
                        if (seleccion != null) {
                            onFechaNacimientoSeleccionada(seleccion)
                            error = null
                            showDatePicker = false
                        } else {
                            error = "Debes seleccionar una fecha"
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
    // ---------------------------------------------------

    Column(Modifier.padding(16.dp)) {
        TitulosRegistro("Edad")

        OutlinedTextField(
            value = fechaNacimientoTexto,
            onValueChange = { },
            label = { Text("Fecha de nacimiento") },
            singleLine = true,
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            colors = ThumbsUpTextFieldColors(),   // usa tu función de colores
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Seleccionar fecha",
                        tint = ThumbsUpMustard
                    )
                }
            }
        )

        Spacer(Modifier.height(24.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) { Text("Atrás") }
            ThumbsUpPrimaryButton(
                text = "Siguiente",
                enabled = fechaNacimiento != null,
                onClick = {
                    if (fechaNacimiento == null) {
                        error = "Debes seleccionar tu fecha de nacimiento"
                    } else {
                        error = null
                        onNext()
                    }
                },
                modifier = Modifier
            )
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

