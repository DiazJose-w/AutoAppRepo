package com.proyecto.autoapp.general.modelo.usuarios

import com.proyecto.autoapp.general.modelo.enumClass.RolUsuario
import com.proyecto.autoapp.general.modelo.perfil.PerfilConductor
import com.proyecto.autoapp.general.modelo.perfil.PerfilPasajero

class Customer (
    id: String = "",
    nombre: String = "",
    apellidos: String? = null,
    email: String? = null,
    edad: Int? = null,
    password: String? = null,
    fotoUrl: String? = null,
    val perfilConductor: PerfilConductor = PerfilConductor(),
    val perfilPasajero: PerfilPasajero = PerfilPasajero(),
    nuevo: Boolean = true
): Usuario(id, nombre, apellidos, email, edad, password, fotoUrl, RolUsuario.CUSTOMER, false)