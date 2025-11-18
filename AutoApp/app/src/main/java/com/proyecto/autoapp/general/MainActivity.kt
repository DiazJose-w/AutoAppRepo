package com.proyecto.autoapp.general

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.proyecto.autoapp.R
import com.proyecto.autoapp.general.galeria.GaleriaScreen
import com.proyecto.autoapp.general.galeria.galeriaViewModel.GaleriaViewModel
import com.proyecto.autoapp.general.maps.MapViewModel
import com.proyecto.autoapp.inicio.login.ViewsLogin.Login
import com.proyecto.autoapp.inicio.login.LoginVM
import com.proyecto.autoapp.inicio.login.ViewsLogin.TokenSMS
import com.proyecto.autoapp.inicio.registro.viewsRegistro.Registro
import com.proyecto.autoapp.inicio.registro.RegistroVM
import com.proyecto.autoapp.inicio.viewInicial.ViewInicial
import com.proyecto.autoapp.ui.theme.AutoAppTheme
import com.proyecto.autoapp.viewUsuario.PerfilUsuario
import com.proyecto.autoapp.viewUsuario.ViewInicialUsuario
import com.proyecto.autoapp.viewUsuario.conductor.ViewConductor
import com.proyecto.autoapp.viewUsuario.perfilVM.PerfilVM

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        val placesClient: PlacesClient = Places.createClient(this)

        var loginVM = LoginVM()
        var registroVM = RegistroVM()
        var mapViewModel = MapViewModel()
        var perfilVM = PerfilVM()
        var galeriaViewModel = GaleriaViewModel()

        mapViewModel.setPlacesClient(placesClient)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoAppTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                NavHost(navController, Rutas.ViewInicial) {

                    /**
                     * Views inicial APP
                     */
                    composable(Rutas.ViewInicial) {
                        ViewInicial(navController, loginVM)
                    }
                    composable(Rutas.Login) {
                        Login(navController, loginVM)
                    }
                    composable(Rutas.Registro) {
                        Registro(navController, registroVM, loginVM)
                    }
                    composable(Rutas.TokenSMS){
                        TokenSMS(navController, loginVM)
                    }

                    /**
                     * Views inicio usuario
                     */
                    composable(Rutas.ViewUsuario){
                        ViewInicialUsuario(mapViewModel, loginVM, navController, perfilVM)
                    }

                    /**
                     * Views perfil y correspondientes al perfil del usuario
                     */
                    composable (Rutas.Perfil){
                        PerfilUsuario(perfilVM, navController, loginVM)
                    }
                    composable (Rutas.Galeria){
                        GaleriaScreen(this@MainActivity, galeriaViewModel, navController, loginVM, perfilVM)
                    }
                    composable(Rutas.ViewConductor) {
                        ViewConductor(mapViewModel, navController, loginVM, perfilVM)
                    }
                }
            }
        }
    }

}