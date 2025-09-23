package com.proyecto.autoapp.inicio.viewInicial

import com.proyecto.autoapp.R
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.proyecto.autoapp.general.Rutas
import com.proyecto.autoapp.inicio.login.LoginVM

@Composable
fun ViewInicial(navController: NavController, loginVM: LoginVM) {
    var context = LocalContext.current

    /**     Disparadores para entrar con cuenta google     */
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.result
            val idToken = account?.idToken
            if (idToken != null) {
                loginVM.loginWithGoogle(idToken)
                /**     CREAR PÁGINA DE INICIO Y NAVEGAR DESDE AQUÍ
                 *  Recuerda cerrar sesión con:
                 *
                 *                          loginVM.signOut(context)
                                                navController.navigate(Rutas.ViewInicial){
                                                   popUpTo(Rutas.Login){inclusive = true}
                                            }
                 * */
                Toast.makeText(context, "Sesión iniciada", Toast.LENGTH_SHORT).show()
                navController.navigate(Rutas.Registro) // Esta la tengo de ejemplo, para poder cerrar sesión después
            } else {
                Toast.makeText(context, "Error obteniendo token de Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }
    /***/

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(48.dp))

                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "ThumbUp",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("¿Tienes cuenta?")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        navController.navigate(Rutas.Registro)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Regístrate")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        launchGoogleSignIn()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF303F9F)
                    )
                ) {
                    Text("Entrar con Google")
                }
            }
        }
    }
}