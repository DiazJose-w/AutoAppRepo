package com.proyecto.autoapp.general.modelo.enumClass

enum class RolUsuario (var valor: Int){
    ADMIN(0),
    CUSTOMER(1);

    companion object {
        fun desdeValor(valor: Int): RolUsuario {
            return entries.first { it.valor == valor }
        }

        fun desdeString(valor : RolUsuario): Int{
            var res: Int

            if(valor == CUSTOMER){
                res = 1
            }else{
                res = 0
            }

            return res
        }
    }
}