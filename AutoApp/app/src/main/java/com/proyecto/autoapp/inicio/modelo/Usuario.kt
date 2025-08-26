package com.proyecto.autoapp.inicio.modelo

import com.proyecto.autoapp.inicio.modelo.enumClass.RolUser
import com.proyecto.autoapp.inicio.modelo.perfil.Perfiles

open class Usuario (
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: RolUser,
    val perfil : Perfiles
)
