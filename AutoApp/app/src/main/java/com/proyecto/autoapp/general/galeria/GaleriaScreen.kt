package com.proyecto.autoapp.general.galeria

import com.proyecto.autoapp.R
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import com.proyecto.autoapp.BuildConfig
import com.proyecto.autoapp.general.galeria.galeriaViewModel.GaleriaViewModel
import coil.compose.AsyncImage
import com.proyecto.autoapp.ui.theme.ThumbUpCard
import com.proyecto.autoapp.ui.theme.ThumbUpMustard
import com.proyecto.autoapp.ui.theme.ThumbUpPurple
import com.proyecto.autoapp.ui.theme.ThumbUpTextPrimary
import com.proyecto.autoapp.ui.theme.ThumbUpTextSecondary
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaleriaScreen(contexto: Context ,galeriaViewModel: GaleriaViewModel) {

    // LiveData -> State (tal cual tenías)
    val imageUri by galeriaViewModel.imageUri.observeAsState(Uri.EMPTY)
    val imageFile by galeriaViewModel.imageFile.observeAsState(null)

    // --------- LAUNCHERS QUE YA TENÍAS ---------

    // Lanzador de cámara
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageFile?.let { file ->
                    galeriaViewModel.updateImageUri(Uri.fromFile(file))
                }
            } else {
                galeriaViewModel.updateImageUri(Uri.EMPTY)
            }
        }

    // Permiso de cámara
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val file = File.createTempFile("pfp", ".jpg", contexto.cacheDir)
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
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                galeriaViewModel.updateImageUri(it)
            }
        }

    // --------- UI ---------
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Galería",
                        color = ThumbUpTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // Aquí pondrás la lógica de VOLVER.
                            // Por ejemplo: navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = ThumbUpTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ThumbUpPurple
                )
            )
        },
        containerColor = ThumbUpPurple
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            // ---- CARD SUPERIOR: FOTO ACTUAL + BOTONES ----
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ThumbUpCard),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // FOTO ACTUAL (usa tu imageUri + AsyncImage si existe, si no placeholder)
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
                                tint = ThumbUpMustard,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Foto de perfil",
                        color = ThumbUpTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Gestiona tus fotos y elige cuál se mostrará a otros usuarios.",
                        color = ThumbUpTextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón CÁMARA
                        OutlinedButton(
                            onClick = {
                                // ABRIR CÁMARA usando el PERMISSION LAUNCHER que ya tienes
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, ThumbUpMustard),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Cámara")
                        }

                        // Botón GALERÍA
                        Button(
                            onClick = {
                                // ABRIR SELECTOR DE GALERÍA usando tu GALLERY LAUNCHER
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ThumbUpMustard,
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

            // ---- TÍTULO SECCIÓN GALERÍA ----
            Text(
                text = "Tus fotos guardadas",
                color = ThumbUpTextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))

            // Lista de ejemplo SOLO para diseño.
            // Más adelante aquí meterás las fotos guardadas del usuario.
            val fakeImages = List(9) { it }

            if (fakeImages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cuando subas fotos, aparecerán aquí.",
                        color = ThumbUpTextSecondary
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
                    items(fakeImages.size) { _ ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ThumbUpCard),
                            contentAlignment = Alignment.Center
                        ) {
                            // Aquí luego pondrás la miniatura real (AsyncImage con URL/uri)
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = ThumbUpMustard,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

