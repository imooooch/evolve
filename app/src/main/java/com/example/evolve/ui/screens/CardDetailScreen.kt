package com.example.evolve.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.evolve.data.CardRepository
import kotlinx.coroutines.delay

@Composable
fun CardDetailScreen(
    expansion: String,
    cardId: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { CardRepository(context) }
    var imagePath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(expansion, cardId) {
        imagePath = null

        val card = repository.getCardById(expansion, cardId)
        val imageName = card?.image

        if (!imageName.isNullOrBlank()) {
            delay(10)
            imagePath = "file:///android_asset/images/$expansion/$imageName"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        val currentPath = imagePath
        if (currentPath != null) {
            AsyncImage(
                model = currentPath,
                contentDescription = "Card Image",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "画像を読み込み中...",
                color = Color.White
            )
        }
    }
}