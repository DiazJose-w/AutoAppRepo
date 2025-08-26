package com.proyecto.autoapp.inicio.modelo.enumClass


enum class PerfilUser(var valor: Int) {
    CONDUCTOR(2),
    PASAJERO(3);

    companion object {
        fun desdeValor(valor: Int): PerfilUser {
            return entries.first { it.valor == valor }
        }

        fun desdeString(valor : PerfilUser): Int{
            var res: Int

            if(valor == CONDUCTOR){
                res = 2
            }else{
                res = 3
            }

            return res
        }
    }
}