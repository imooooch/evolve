package com.example.evolve.ui.screens.battlescreen.Opponent

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.evolve.model.CardData
import com.example.evolve.ui.components.CardWithStats
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.evolve.battle.ImageDisplaySide
import com.example.evolve.ui.viewmodel.BattleViewModel

private const val cardWidthRatio = 0.12f
private const val cardHeightRatio = 0.08f
private const val horizontalSpacingRatio = 0.1f
private const val verticalSpacingRatio = 0.01f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OpponentFieldArea(
    modifier: Modifier = Modifier,
    screenWidth: Float,
    screenHeight: Float,
    fieldCards: List<CardData>,
    viewModel: BattleViewModel
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
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                if (card != null) {
                                    Log.d("長押し", "OpponentFieldArea 長押し：${card.name}")
                                    viewModel.showImageFromCardOnSide(
                                        card,
                                        ImageDisplaySide.Bottom
                                    )
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (card != null) {
                        val isActed = card.isActed
                        val actOffsetY = (cardHeight - cardWidth) / 2

                        CardWithStats(
                            card = card,
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(y = if (isActed) actOffsetY else 0.dp)
                                .graphicsLayer {
                                    rotationZ = if (isActed) 90f else 0f
                                },
                            showAbilities = true
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)) {
            repeat(2) { index ->
                val card = fieldCards.getOrNull(index + 3)
                Box(
                    modifier = Modifier
                        .size(cardWidth, cardHeight)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                if (card != null) {
                                    Log.d("長押し", "OpponentFieldArea 長押し：${card.name}")
                                    viewModel.showImageFromCardOnSide(
                                        card,
                                        ImageDisplaySide.Bottom
                                    )
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (card != null) {
                        val isActed = card.isActed
                        val actOffsetY = (cardHeight - cardWidth) / 2

                        CardWithStats(
                            card = card,
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(y = if (isActed) actOffsetY else 0.dp)
                                .graphicsLayer {
                                    rotationZ = if (isActed) 90f else 0f
                                },
                            showAbilities = true
                        )
                    }
                }
            }
        }
    }
}