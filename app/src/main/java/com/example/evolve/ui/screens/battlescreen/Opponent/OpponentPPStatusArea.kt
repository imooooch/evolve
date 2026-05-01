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
fun OpponentPPStatusArea(
    modifier: Modifier = Modifier,
    currentPP: Int,
    maxPP: Int
) {
    Box(
        modifier = modifier.graphicsLayer(rotationZ = 180f), // 追加
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$currentPP / $maxPP",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}