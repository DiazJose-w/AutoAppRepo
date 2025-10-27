package com.proyecto.autoapp.viewUsuario.perfilVM

import androidx.navigation.NavController
import com.proyecto.autoapp.viewUsuario.PerfilUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class PerfilVM {

    // Estado observable por la UI
    private val _uiState = MutableStateFlow(
        PerfilUiState(
            nombre = "",
            apellidos = "",
            edad = "",
            email = "",
        )
    )
    val uiState: StateFlow<PerfilUiState> = _uiState

    // --------- EDITAR CAMPOS BÁSICOS ---------

    fun onNombreChange(nuevo: String) {
        _uiState.update { it.copy(nombre = nuevo, isSaveEnabled = true) }
    }

    fun onApellidosChange(nuevo: String) {
        _uiState.update { it.copy(apellidos = nuevo, isSaveEnabled = true) }
    }

    fun onEdadChange(nuevaEdad: String) {
        _uiState.update { curr ->
            val edadNum = nuevaEdad.toIntOrNull() ?: 0
            curr.copy(
                edad = nuevaEdad,
                showEdadWarningConductor = curr.isConductorSelected && edadNum < 18,
                isSaveEnabled = true
            )
        }
    }

    // --------- FOTOS ---------

    fun onCambiarFotoPerfil() {
        // Aquí lanzarás flujo de selección de imagen de perfil
        // _uiState.update { it.copy(fotoPerfilUrl = nuevaUrl, isSaveEnabled = true) }
    }

    fun onAbrirGaleria() {
        // Aquí podrías abrir una galería o lista de fotos adicionales
    }

    // --------- ROLES ---------

    fun onPasajeroToggle(checked: Boolean) {
        _uiState.update {
            it.copy(
                isPasajeroSelected = checked,
                isSaveEnabled = true
            )
        }
    }

    fun onConductorToggle(checked: Boolean) {
        _uiState.update { curr ->
            val edadNum = curr.edad.toIntOrNull() ?: 0
            curr.copy(
                isConductorSelected = checked,
                showEdadWarningConductor = checked && edadNum < 18,
                isSaveEnabled = true
            )
        }
    }

    // --------- GUARDAR ---------

    fun onGuardarCambios() {
        // Aquí puedes subir datos a Firestore o backend
        _uiState.update { it.copy(isSaveEnabled = false) }
    }

    // --------- ATRÁS ---------

    fun onBackPressed(navController: NavController) {
        // Si hay cambios sin guardar (_uiState.value.isSaveEnabled)
        // podrías mostrar diálogo, o simplemente navegar atrás:
        // navController.popBackStack()
    }
}
