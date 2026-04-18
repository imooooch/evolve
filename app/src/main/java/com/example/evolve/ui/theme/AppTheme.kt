package com.example.evolve.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// アプリ全体のテーマ設定
private val AppColorScheme = lightColorScheme(
        primary = Color(0xFF6200EA),
        secondary = Color(0xFF03DAC5),
        background = Color(0xFF1E1E1E),
        surface = Color(0xFF2C2C2C),
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
        MaterialTheme(
                colorScheme = AppColorScheme,
                typography = Typography,
                shapes = Shapes,
                content = content
        )
}
