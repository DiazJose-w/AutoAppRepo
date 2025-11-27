package com.proyecto.autoapp.general.modelo.peticiones

import com.proyecto.autoapp.general.modelo.dataClass.InfoConductor
import com.proyecto.autoapp.general.modelo.dataClass.PuntoPeticion
import com.proyecto.autoapp.general.modelo.dataClass.TrackingViajero

data class Peticion(
    val id: String = "",
    val uidUsuario: String = "",
    val uidConductorCan: List<String> = emptyList(), // Conductores que cancelaron
    val estado: String = "pendiente",
    val timestamp: Long = System.currentTimeMillis(),

    // Punto de origen y destino
    val inicio: PuntoPeticion = PuntoPeticion(),
    val destino: PuntoPeticion = PuntoPeticion(),

    // Conductor que ha aceptado
    val infoConductor: InfoConductor? = null,

    // Tracking de ubicación del viajero
    val trackingViajero: TrackingViajero? = null
)

