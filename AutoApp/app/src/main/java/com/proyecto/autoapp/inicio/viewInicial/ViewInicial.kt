package com.proyecto.autoapp.inicio.viewInicial

import com.proyecto.autoapp.R
import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.proyecto.autoapp.general.Rutas
import com.proyecto.autoapp.inicio.login.LoginVM
import com.proyecto.autoapp.ui.theme.*

@Composable
fun ViewInicial(navController: NavController, loginVM: LoginVM) {
    var TAG="Jose"
    var context = LocalContext.current

    // Launcher que recibe el resultado del intent de Google
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.result
                val idToken = account?.idToken

                if (idToken != null) {
                    loginVM.loginWithGoogle(idToken, { ok ->
                        if (ok) {
                            Toast.makeText(context, "Sesión iniciada", Toast.LENGTH_SHORT).show()
                            navController.navigate(Rutas.ViewViajero)
                        } else {
                            Toast.makeText(context, "No se pudo iniciar sesión", Toast.LENGTH_SHORT).show()
                        }
                    }) { uid ->
                        loginVM.uidActual= uid
                        Log.d("Jose", "UID del usuario: ${loginVM.uidActual}")
                    }
                } else {
                    Toast.makeText(context, "Error obteniendo token de Google", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Error iniciando con Google", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Inicio cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    // Esta función la llamas desde tu botón "Inicia con Google"
    fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut()
            .addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
    }

    /** ----------------------------------------------- */

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ThumbUpPurple
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(ThumbUpPurple)
        ) {
            Image(
                painter = painterResource(R.mipmap.camino_central),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .alpha(0.15f),
                contentScale = ContentScale.FillWidth
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.55f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.mipmap.logo_thumbsup),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(350.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            navController.navigate(Rutas.Login)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThumbUpMustard,
                            contentColor = ThumbUpPurple
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Tengo cuenta")
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            navController.navigate(Rutas.Registro)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ThumbUpMustard
                        ),
                        border = BorderStroke(2.dp, ThumbUpMustard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Regístrate")
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            launchGoogleSignIn()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ThumbUpMustard
                        ),
                        border = BorderStroke(2.dp, ThumbUpMustard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Entrar con Google")
                    }
                }
            }
        }
    }
}
