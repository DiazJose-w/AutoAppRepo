package com.proyecto.autoapp.general.modelo.usuarios

import com.proyecto.autoapp.general.modelo.enumClass.RolUsuario

class Admin(
    id: String = "",
    nombre: String = "",
    apellidos: String? = null,
    email: String? = null,
    edad: Int? = null,
    password: String? = null,
    fotoUrl: String? = null,
    nuevo: Boolean = false
) : Usuario(id, nombre, apellidos, email, edad, password, fotoUrl, RolUsuario.ADMIN, nuevo)
