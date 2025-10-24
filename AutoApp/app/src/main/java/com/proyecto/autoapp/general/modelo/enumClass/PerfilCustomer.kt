package com.proyecto.autoapp.general.modelo.enumClass

enum class PerfilCustomer(var valor: Int) {
    CONDUCTOR(2),
    PASAJERO(3);

    companion object {
        fun desdeValor(valor: Int): PerfilCustomer {
            return entries.first { it.valor == valor }
        }

        fun desdeString(valor : PerfilCustomer): Int{
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