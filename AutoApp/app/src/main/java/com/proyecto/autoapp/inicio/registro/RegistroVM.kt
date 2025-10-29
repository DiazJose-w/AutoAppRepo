package com.proyecto.autoapp.inicio.registro

import com.google.firebase.functions.FirebaseFunctions
import android.util.Log
import androidx.compose.foundation.layout.Column
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.functions
import com.proyecto.autoapp.general.Coleccion
import com.proyecto.autoapp.general.modelo.enumClass.RolUsuario
import com.proyecto.autoapp.general.modelo.perfil.PerfilConductor
import com.proyecto.autoapp.general.modelo.perfil.PerfilPasajero
import com.proyecto.autoapp.general.modelo.usuarios.Customer
import com.proyecto.autoapp.general.modelo.usuarios.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await

class RegistroVM() {
    private val TAG = "jose"
    private val auth = FirebaseAuth.getInstance()
    var usuario = Coleccion.Usuario

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    /**     MÉTODOS PARA EL REGISTRO DEL USUARIO     */
    fun registroWhitEmail(nombre: String, apellidos: String, edad: String, password: String, email: String, onResult: (Boolean) -> Unit){
        Log.e(TAG, "Entrando en registroWhit")

        isLoading.value = true
        errorMessage.value = null

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid

                    if (userId != null) {
                        val edadInt = edad.toIntOrNull() ?: -1

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
                                vehiculoActivoId = null
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
                            "password" to customer.password,
                            "fotoUrl" to customer.fotoUrl,
                            "rol" to RolUsuario.CUSTOMER.name,
                            "nuevo" to customer.nuevo,
                            "perfilConductor" to mapOf(
                                "enabled" to customer.perfilConductor.enabled,
                                "ratingAvg" to customer.perfilConductor.ratingAvg,
                                "ratingCount" to customer.perfilConductor.ratingCount,
                                "vehiculoActivoId" to customer.perfilConductor.vehiculoActivoId
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
                                Log.e(TAG, "[RegisterVM] Usuario registrado y guardado en Firestore:")
                                Log.e(TAG, datosUsuario.toString())

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
                        // userId == null, cosa rara pero la controlamos
                        Log.e(TAG, "userId nulo tras createUserWithEmailAndPassword")

                        isLoading.value = false
                        errorMessage.value = "No se pudo obtener el UID del usuario"
                        onResult(false)
                    }

                } else {
                    Log.e(TAG, "Fallo en createUserWithEmailAndPassword", task.exception)

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
                .await() // usa kotlinx-coroutines-play-services
            true
        } catch (e: Exception) {
            false
        }
    }
}