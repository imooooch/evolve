package com.example.evolve.ui.screens.battlescreen.Player

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.example.evolve.ui.screens.loadBackgroundImage

@Composable
fun PlayerDeckArea(modifier: Modifier = Modifier, count: Int) {
    val deckBackBitmap = loadBackgroundImage("images/battle/Back.jpg")
    val deckBitmap = loadBackgroundImage("images/battle/Deck.png")
    Box(modifier = modifier) {
        Image(
            bitmap = if (count > 0) deckBackBitmap else deckBitmap,
            contentDescription = "Player Deck Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}