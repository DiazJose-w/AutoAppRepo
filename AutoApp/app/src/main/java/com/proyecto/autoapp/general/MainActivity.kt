package com.proyecto.autoapp.general

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.proyecto.autoapp.inicio.login.Login
import com.proyecto.autoapp.inicio.login.LoginVM
import com.proyecto.autoapp.inicio.registro.Registro
import com.proyecto.autoapp.inicio.registro.RegistroVM
import com.proyecto.autoapp.inicio.viewInicial.ViewInicial
import com.proyecto.autoapp.ui.theme.AutoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var loginVM = LoginVM()
        var registroVM = RegistroVM()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoAppTheme {
                val navController = rememberNavController()
                NavHost(navController, Rutas.ViewInicial) {
                    composable(Rutas.ViewInicial) {
                        ViewInicial(navController)
                    }
                    composable(Rutas.Login) {
                        Login(navController, loginVM)
                    }
                    composable(Rutas.Registro) {
                        Registro(navController, registroVM)
                    }
                }
            }
        }
    }
}