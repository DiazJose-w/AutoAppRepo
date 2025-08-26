package com.proyecto.autoapp.inicio.modelo.perfil

data class PerfilConductor(
    val enabled: Boolean = false,
//    val licencia: String? = null,
//    val verificado: Boolean = false,
    val ratingAvg: Double = 0.0,
    val ratingCount: Long = 0
)
