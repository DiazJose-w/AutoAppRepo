package com.proyecto.autoapp.general.galeria

import com.proyecto.autoapp.R
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.proyecto.autoapp.BuildConfig
import com.proyecto.autoapp.general.galeria.galeriaViewModel.GaleriaViewModel
import coil.compose.AsyncImage
import android.os.Environment
import android.widget.Toast
import com.proyecto.autoapp.inicio.login.LoginVM
import com.proyecto.autoapp.ui.theme.*
import com.proyecto.autoapp.viewUsuario.perfilVM.PerfilVM
import java.io.File
import androidx.compose.runtime.collectAsState
import androidx.core.net.toUri


/***
 *
 *  ESTA VIEW ESTÁ MAL ESTRUCTURADA AHORA MISMO:
 *      Lo que hace es mostrar una ventana donde se puede elegir cambiar la foto de perfil. NO.
 *      Lo que debe de hacer es contener la gestión del perfil del usuario. Es decir, que el usuario pueda subir fotos suyas, como
 *     si de una red social se tratara, para que usuarios afines (Funcionalidad de futuro), puedan verlas.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaleriaScreen(contexto: Context, galeriaViewModel: GaleriaViewModel, navController: NavController,  loginVM: LoginVM, perfilVM: PerfilVM) {
    var TAG = "Jose"
    val imageUri by galeriaViewModel.imageUri.observeAsState(Uri.EMPTY)
    val imageFile by galeriaViewModel.imageFile.observeAsState(null)
    val listaFotosPerfil by galeriaViewModel.fotosPerfil.observeAsState(emptyList())
    val hayCambiosPendientes by galeriaViewModel.hayCambiosPendientes.observeAsState(false)

    val usuarioActual by loginVM.uidActual.collectAsState()
    var fotoPerfilUrl by remember { mutableStateOf<String?>(null) }

    /**
     * Permisos de cámara o galería
     * */
    // Lanzador de cámara
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageFile?.let { file ->
                    galeriaViewModel.updateImageUri(Uri.fromFile(file))
                    galeriaViewModel.marcarCambiosPendientes()
                }
            } else {
                galeriaViewModel.updateImageUri(Uri.EMPTY)
            }
        }

    // Permiso de cámara
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val file = File.createTempFile("pfp", ".jpg", contexto.getExternalFilesDir(Environment.DIRECTORY_PICTURES))
                galeriaViewModel.setImageFile(file)

                val uri = FileProvider.getUriForFile(
                    contexto,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file
                )
                cameraLauncher.launch(uri)
            }
        }

    // Selector de galería
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            //mainViewModel.updateImageUri(it)
            //Copiamos el contenido de la Uri en un archivo temporal
            val inputStream = contexto.contentResolver.openInputStream(it)
            val tempFile = File.createTempFile("GAL_", ".jpg", contexto.cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }

            //Actualizamos el ViewModel con el archivo y la Uri
            galeriaViewModel.updateImageUri(it) //Actualiza la Uri para mostrarla en la UI
            galeriaViewModel.setImageFile(tempFile) //Actualiza el archivo para la carga
            galeriaViewModel.marcarCambiosPendientes()
        }
    }

    // Carga la imagen según el usuario que esté logeado
    LaunchedEffect(usuarioActual) {
        if (usuarioActual.isNotBlank()) {
            perfilVM.cargarFotoPerfil(usuarioActual) { url ->
                fotoPerfilUrl = url
                if (url.isNotBlank()) {
                    galeriaViewModel.updateImageUri(url.toUri())
                } else {
                    galeriaViewModel.updateImageUri(Uri.EMPTY)
                }
            }
        } else {
            Log.e(TAG, "Hubo un problema al cargar el usuario actual")
        }
    }


    var mostrarDialogoSalida by remember { mutableStateOf(false) }

    DialogoConfirmacionThumbsUp(
        visible = mostrarDialogoSalida,
        onGuardarYSalir = {
            galeriaViewModel.uploadImage(contexto) { exito ->
                if (exito) {
                    mostrarDialogoSalida = false
                    navController.popBackStack()
                } else {
                    Toast.makeText(contexto, "Error subiendo imagen", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onSalirSinGuardar = {
            galeriaViewModel.descartarCambios()
            mostrarDialogoSalida = false
            navController.popBackStack()
        },
        onDismiss = {
            mostrarDialogoSalida = false
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Galería",
                        color = ThumbsUpTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hayCambiosPendientes) {
                                mostrarDialogoSalida = true
                            } else {
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = ThumbsUpTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ThumbsUpPurple
                )
            )
        },
        containerColor = ThumbsUpPurple
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            /**
             * Card superior. Foto de perfil más botones
             * */
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ThumbsUpCard),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (imageUri != Uri.EMPTY) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = contexto.getString(R.string.app_name),
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Foto de perfil",
                                tint = ThumbsUpMustard,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Foto de perfil",
                        color = ThumbsUpTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Gestiona tus fotos y elige cuál se mostrará a otros usuarios.",
                        color = ThumbsUpTextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        /**
                         * Botón de la cámara
                         * */
                        OutlinedButton(
                            onClick = {
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, ThumbsUpMustard),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Cámara")
                        }

                        /**
                         * Botón de la galería
                         * */
                        Button(
                            onClick = {
                                // ABRIR SELECTOR DE GALERÍA usando tu GALLERY LAUNCHER
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ThumbsUpMustard,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Galería")
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Tus fotos guardadas",
                color = ThumbsUpTextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))
            /**
             * Lista de imágenes guardadas en la galería
             *
             * Esto lo dejaremos para más adelante
             * */

            if (listaFotosPerfil.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cuando subas fotos, aparecerán aquí.",
                        color = ThumbsUpTextSecondary
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp)
                ) {
                    items(listaFotosPerfil.size) { index ->
                        val img = listaFotosPerfil[index]

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ThumbsUpCard),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = img,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.ic_launcher_foreground), // opcional
                                error = painterResource(id = R.drawable.ic_launcher_foreground)        // opcional
                            )
                        }
                    }
                }
            }
        }
    }
}

