package com.proyecto.autoapp.general.funcionesComunes

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat

/**
 * Funciones:
 * - Expresión regular para el email
 * - Comprobación de campos de registro
 * - Método comprobar edad del usuario
 * - Expresión regular para seguridad de contraseña
 * - Función para formato E164
 */

// Composable para la imagen según paso del registro
fun imagenRegistro(){
    /**     FALTA POR IMPLEMENTAR. BUSCAR IMÁGENES PARA ELLO     */
}

// Función para castear el número de teléfono
fun formatE164(phoneRaw: String, defaultRegion: String = "ES"): String? {
    val util = PhoneNumberUtil.getInstance()

    return try {
        val proto = util.parse(phoneRaw, defaultRegion)
        if (util.isValidNumber(proto)) util.format(proto, PhoneNumberFormat.E164)
        else
            null
    } catch (_: NumberParseException) {
        null
    }
}

// Expresión para comprobar si el formato del email es correcto
fun validarEmail(email: String): Boolean {
    val emailPermitido = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
    return email.matches(emailPermitido.toRegex())
}

// Función para comprobar que el usuario cumple con la edad mínima
fun isEdadValida(edadTexto: String): Boolean {
    val edadInt = edadTexto.toIntOrNull() ?: return false
    return edadInt in 18..80
}