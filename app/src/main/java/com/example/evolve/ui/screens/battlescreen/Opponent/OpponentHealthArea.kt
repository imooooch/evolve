package com.example.evolve.ui.screens.battlescreen.Opponent

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun OpponentHealthArea(modifier: Modifier = Modifier, health: Int = 20) {
    Box(
        modifier = modifier.graphicsLayer(rotationZ = 180f), // 追加
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$health",
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}