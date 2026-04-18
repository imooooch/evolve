package com.example.evolve.ui.screens.battlescreen.Opponent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.example.evolve.model.CardData
import com.example.evolve.ui.utils.loadCardImage
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun OpponentHandArea(
    modifier: Modifier = Modifier,
    cards: List<CardData>,
    cardWidthRatio: Float = 0.6f,
    cardHeightRatio: Float = 0.4f,
    maxRotation: Float = 20f,
    baseYRatio: Float = 0.25f,
    downwardFactorRatio: Float = 0.005f
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()
    val cardWidthPx = screenWidth * cardWidthRatio
    val cardHeightPx = screenHeight * cardHeightRatio
    val totalCards = cards.size
    if (totalCards == 0) return
    val spacingRatio = -0.35f - (totalCards * 0.0093f)
    val spacingPx = screenWidth * spacingRatio
    val baseRotation = if (totalCards > 1) maxRotation / (totalCards - 1) else 0f
    val baseY = screenHeight * baseYRatio

    data class CardInfo(val index: Int, val offsetX: Float, val offsetY: Float, val rotation: Float)
    val cardInfoList = cards.mapIndexed { index, _ ->
        val offsetFromCenter = index - (totalCards - 1) / 2f
        val offsetX = offsetFromCenter * (cardWidthPx + spacingPx)
        val offsetY = baseY + abs(offsetFromCenter) * (screenHeight * downwardFactorRatio)
        val rotation = offsetFromCenter * baseRotation
        CardInfo(index, offsetX, offsetY, rotation)
    }

    Box(
        modifier = modifier.graphicsLayer(rotationZ = 180f),
        contentAlignment = Alignment.TopCenter
    ) {
        cards.forEachIndexed { index, _ ->
            val info = cardInfoList[index]
            val centerX = screenWidth / 2f - 60f
            val offset = IntOffset(
                x = (centerX + info.offsetX - cardWidthPx / 2).roundToInt(),
                y = (baseY + info.offsetY - cardHeightPx / 2).roundToInt()  // 👈 baseY を使わない
            )
            Box(
                modifier = Modifier
                    .offset { offset }
                    .size(
                        with(density) { cardWidthPx.toDp() },
                        with(density) { cardHeightPx.toDp() }
                    )
                    .graphicsLayer {
                        rotationZ = info.rotation
                    }
                    .zIndex(index.toFloat())
            ) {
                Image(
                    bitmap = loadCardImage("images/battle/Back.jpg"),
                    contentDescription = "Opponent Hand Card",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}