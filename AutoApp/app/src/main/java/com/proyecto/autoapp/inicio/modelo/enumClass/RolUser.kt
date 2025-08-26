package com.proyecto.autoapp.inicio.modelo.enumClass

enum class RolUser (var valor: Int){
    ADMIN(0),
    USER(1),
    DRIVER(2),
    PASSENGER(3);

    companion object {
        fun desdeValor(valor: Int): RolUser {
            return entries.first { it.valor == valor }
        }

        fun desdeString(valor : RolUser): Int{
            var res: Int

            if(valor.equals(USER)){
                res = 1
            }else if(valor.equals(DRIVER)){
                res = 2
            }else if (valor.equals(PASSENGER)){
                res = 3
            }else{
                res = 0
            }

            return res
        }
    }
}