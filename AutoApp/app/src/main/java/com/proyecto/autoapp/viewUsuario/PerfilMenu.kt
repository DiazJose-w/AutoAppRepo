package com.proyecto.autoapp.viewUsuario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Es el menú del usuario que se expande cuando pulsas en la foto de perfil
 * que se ve en la view inicial.
 * */
@Composable
fun PerfilMenu(onPerfil: () -> Unit, onHistorial: () -> Unit, onFavoritos: () -> Unit, onConfiguracion: () -> Unit, onLogout: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopStart
    ) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Abrir menú de perfil",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text("Mi perfil") },
                leadingIcon = { Icon(Icons.Default.Face, contentDescription = null) },
                onClick = { expanded = false; onPerfil() }
            )
            DropdownMenuItem(
                text = { Text("Historial") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                onClick = { expanded = false; onHistorial() }
            )
            DropdownMenuItem(
                text = { Text("Favoritos") },
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                onClick = { expanded = false; onFavoritos() }
            )
            DropdownMenuItem(
                text = { Text("Configuración") },
                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                onClick = { expanded = false; onConfiguracion() }
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 1.dp
            )

            DropdownMenuItem(
                text = { Text("Cerrar sesión") },
                leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) },
                onClick = {
                    expanded = false;
                    onLogout()
                }
            )
        }
    }
}
