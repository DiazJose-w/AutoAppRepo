package com.proyecto.autoapp.inicio.modelo.enumClass

enum class RolUser (var valor: Int){
    ADMIN(0),
    USER(1);

    companion object {
        fun desdeValor(valor: Int): RolUser {
            return entries.first { it.valor == valor }
        }

        fun desdeString(valor : RolUser): Int{
            var res: Int

            if(valor == USER){
                res = 1
            }else{
                res = 0
            }

            return res
        }
    }
}