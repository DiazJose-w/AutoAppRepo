package com.proyecto.autoapp.general

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarGeneral(titulo: String, onAccion: (Int) -> Unit) {
    val ThumbUpPurple = Color(0xFF180038)
    val ThumbUpMustard = Color(0xFFE09810)

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ThumbUpPurple,
            titleContentColor = ThumbUpMustard
        ),
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = titulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        actions = {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "cerrar página actual",
                tint = Color.White,
                modifier = Modifier
                    .width(24.dp)
                    .clickable { onAccion(1) }
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
    )
}