package com.proyecto.autoapp.general.modelo.peticiones

data class Peticion(
    val id: String = "",
    val uidUsuario: String = "",
    var uidConductorAcep: String= "",
    var uidConductorCan : List<String> = emptyList(),
    val inicioTexto: String = "",
    val inicioLat: Double? = null,
    val inicioLng: Double? = null,
    val inicioPlaceId: String? = null,
    val destinoTexto: String = "",
    val destinoLat: Double? = null,
    val destinoLng: Double? = null,
    val destinoPlaceId: String? = null,
    val estado: String = "pendiente",
    val timestamp: Long = System.currentTimeMillis()
)
