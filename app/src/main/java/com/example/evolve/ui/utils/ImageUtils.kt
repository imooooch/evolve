package com.example.evolve.ui.utils

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

@Composable
fun loadCardImage(imagePath: String): ImageBitmap {
    val context = LocalContext.current
    return remember(imagePath) {
        context.assets.open(imagePath).use {
            BitmapFactory.decodeStream(it).asImageBitmap()
        }
    }
}
