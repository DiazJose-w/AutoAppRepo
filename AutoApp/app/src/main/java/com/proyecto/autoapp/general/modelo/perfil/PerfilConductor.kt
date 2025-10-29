package com.proyecto.autoapp.general.modelo.perfil

data class PerfilConductor(
    val enabled: Boolean = false,
    val ratingAvg: Double = 0.0,
    val ratingCount: Long = 0,
    val vehiculoActivoId: String? = null
)
