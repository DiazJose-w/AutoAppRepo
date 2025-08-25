package com.proyecto.autoapp.inicio.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.autoapp.general.Coleccion
import kotlinx.coroutines.flow.MutableStateFlow

class LoginVM {
    var TAG = "Jose"
    private val auth = FirebaseAuth.getInstance()

    val isLoading = MutableStateFlow(false)
    val loginSuccess = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    /** Login con correo y contraseña.*/
    fun login(correo: String, pass: String, onResult: (Boolean) -> Unit){
        isLoading.value = true
        errorMessage.value = null
        loginSuccess.value = false

        auth.signInWithEmailAndPassword(correo, pass)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    val usuario = task.result?.user

                } else {
                    onResult(false)
                    errorMessage.value = task.exception?.message ?: "Error desconocido"
                }
            }
    }

    fun logout(onComplete: (Boolean) -> Unit) {
        val usuario = auth.currentUser
        if (usuario != null) {
            val userId = usuario.uid

            val db = FirebaseFirestore.getInstance()
            db.collection(Coleccion.Usuario)
                .document(userId)
                .get()
                .addOnSuccessListener {
                    auth.signOut()
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        } else {
            onComplete(false)
        }
    }
    /** Login con Google */
}