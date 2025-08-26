package com.proyecto.autoapp.inicio.login

import com.proyecto.autoapp.R
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.autoapp.general.Coleccion
import kotlinx.coroutines.flow.MutableStateFlow

class LoginVM {
    var TAG = "Jose"
    private val auth = FirebaseAuth.getInstance()

    val isLoading = MutableStateFlow(false)
    val loginSuccess = MutableStateFlow(false)
    val loginGoogleSuccess = MutableStateFlow(false)
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
    fun loginWithGoogle(idToken: String) {
        isLoading.value = true
        errorMessage.value = null
        loginSuccess.value = false

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    loginSuccess.value = true
                    loginGoogleSuccess.value = true
                } else {
                    errorMessage.value = task.exception?.message ?: "Error desconocido"
                }
            }
    }

    fun signOut(context: Context) {
        Log.d(TAG, "signOut() llamado ${loginGoogleSuccess.value}")
        if (loginGoogleSuccess.value) {
            //El usuario inició sesión con Google
            val googleSignInClient = GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            )

            googleSignInClient.revokeAccess().addOnCompleteListener { revokeTask ->
                if (revokeTask.isSuccessful) {
                    Log.d(TAG, "Acceso revocado correctamente")
                    auth.signOut()
                    Log.d(TAG, "Sesión cerrada correctamente")
                } else {
                    Log.e(TAG, "Error al revocar el acceso")
                }
            }
        } else {
            //El usuario no inició sesión con Google (email/contraseña u otro proveedor)
            auth.signOut()
            Log.d(TAG, "Sesión cerrada para usuario no Google")
        }

        //Actualizar el estado de las variables de UI
        loginGoogleSuccess.value = false
        loginSuccess.value = false
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}