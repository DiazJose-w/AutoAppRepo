package com.proyecto.autoapp.inicio.registro

import com.google.firebase.functions.FirebaseFunctions
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.functions
import com.proyecto.autoapp.general.Coleccion
import com.proyecto.autoapp.general.modelo.enumClass.RolUsuario
import com.proyecto.autoapp.general.modelo.usuarios.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await

class RegistroVM() {
    private val TAG = "jose"
    private val auth = FirebaseAuth.getInstance()

    val isLoading = MutableStateFlow(false)
    val loginSuccess = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    /**     MÉTODOS PARA EL REGISTRO DEL USUARIO     */
    fun registroWhitEmail(nombre: String, apellidos: String, edad: String, password: String, email: String, onResult: (Boolean) -> Unit){
        Log.e(TAG, "Entrando en registroWhit")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading.value = false

                if(task.isSuccessful){
                    val userId = task.result?.user?.uid
                    if(userId != null){
                        // Lo primero que hago es crear un usuario de customer.
                        // después en editar perfil, al registrar al usuario
                        //es donde debo darle el perfil de conductor o pasajero o ambos si así lo fuese

                        val edadInt = edad.toIntOrNull() ?: -1
                        val usuario = Usuario(
                           userId,
                           nombre,
                           apellidos,
                           email,
                           edadInt,
                           password,
                           null,
                           RolUsuario.CUSTOMER,
                           false
                       )
//                        Log.e(TAG, "[RegisterVM] Usuario preparado:")
//                        Log.e(TAG, " - id..........: ${usuario.id}")
//                        Log.e(TAG, " - nombre......: ${usuario.nombre}")
//                        Log.e(TAG, " - apellidos...: ${usuario.apellidos}")
//                        Log.e(TAG, " - email.......: ${usuario.email}")
//                        Log.e(TAG, " - edad........: ${usuario.edad}")
//                        Log.e(TAG, " - rol.........: ${usuario.rol}")
//                        Log.e(TAG, " - nuevo.......: ${usuario.nuevo}")

                        // Mapeo de la colección
                        val datosUsuario = mapOf(
                            "id" to usuario.id,
                            "nombre" to usuario.nombre,
                            "apellidos" to usuario.apellidos,
                            "email" to usuario.email,
                            "edad" to usuario.edad,
                            "password" to usuario.password,
                            "fotoUrl" to usuario.fotoUrl,
                            "rolUsuario" to usuario.rol!!.name,
                            "nuevo" to usuario.nuevo
                        )

                        val db = FirebaseFirestore.getInstance()
                        db.collection(Coleccion.Usuario)
                            .document(userId)
                            .set(datosUsuario)
                            .addOnSuccessListener {
                                Log.e(TAG, " Datos del usuario mapeados: $datosUsuario")
                                onResult(true)
                            }
                            .addOnFailureListener { e ->
                                onResult(false)
                                errorMessage.value = e.message ?: "Error desconocido"
                                Log.e(TAG, "Error al registrar", e)
                            }
                    }else {
                        onResult(false)
                        errorMessage.value = task.exception?.message ?: "Error desconocido"
                        Log.e(TAG, "Error en el registro", task.exception)
                    }
                }else {
                    onResult(false)
                    errorMessage.value = task.exception?.message ?: "Error creando Auth"
                    Log.e(TAG, "Fallo en createUserWithEmailAndPassword", task.exception)
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