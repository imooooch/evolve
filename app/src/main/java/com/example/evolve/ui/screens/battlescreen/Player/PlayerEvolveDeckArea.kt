package com.example.evolve.ui.screens.battlescreen.Player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.example.evolve.model.CardData
import com.example.evolve.ui.screens.loadBackgroundImage
import com.example.evolve.ui.utils.loadCardImage
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import com.example.evolve.ui.screens.battlescreen.common.EvolveDeckListOverlay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerEvolveDeckArea(
    modifier: Modifier = Modifier,
    hasCard: Boolean,
    evolveCards: List<CardData> = emptyList()
) {
    var showOverlay by remember { mutableStateOf(false) }

    val evolveBitmap = loadBackgroundImage("images/battle/Evolve.png")

    if (evolveCards.any { it.isFaceUp }) {
        val topFaceUpCard = evolveCards.lastOrNull { it.isFaceUp }
        Box(
            modifier = modifier
                .fillMaxSize()
                .clickable { showOverlay = true }
        ) {
            if (topFaceUpCard != null) {
                Image(
                    bitmap = loadCardImage("images/${topFaceUpCard.expansion}/${topFaceUpCard.image}"),
                    contentDescription = topFaceUpCard.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    } else if (evolveCards.isNotEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clickable { showOverlay = true }
        ) {
            Image(
                bitmap = loadBackgroundImage("images/battle/Back.jpg"),
                contentDescription = evolveCards.last().name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
    } else {
        Box(modifier = modifier) {
            Image(
                bitmap = evolveBitmap,
                contentDescription = "Evolve Deck Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
    }

    if (showOverlay) {
        EvolveDeckListOverlay(
            cards = evolveCards,
            onDismiss = {
                showOverlay = false
            },
            emptyMessage = null
        )
    }
}