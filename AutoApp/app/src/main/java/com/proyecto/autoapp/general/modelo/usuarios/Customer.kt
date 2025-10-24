package com.proyecto.autoapp.general.modelo.usuarios

import com.proyecto.autoapp.general.modelo.enumClass.PerfilCustomer
import com.proyecto.autoapp.general.modelo.enumClass.RolUsuario

class Customer (
    id: String,
    nombre: String,
    apellidos: String?,
    email: String?,
    edad: Int?,
    password: String?,
    fotoUrl: String?,
    perfil: PerfilCustomer?
): Usuario(id, nombre, apellidos, email, edad, password, fotoUrl, RolUsuario.CUSTOMER)