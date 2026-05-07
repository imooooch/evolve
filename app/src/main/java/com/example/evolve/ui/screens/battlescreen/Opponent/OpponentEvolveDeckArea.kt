package com.example.evolve.ui.screens.battlescreen.Opponent

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import com.example.evolve.model.CardData
import com.example.evolve.ui.screens.battlescreen.common.EvolveDeckListOverlay
import com.example.evolve.ui.utils.loadCardImage

@Composable
fun OpponentEvolveDeckArea(
    modifier: Modifier = Modifier,
    hasCard: Boolean,
    evolveCards: List<CardData> = emptyList()
) {
    val faceUpCards = evolveCards.filter { it.isFaceUp }
    val topFaceUpCard = faceUpCards.lastOrNull()
    var showList by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .graphicsLayer(rotationZ = 180f)
            .clickable {
                showList = true
            }
    ) {
        if (hasCard) {
            Image(
                bitmap = loadCardImage(
                    if (topFaceUpCard != null) {
                        "images/${topFaceUpCard.expansion}/${topFaceUpCard.image}"
                    } else {
                        "images/battle/Back.jpg"
                    }
                ),
                contentDescription = "Opponent Evolve Deck",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showList) {
        EvolveDeckListOverlay(
            cards = faceUpCards,
            onDismiss = { showList = false },
            emptyMessage = "表向きのカードがありません"
        )
    }
}