package com.proyecto.autoapp.general.modelo.dataClass

data class TrackingViajero(
    val compartiendo: Boolean = false,
    val lat: Double? = null,
    val lng: Double? = null,
    val ultimaActualizacion: Long? = null
)

