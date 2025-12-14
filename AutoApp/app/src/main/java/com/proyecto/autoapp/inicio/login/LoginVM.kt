package com.proyecto.autoapp.inicio.login

import android.app.Activity
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
import com.proyecto.autoapp.R
import com.proyecto.autoapp.general.Coleccion
import com.proyecto.autoapp.general.funcionesComunes.formatE164
import com.proyecto.autoapp.general.modelo.enumClass.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.concurrent.TimeUnit

class LoginVM {

    private val TAG = "Jose"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usuarioCollection = Coleccion.Usuario

    private val _uidActual = MutableStateFlow("")
    val uidActual: StateFlow<String> = _uidActual.asStateFlow()

    fun setUidActual(uid: String) {
        _uidActual.value = uid
    }

    fun limpiarUidActual() {
        _uidActual.value = ""
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private fun setLoading(value: Boolean) {
        _isLoading.value = value
    }

    private fun setError(msg: String?) {
        _errorMessage.value = msg
    }

    private fun setLoginSuccess(value: Boolean) {
        _loginSuccess.value = value
    }

    var usuarioLogeado: Map<String, Any>? = null

    fun login(correo: String, pass: String, onResult: (Boolean) -> Unit) {
        setLoading(true)
        setError(null)

        auth.signInWithEmailAndPassword(correo, pass)
            .addOnCompleteListener { task ->
                setLoading(false)

                if (!task.isSuccessful) {
                    setError(task.exception?.message ?: "Error desconocido en el inicio de sesión")
                    onResult(false)
                    return@addOnCompleteListener
                }

                val usuarioAuth = task.result?.user
                if (usuarioAuth == null) {
                    setError("Error al obtener el usuario")
                    onResult(false)
                    return@addOnCompleteListener
                }

                db.collection(usuarioCollection)
                    .document(usuarioAuth.uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            setUidActual(doc.id)
                            Log.e(TAG, "Uid de usuario logeado = ${uidActual.value}")
                            onResult(true)
                        } else {
                            setError("No existe documento de usuario en Firestore")
                            onResult(false)
                        }
                    }
                    .addOnFailureListener { e ->
                        setError(e.message)
                        onResult(false)
                    }
            }
    }

    fun loginWithGoogle(idToken: String, onResult: (Boolean) -> Unit, uid: (String) -> Unit) {
        setLoading(true)
        setError(null)

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                setLoading(false)

                if (!task.isSuccessful) {
                    setError(task.exception?.message ?: "Error desconocido en login")
                    onResult(false)
                    return@addOnCompleteListener
                }

                val userAuth = auth.currentUser
                if (userAuth == null) {
                    setError("No se pudo obtener el usuario autenticado")
                    onResult(false)
                    return@addOnCompleteListener
                }

                val uidUsuario = userAuth.uid
                val usuarioDocRef = db.collection(usuarioCollection).document(uidUsuario)

                usuarioDocRef.get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            setUidActual(doc.id)

                            val esNuevo = doc.getBoolean("nuevo") ?: false
                            if (esNuevo) {
                                usuarioDocRef.update("nuevo", false)
                            }

                            uid(uidActual.value)
                            onResult(true)
                        } else {
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

                            val nuevoUsuario: Map<String, Any> = mapOf(
                                "id" to uidUsuario,
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
                                    setUidActual(usuarioDocRef.id)
                                    uid(uidActual.value)
                                    onResult(true)
                                }
                                .addOnFailureListener { e ->
                                    setError(e.message ?: "Error guardando usuario nuevo")
                                    onResult(false)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        setError(e.message ?: "Error leyendo usuario en Firestore")
                        onResult(false)
                    }
            }
    }

    fun signOut(context: Context, onResult: (Boolean) -> Unit) {
        usuarioLogeado = null
        limpiarUidActual()
        setLoginSuccess(false)
        setError(null)

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
                    setError(e.message)
                    onResult(false)
                }
        } catch (e: Exception) {
            try { auth.signOut() } catch (_: Exception) { }
            setError(e.message)
            onResult(true)
        }
    }

    fun obtenerRolesUsuario(onResult: (esViajero: Boolean, esConductor: Boolean) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(true, false)
            return
        }

        db.collection(usuarioCollection)
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val perfilPasajero = doc.get("perfilPasajero") as? Map<*, *>
                val esViajero = (perfilPasajero?.get("enabled") as? Boolean) ?: false

                val perfilConductor = doc.get("perfilConductor") as? Map<*, *>
                val esConductor = (perfilConductor?.get("enabled") as? Boolean) ?: false

                onResult(esViajero, esConductor)
            }
            .addOnFailureListener {
                onResult(true, false)
            }
    }

    // ==========================================================
    // OTP / VERIFICACIÓN POR SMS
    // ==========================================================
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val _codeSent = MutableStateFlow(false)
    val codeSent: StateFlow<Boolean> = _codeSent.asStateFlow()

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            setLoading(false)
            Log.e(TAG, "onVerificationFailed", e)
            setError(e.localizedMessage ?: "No se pudo verificar el teléfono")
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, token)
            setLoading(false)
            this@LoginVM.verificationId = verificationId
            this@LoginVM.resendToken = token
            _codeSent.value = true
            Log.d(TAG, "onCodeSent: token guardado")
        }

        override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
            super.onCodeAutoRetrievalTimeOut(verificationId)
            Log.w(TAG, "Auto-retrieval timeout")
        }
    }

    fun startPhoneVerification(activity: Activity, numberPhone: String) {
        if (numberPhone.isBlank()) {
            setError("Introduce un teléfono válido")
            return
        }

        setLoading(true)
        setError(null)

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(numberPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun resendCode(activity: Activity, phoneE164: String) {
        val token = resendToken ?: run {
            setError("No hay token de reenvío")
            return
        }

        setLoading(true)
        setError(null)

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
        val id = verificationId ?: run {
            setError("Primero solicita el código")
            return
        }

        if (code.length < 6) {
            setError("El código debe tener 6 dígitos")
            return
        }

        setLoading(true)
        setError(null)

        val credential = PhoneAuthProvider.getCredential(id, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                setLoading(false)

                if (task.isSuccessful) {
                    setLoginSuccess(true)
                    Log.d(TAG, "Inicio de sesión por SMS OK")
                } else {
                    Log.e(TAG, "signInWithCredential error", task.exception)
                    setError(task.exception?.localizedMessage ?: "Código incorrecto o caducado")
                }
            }
    }

    // -----------------------------
    // CHECK TELÉFONO REGISTRADO
    // -----------------------------
    fun comprobarTelefonoRegistrado(email: String, phoneInput: String, onResult: (Boolean) -> Unit) {
        val emailNorm = email.trim().lowercase(Locale.ROOT)
        val phoneE164 = if (phoneInput.isBlank()) null else formatE164(phoneInput, defaultRegion = "ES")

        if (phoneInput.isNotBlank() && phoneE164 == null) {
            Log.e(TAG, "Teléfono inválido: $phoneInput")
            onResult(false)
            return
        }

        db.collection(usuarioCollection)
            .whereEqualTo("telefono", phoneE164 ?: "")
            .whereEqualTo("email", emailNorm)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val existe = !snapshot.isEmpty
                Log.e(TAG, "Existe = $existe (tel=$phoneE164, email=$emailNorm)")
                onResult(existe)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error comprobando teléfono", e)
                setError(e.message ?: "Error comprobando el teléfono")
                onResult(false)
            }
    }
}
