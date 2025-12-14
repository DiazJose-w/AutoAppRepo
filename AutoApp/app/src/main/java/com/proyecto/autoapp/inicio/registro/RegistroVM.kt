package com.proyecto.autoapp.inicio.registro

import com.google.firebase.functions.FirebaseFunctions
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.functions
import com.proyecto.autoapp.general.Coleccion
import com.proyecto.autoapp.general.modelo.enumClass.RolUsuario
import com.proyecto.autoapp.general.modelo.perfil.PerfilConductor
import com.proyecto.autoapp.general.modelo.perfil.PerfilPasajero
import com.proyecto.autoapp.general.modelo.usuarios.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import java.util.Date

class RegistroVM() {
    val TAG = "Jose"
    val auth = FirebaseAuth.getInstance()
    var usuario = Coleccion.Usuario

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    /**     MÉTODOS PARA EL REGISTRO DEL USUARIO     */
    fun registroWhitEmail(nombre: String, apellidos: String, fechaNacimiento: Long?, password: String, email: String, onResult: (Boolean) -> Unit, uid: (String) -> Unit) {
        isLoading.value = true
        errorMessage.value = null

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid

                    if (userId != null) {
                        val edadInt = calcularEdad(fechaNacimiento)

                        val fechaNacimientoTimestamp = fechaNacimiento?.let { millis ->
                            Timestamp(Date(millis))
                        }

                        val customer = Customer(
                            id = userId,
                            nombre = nombre,
                            apellidos = apellidos,
                            email = email,
                            edad = edadInt,
                            password = password,
                            fotoUrl = null,
                            perfilConductor = PerfilConductor(
                                enabled = false,
                                ratingAvg = 0.0,
                                ratingCount = 0,
                                vehiculoActivoId = null,
                                licenciaSubida = false,
                                licenciaVerificada = false
                            ),
                            perfilPasajero = PerfilPasajero(
                                enabled = false,
                                ratingAvg = 0.0,
                                ratingCount = 0
                            ),
                            nuevo = true
                        )

                        val datosUsuario: Map<String, Any?> = mapOf(
                            "id" to customer.id,
                            "nombre" to customer.nombre,
                            "apellidos" to customer.apellidos,
                            "email" to customer.email,
                            "edad" to customer.edad,
                            "fechaNacimiento" to fechaNacimientoTimestamp,
                            "password" to customer.password,
                            "fotoUrl" to customer.fotoUrl,
                            "rol" to RolUsuario.CUSTOMER.name,
                            "nuevo" to true,
                            "perfilConductor" to mapOf(
                                "enabled" to customer.perfilConductor.enabled,
                                "ratingAvg" to customer.perfilConductor.ratingAvg,
                                "ratingCount" to customer.perfilConductor.ratingCount,
                                "vehiculoActivoId" to customer.perfilConductor.vehiculoActivoId,
                                "licenciaSubida" to customer.perfilConductor.licenciaSubida,
                                "licenciaVerificada" to customer.perfilConductor.licenciaVerificada
                            ),
                            "perfilPasajero" to mapOf(
                                "enabled" to customer.perfilPasajero.enabled,
                                "ratingAvg" to customer.perfilPasajero.ratingAvg,
                                "ratingCount" to customer.perfilPasajero.ratingCount
                            )
                        )

                        val db = FirebaseFirestore.getInstance()

                        db.collection(usuario)
                            .document(userId)
                            .set(datosUsuario)
                            .addOnSuccessListener {
                                uid(userId)
                                Log.e(TAG, "Usuario registrado $datosUsuario")
                                isLoading.value = false
                                onResult(true)
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error al guardar el usuario en Firestore", e)
                                isLoading.value = false
                                errorMessage.value = e.message ?: "Error desconocido al guardar en Firestore"
                                onResult(false)
                            }

                    } else {
                        isLoading.value = false
                        errorMessage.value = "No se pudo obtener el UID del usuario"
                        onResult(false)
                    }
                } else {
                    isLoading.value = false
                    errorMessage.value = task.exception?.message ?: "Error creando Auth"
                    onResult(false)
                }
            }
    }

    /** -------------------- Enviar token por EMAIL -------------------- */
    private val functions: FirebaseFunctions = Firebase.functions("europe-west1") // región

    suspend fun requestVerification(email: String): Boolean {
        return try {
            functions
                .getHttpsCallable("requestEmailToken")
                .call(mapOf("email" to email))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun calcularEdad(fechaNacimiento: Long?): Int {
        var edadResultado = 0

        if (fechaNacimiento != null) {
            val nacimiento = java.util.Calendar.getInstance().apply {
                timeInMillis = fechaNacimiento
            }

            val hoy = java.util.Calendar.getInstance()

            edadResultado = hoy.get(java.util.Calendar.YEAR) - nacimiento.get(java.util.Calendar.YEAR)

            val mesHoy = hoy.get(java.util.Calendar.MONTH)
            val diaHoy = hoy.get(java.util.Calendar.DAY_OF_MONTH)

            val mesNac = nacimiento.get(java.util.Calendar.MONTH)
            val diaNac = nacimiento.get(java.util.Calendar.DAY_OF_MONTH)

            if (mesHoy < mesNac || (mesHoy == mesNac && diaHoy < diaNac)) {
                edadResultado--
            }
        }

        return edadResultado
    }
}