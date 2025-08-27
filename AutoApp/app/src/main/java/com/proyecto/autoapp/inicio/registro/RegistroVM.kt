package com.proyecto.autoapp.inicio.registro

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.TimeUnit

class RegistroVM() {
    private val TAG = "jose"
    private val auth = FirebaseAuth.getInstance()

    val isLoading = MutableStateFlow(false)
    val loginSuccess = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)
    val codeSent = MutableStateFlow(false)


    /**     MÉTODOS Y HERRAMIENTAS PARA LA VERIFICACIÓN MEDIANTE SMS     */
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
            this@RegistroVM.verificationId = verificationId
            this@RegistroVM.resendToken = token
            codeSent.value = true
            Log.d(TAG, "onCodeSent: id guardado $token")
        }

        override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
            super.onCodeAutoRetrievalTimeOut(verificationId)
            Log.w(TAG, "Auto-retrieval timeout")
        }
    }

    // 1) Enviar SMS
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

    // 2) Reenviar SMS
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

    // 3) Verificar código manual
    fun verifyCode(code: String) {
        val id = verificationId ?: run { errorMessage.value = "Primero solicita el código"; return }
        if (code.length < 6) { errorMessage.value = "El código debe tener 6 dígitos"; return }

        isLoading.value = true
        val credential = PhoneAuthProvider.getCredential(id, code)
        signInWithPhoneAuthCredential(credential)
    }

    // 4) Entrar con el código enviado al teléfono
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


    /**     MÉTODOS PARA EL REGISTRO DEL USUARIO     */

    fun registroWhitEmail(nombre: String, apellidos: String, email: String,
                          edad: Int, password: String,
                          onResult: (Boolean) -> Unit){


    }
}