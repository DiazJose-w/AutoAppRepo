package com.proyecto.autoapp.inicio.modelo.perfil

data class Perfiles(
    val conductor: PerfilConductor = PerfilConductor(),
    val pasajero: PerfilPasajero = PerfilPasajero()
)