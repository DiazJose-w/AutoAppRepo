package com.proyecto.autoapp.general.modelo.dataClass

data class Vehiculo(
    val id: String = "",
    val modelo: String = "",
    val matricula: String = "",
    val color: String = "",
    val plazas: Int = 4,
    val fotoUrl: String? = null,
    val verificado: Boolean = false,
    val activo: Boolean = false
)