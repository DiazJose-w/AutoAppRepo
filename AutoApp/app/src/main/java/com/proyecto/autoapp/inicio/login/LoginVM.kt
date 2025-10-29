package com.proyecto.autoapp.inicio.login

import android.app.Activity
import com.proyecto.autoapp.R
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.autoapp.general.Coleccion
import com.proyecto.autoapp.general.modelo.enumClass.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.TimeUnit

class LoginVM {
    var TAG = "Jose"
    var usuario = Coleccion.Usuario
    private val auth = FirebaseAuth.getInstance()

    private var usuarioLogeado: Map<String, Any>? = null

    val isLoading = MutableStateFlow(false)
    val loginSuccess = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    // Propiedad de verificación sms
    val codeSent = MutableStateFlow(false)

    /** Login con correo y contraseña.
     *  Login con Google.
     *  SingOut
     **/
    fun login(correo: String, pass: String, onResult: (Boolean) -> Unit) {
        isLoading.value = true
        errorMessage.value = null

        auth.signInWithEmailAndPassword(correo, pass)
            .addOnCompleteListener { task ->
                isLoading.value = false

                if (task.isSuccessful) {
                    val usuarioAuth = task.result?.user

                    if (usuarioAuth != null) {
                        val uid = usuarioAuth.uid
                        val firestore = FirebaseFirestore.getInstance()
                        val usuarioDocRef = firestore.collection(usuario).document(uid)

                        usuarioDocRef.get()
                            .addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    usuarioLogeado = snapshot.data
                                    onResult(true)
                                } else {
                                    onResult(false)
                                }
                            }
                            .addOnFailureListener { e ->
                                errorMessage.value = e.message
                                onResult(false)
                            }
                    } else {
                        errorMessage.value = "Error al obtener el usuario"
                        onResult(false)
                    }
                } else {
                    errorMessage.value = task.exception?.message ?: "Error desconocido en el inicio de sesión"
                    onResult(false)
                }
            }
    }

    fun loginWithGoogle(idToken: String,onResult: (Boolean) -> Unit) {
        isLoading.value = true
        errorMessage.value = null

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                isLoading.value = false

                if (task.isSuccessful) {
                    val userAuth = auth.currentUser

                    if (userAuth != null) {
                        val uid = userAuth.uid
                        val firestore = FirebaseFirestore.getInstance()
                        val usuarioDocRef = firestore.collection(usuario).document(uid)

                        usuarioDocRef.get()
                            .addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    usuarioLogeado = snapshot.data
                                    val esNuevo = snapshot.getBoolean("nuevo") ?: false
                                    if (esNuevo) {
                                        usuarioDocRef.update("nuevo", false)
                                    }
                                    onResult(true)
                                } else {
                                    // Primero mapear los perfiles
                                    val perfilConductorInit = mapOf(
                                        "enabled" to false,
                                        "ratingAvg" to 0.0,
                                        "ratingCount" to 0,
                                        "vehiculoActivoId" to null,
                                        "licenciaSubida" to false,
                                        "licenciaVerificada" to false
                                    )

                                    val perfilPasajeroInit = mapOf(
                                        "enabled" to false,
                                        "ratingAvg" to 0.0,
                                        "ratingCount" to 0
                                    )

                                    // 2. Montamos el documento del usuario
                                    val nuevoUsuario: Map<String, Any> = mapOf(
                                        "id" to uid,
                                        "nombre" to (userAuth.displayName ?: ""),
                                        "apellidos" to "",
                                        "email" to (userAuth.email ?: ""),
                                        "edad" to 0,
                                        "password" to "",
                                        "fotoUrl" to (userAuth.photoUrl?.toString() ?: ""),
                                        "rol" to RolUsuario.CUSTOMER.name,
                                        "nuevo" to true,
                                        "perfilConductor" to perfilConductorInit,
                                        "perfilPasajero" to perfilPasajeroInit
                                    )
                                    usuarioDocRef
                                        .set(nuevoUsuario)
                                        .addOnSuccessListener {
                                            usuarioLogeado = nuevoUsuario
                                            onResult(true)
                                        }
                                        .addOnFailureListener { e ->
                                            errorMessage.value = e.message ?: "Error guardando usuario nuevo"
                                            onResult(false)
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                errorMessage.value = e.message ?: "Error leyendo usuario en Firestore"
                                onResult(false)
                            }
                    } else {
                        errorMessage.value = "No se pudo obtener el usuario autenticado"
                        onResult(false)
                    }
                } else {
                    errorMessage.value = task.exception?.message ?: "Error desconocido en login"
                    onResult(false)
                }
            }
    }

    fun signOut(context: Context, onResult: (Boolean) -> Unit) {
        usuarioLogeado = null

        // No usamos revokeAccess() porque no queremos volver a pedir permisos cada vez.
        try {
            val googleSignInClient = GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            )

            googleSignInClient.signOut()
                .addOnCompleteListener {
                    auth.signOut()
                    onResult(true)
                }
                .addOnFailureListener { e ->
                    auth.signOut()
                    errorMessage.value = e.message
                    onResult(false)
                }

        } catch (e: Exception) {
            try {
                auth.signOut()
            } catch (_: Exception) { }
            errorMessage.value = e.message
            onResult(true)
        }
    }

    /**
     * MÉTODOS Y HERRAMIENTAS PARA LA VERIFICACIÓN MEDIANTE SMS
     *  -> 1) Enviar SMS
     *  -> 2) Reenviar SMS
     *  -> 3) Verificar código manual
     *  -> 4) Entrar con el código enviado al teléfono
     * */
    // Guardamos datos del proceso OTP
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    // Callbacks con firmas correctas
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Puede auto-verificar sin teclear el código
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {   // <-- FirebaseException
            isLoading.value = false
            Log.e(TAG, "onVerificationFailed", e)
            errorMessage.value = e.localizedMessage ?: "No se pudo verificar el teléfono"
        }

        override fun onCodeSent( verificationId: String,token: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(verificationId, token)
            isLoading.value = false
            this@LoginVM.verificationId = verificationId
            this@LoginVM.resendToken = token
            codeSent.value = true
            Log.d(TAG, "onCodeSent: id guardado $token")
        }

        override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
            super.onCodeAutoRetrievalTimeOut(verificationId)
            Log.w(TAG, "Auto-retrieval timeout")
        }
    }

    fun startPhoneVerification(activity: Activity, numberPhone: String) {
        if (numberPhone.isBlank()) {
            errorMessage.value = "Introduce un teléfono válido"

        }else{
            isLoading.value = true

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(numberPhone)                // p.ej. "+34600111222"
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)                    // la Activity viene de la UI
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    fun resendCode(activity: Activity, phoneE164: String) {
        val token = resendToken ?: run { errorMessage.value = "No hay token de reenvío"; return }
        isLoading.value = true

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneE164)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode(code: String) {
        val id = verificationId ?: run { errorMessage.value = "Primero solicita el código"; return }
        if (code.length < 6) { errorMessage.value = "El código debe tener 6 dígitos"; return }

        isLoading.value = true
        val credential = PhoneAuthProvider.getCredential(id, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    loginSuccess.value = true
                    Log.d(TAG, "Inicio de sesión por SMS OK")
                } else {
                    Log.e(TAG, "signInWithCredential error", task.exception)
                    errorMessage.value = task.exception?.localizedMessage
                        ?: "Código incorrecto o caducado"
                }
            }
    }
}