package com.example.evolve.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LeaderInfo(modifier: Modifier = Modifier, health: Int) {
    Box(
        modifier = modifier
            .background(Color.Red)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "HP: $health",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
