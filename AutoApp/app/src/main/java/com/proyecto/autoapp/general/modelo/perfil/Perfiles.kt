package com.proyecto.autoapp.general.modelo.perfil

data class Perfiles(
    val conductor: PerfilConductor = PerfilConductor(),
    val pasajero: PerfilPasajero = PerfilPasajero()
)