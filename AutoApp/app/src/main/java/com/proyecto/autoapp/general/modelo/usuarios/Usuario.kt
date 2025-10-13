package com.proyecto.autoapp.general.modelo.usuarios

import com.proyecto.autoapp.general.modelo.enumClass.RolUsuario

open class Usuario (
    val id: String,
    val nombre: String?,
    var apellidos : String?,
    val email: String?,
    var edad: Int?,
    val password: String?,
    val fotoUrl: String?,
    val rol: RolUsuario?
)