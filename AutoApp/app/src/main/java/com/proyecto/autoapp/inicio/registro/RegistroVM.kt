package com.proyecto.autoapp.inicio.registro

import com.google.firebase.functions.FirebaseFunctions
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.functions
import com.proyecto.autoapp.general.Coleccion
import com.proyecto.autoapp.inicio.modelo.enumClass.RolUsuario
import com.proyecto.autoapp.inicio.modelo.usuarios.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class RegistroVM() {
    private val TAG = "jose"
    private val auth = FirebaseAuth.getInstance()

    val isLoading = MutableStateFlow(false)
    val loginSuccess = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    /**     MÉTODOS PARA EL REGISTRO DEL USUARIO     */
    fun registroWhitEmail(nombre: String, apellidos: String, email: String, edad: Int, password: String, onResult: (Boolean) -> Unit){

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading.value = false

                if(task.isSuccessful){
                    val userId = task.result?.user?.uid
                    if(userId != null){
                        // Lo primero que hago es crear un usuario de customer. Después en editar perfil al registrar
                        //es donde debo darle el perfil de conductor o pasajero o ambos si así lo fuese
                       val usuario = Usuario(
                           userId,
                           nombre,
                           apellidos,
                           email,
                           edad,
                           password,
                           null,
                           RolUsuario.CUSTOMER
                       )
                        Log.e(TAG, "[RegisterVM]-> id registrado ${usuario.id}")

                        val db = FirebaseFirestore.getInstance()
                        db.collection(Coleccion.Usuario)
                            .document(userId)
                            .set(
                                mapOf(
                                    "id" to usuario.id,
                                    "nombre" to usuario.nombre,
                                    "apellidos" to usuario.apellidos,
                                    "email" to usuario.email,
                                    "edad" to usuario.edad,
                                    "password" to usuario.password,
                                    "fotoUrl" to usuario.fotoUrl,
                                    "rolUsuario" to usuario.rol
                                )
                            )
                            .addOnSuccessListener {
                                onResult(true)
                            }
                            .addOnFailureListener { e ->
                                onResult(false)
                                errorMessage.value = e.message ?: "Error desconocido"
                                Log.e(TAG, "Error en el registro", e)
                            }
                    }else {
                        onResult(false)
                        errorMessage.value = task.exception?.message ?: "Error desconocido"
                        Log.e(TAG, "Error en el registro", task.exception)
                    }
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