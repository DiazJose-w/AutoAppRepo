package com.proyecto.autoapp.general.modelo.dataClass

import com.proyecto.autoapp.general.modelo.enumClass.Estado


// =====================================================
// Muestra los datos en la view perfil de usuario
// =====================================================
data class PerfilUiState(
    val nombre: String = "",
    val apellidos: String = "",
    val edad: String = "",
    val email: String = "",

    val fotoPerfilUrl: String? = null,
    val tieneMasFotos: Boolean = false,

    val isPasajeroSelected: Boolean = false,
    val isConductorSelected: Boolean = false,

    // Pasajero
    val pasajeroEnabled: Estado = Estado.PENDIENTE,
    val pasajeroRatingAvg: Double = 0.0,
    val pasajeroRatingCount: Long = 0,

    // Conductor
    val conductorEnabled: Estado = Estado.PENDIENTE,
    val conductorRatingAvg: Double = 0.0,
    val conductorRatingCount: Long = 0,

    // Licencia / verificación
    val licenciaSubida: Boolean = false,
    val licenciaVerificada: Boolean = false,

    // Vehículo
    val vehiculoFotoUrl: String? = null,
    val vehiculoDescripcion: String = "",

    // UI
    val isSaveEnabled: Boolean = true,
    val showEdadWarningConductor: Boolean = false
)

