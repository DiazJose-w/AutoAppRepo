package com.proyecto.autoapp.general.modelo.dataClass

data class ViajeUi(
    val id: String = "",
    val conductor: String = "",
    val valoracion: Double = 0.0, // Valoración promeria del conductor
    val vehiculo: String = "",
    val color: String = "",
    val plazasDisponibles: Int = 0,
    val distancia: String = "",
    val duracion: String = "",
    val horaRecogida: String = "",
    val horaLlegada: String = "",
    val puntoRecogida: String = "",
    val puntoDestino: String = ""
)
