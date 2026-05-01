package com.example.evolve.ui.screens.battlescreen.Opponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import com.example.evolve.model.CardData

private const val cardWidthRatio = 0.12f
private const val cardHeightRatio = 0.08f
private const val horizontalSpacingRatio = 0.1f
private const val verticalSpacingRatio = 0.01f

@Composable
fun OpponentFieldArea(
    modifier: Modifier = Modifier,
    screenWidth: Float,
    screenHeight: Float,
    fieldCards: List<CardData>
) {
    val density = LocalDensity.current
    val cardWidth = with(density) { (screenWidth * cardWidthRatio).toDp() }
    val cardHeight = with(density) { (screenHeight * cardHeightRatio).toDp() }
    val horizontalSpacing = with(density) { (screenWidth * horizontalSpacingRatio).toDp() }
    val verticalSpacing = with(density) { (screenHeight * verticalSpacingRatio).toDp() }

    Column(
        modifier = modifier.graphicsLayer(rotationZ = 180f),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)) {
            repeat(3) { index ->
                val card = fieldCards.getOrNull(index)
                Box(
                    modifier = Modifier
                        .size(cardWidth, cardHeight)
                        .background(Color.White.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    // 後で CardWithStats に置き換える
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)) {
            repeat(2) { index ->
                val card = fieldCards.getOrNull(index + 3)
                Box(
                    modifier = Modifier
                        .size(cardWidth, cardHeight)
                        .background(Color.LightGray.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                }
            }
        }
    }
}