package com.example.evolve.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text


@Composable
fun CardDetailScreen(expansion: String, cardId: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val imagePath = "file:///android_asset/images/$expansion/$cardId"

    LaunchedEffect(expansion, cardId) {
        val tempDeck = loadTempDeck(context)
        val tempCard = tempDeck.cards.find { it.card == cardId }

        val imageName = tempCard?.image ?: run {
            val deckCards = loadCardsFromJson(context, expansion)
            deckCards.find { it.card == cardId }?.image
        }

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        if (imagePath != null) {
            AsyncImage(
                model = imagePath,
                contentDescription = "Card Image",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "画像が見つかりません",
                color = Color.White
            )
        }
    }
}
