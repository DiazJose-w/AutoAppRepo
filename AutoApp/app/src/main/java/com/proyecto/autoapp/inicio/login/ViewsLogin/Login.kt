package com.proyecto.autoapp.inicio.login.ViewsLogin

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.proyecto.autoapp.general.Rutas
import com.proyecto.autoapp.general.TopBarGeneral
import com.proyecto.autoapp.inicio.login.LoginVM

@Composable
fun Login(navController: NavController, loginVM: LoginVM) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading by loginVM.isLoading.collectAsState(initial = false)

    val ThumbUpPurple = Color(0xFF180038)
    val ThumbUpMustard = Color(0xFFF2C94C)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBarGeneral(
                "Login",
                onAccion = {
                    when (it) {
                        1 -> navController.popBackStack()
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(ThumbUpPurple),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(com.proyecto.autoapp.R.mipmap.camino_central),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .alpha(0.15f),
                contentScale = ContentScale.FillWidth
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bienvenido a ThumbUp",
                    color = ThumbUpMustard,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = ThumbUpMustard,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 1f),
                        focusedLabelColor = ThumbUpMustard,
                        unfocusedLabelColor = Color.White.copy(alpha = 1f),
                        cursorColor = ThumbUpMustard,
                        focusedTextColor = Color(0xFF111111),
                        unfocusedTextColor = Color(0xFF111111)
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = ThumbUpMustard,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.6f),
                        focusedLabelColor = ThumbUpMustard,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                        cursorColor = ThumbUpMustard,
                        focusedTextColor = Color(0xFF111111),
                        unfocusedTextColor = Color(0xFF111111)
                    )
                )

                Spacer(Modifier.height(16.dp))

                TextButton(
                    onClick = { navController.navigate(Rutas.TokenSMS) }
                ) {
                    Text(
                        "Recuperar contraseña",
                        color = ThumbUpMustard,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "No debe haber campos vacíos", Toast.LENGTH_SHORT).show()
                        } else {
                            loginVM.login(email, password) { ok ->
                                if (ok) {
                                    /** Pasamos a la página principal */
                                } else {
                                    Toast.makeText(context, "Login incorrecto", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThumbUpMustard,
                        contentColor = Color(0xFF1A1A1A)
                    )
                ) {
                    Text("Iniciar sesión", fontWeight = FontWeight.SemiBold)
                }
            }

            if (isLoading) {
                Dialog(onDismissRequest = { }) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(Color.White, shape = RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ThumbUpMustard)
                    }
                }
            }
        }
    }

}