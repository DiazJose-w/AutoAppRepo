package com.proyecto.autoapp.general.modelo.dataClass

data class PuntoPeticion(
    val texto: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    val placeId: String? = null
)

