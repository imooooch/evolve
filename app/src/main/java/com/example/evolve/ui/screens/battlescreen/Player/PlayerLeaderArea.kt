package com.example.evolve.ui.screens.battlescreen.Player

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.example.evolve.ui.screens.loadBackgroundImage

@Composable
fun PlayerLeaderArea(modifier: Modifier = Modifier, hasCard: Boolean) {
    val leaderBitmap = loadBackgroundImage("images/battle/Leader.png")
    Box(modifier = modifier) {
        Image(
            bitmap = leaderBitmap,
            contentDescription = "Leader Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        if (hasCard) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // カード表示処理
            }
        }
    }
}