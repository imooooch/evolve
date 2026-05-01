package com.example.evolve.ui.screens.battlescreen.Player

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.example.evolve.model.CardData
import com.example.evolve.ui.viewmodel.BattleViewModel
import com.example.evolve.model.CardInfo
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.ui.draw.alpha
import com.example.evolve.ui.utils.loadCardImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import com.example.evolve.battle.ImageDisplaySide

@Composable
fun PlayerHandArea(
    modifier: Modifier = Modifier,
    cards: List<CardData>,
    viewModel: BattleViewModel,
    cardWidthRatio: Float = 0.6f,
    cardHeightRatio: Float = 0.4f,
    maxRotation: Float = 20f,
    baseYRatio: Float = 0.1f,
    downwardFactorRatio: Float = 0.005f,
    onCardPlayed: (Int) -> Unit = {} // ✅ CardData → Int に変更
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()
    val cardWidthPx = screenWidth * cardWidthRatio
    val cardHeightPx = screenHeight * cardHeightRatio
    val baseY = screenHeight * baseYRatio
    val totalCards = cards.size
    if (totalCards == 0) return

    val spacingRatio = -0.35f - (totalCards * 0.0093f)
    val spacingPx = screenWidth * spacingRatio
    val baseRotation = if (totalCards > 1) maxRotation / (totalCards - 1) else 0f
    val dragOffset = remember { mutableStateOf(Offset.Zero) }
    val dragStartOffsetY = remember { mutableStateOf(0f) }
    val isDragging = remember { mutableStateOf(false) }

    val centerX = screenWidth / 2f
    val cardInfoList = cards.mapIndexed { index, card ->
        val offsetFromCenter = index - (totalCards - 1) / 2f
        val offsetX = offsetFromCenter * (cardWidthPx + spacingPx)
        val offsetY = abs(offsetFromCenter) * (screenHeight * downwardFactorRatio)
        val rotation = offsetFromCenter * baseRotation
        CardInfo(
            card = card,
            index = index,
            offsetX = offsetX,
            offsetY = offsetY,
            rotation = rotation
        )
    }
    val selectedHandIndex by viewModel.selectedHandIndex.collectAsState()

    LaunchedEffect(isDragging.value, selectedHandIndex, cards.size) {
        val validIndex = selectedHandIndex

        if (validIndex != null && validIndex in cards.indices) {
            viewModel.showImageFromCardOnSide(
                cards[validIndex],
                ImageDisplaySide.Top
            )
        } else {
            viewModel.clearImageAndMenu()
        }
    }
        val touchAreaHeight = if (isDragging.value) screenHeight else screenHeight * 0.2f
        Box(
            modifier = modifier
                .fillMaxWidth()
                .zIndex(0f)
                .height(with(density) { touchAreaHeight.toDp() }),
            contentAlignment = Alignment.BottomCenter
        ) {

            cards.withIndex().forEach { (index, card) ->
                val info = cardInfoList[index]
                val offset = IntOffset(
                    x = (info.offsetX).roundToInt(),
                    y = (baseY + info.offsetY - cardHeightPx / 2).roundToInt()
                )

                Box(
                    modifier = Modifier
                        .offset { offset }
                        .size(
                            with(density) { cardWidthPx.toDp() },
                            with(density) { cardHeightPx.toDp() }
                        )
                        .graphicsLayer { rotationZ = info.rotation }
                        .alpha(if (isDragging.value && index == selectedHandIndex) 0f else 1f)
                        .zIndex(if (isDragging.value && index == selectedHandIndex) 50f else index.toFloat())
                        .pointerInput(card.takeIf { !isDragging.value }) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val down = event.changes.firstOrNull() ?: continue

                                    if (down.pressed) {
                                        viewModel.clearHighlight()
                                        viewModel.clearHighlightExCard()
                                        viewModel.clearSelectedCardIndex()
                                        viewModel.clearSelectedExCardIndex()
                                        viewModel.selectHandCard(index)
                                        Log.d("選択状態", "選択状態：${card.name}")

                                        val cardCenterY = baseY + info.offsetY
                                        dragStartOffsetY.value = down.position.y - cardCenterY
                                        dragOffset.value = down.position

                                        val dragThreshold = with(density) { 8.dp.toPx() }
                                        var hasDragged = false
                                        dragStartOffsetY.value = down.position.y - cardCenterY
                                        dragOffset.value = down.position

                                        while (down.pressed) {
                                            val moveEvent = awaitPointerEvent()
                                            val move = moveEvent.changes.first()
                                            val delta = move.position - down.position

                                            if (!hasDragged && delta.getDistance() > dragThreshold) {
                                                isDragging.value = true
                                                hasDragged = true
                                            }

                                            if (hasDragged) {
                                                dragOffset.value = move.position
                                            }

                                            if (move.changedToUp()) {
                                                val currentSelectedIndex = selectedHandIndex

                                                if (
                                                    dragOffset.value.y / screenHeight < -0.35f &&
                                                    currentSelectedIndex != null &&
                                                    currentSelectedIndex in cards.indices
                                                ) {
                                                    Log.d(
                                                        "プレイ判定",
                                                        "プレイ：${cards[currentSelectedIndex].name}"
                                                    )
                                                    onCardPlayed(currentSelectedIndex)
                                                }
                                                break
                                            }
                                        }
                                        isDragging.value = false
                                        Log.d("選択状態", "選択解除：${card.name}")
                                        viewModel.clearSelectedCard()
                                        viewModel.clearImageAndMenu()
                                    }
                                }
                            }
                        }
                ) {
                    val highlightIndex by viewModel.highlightCardIndex.collectAsState()
                    if (highlightIndex == index || selectedHandIndex == index) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .zIndex(10f)
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                )
                        )
                    }

                    Image(
                        bitmap = loadCardImage("images/${card.expansion}/${card.image}"),
                        contentDescription = card.name,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                }

            }
            val draggingIndex = selectedHandIndex

            if (
                isDragging.value &&
                draggingIndex != null &&
                draggingIndex in cards.indices &&
                draggingIndex in cardInfoList.indices
            ) {
                val draggedCard = cards[draggingIndex]
                val draggedInfo = cardInfoList[draggingIndex]

                val dragX = dragOffset.value.x.coerceIn(-2050f, screenWidth + 2050f)
                val dragY = dragOffset.value.y.coerceIn(-2050f, screenHeight + 2050f)

                val yDeltaRatio = ((baseY - dragY).coerceAtLeast(0f) / screenHeight)
                val correctionRatio = 0.55f
                val xCorrection = draggedInfo.offsetX * yDeltaRatio * correctionRatio

                val offset = IntOffset(
                    x = (dragX + draggedInfo.offsetX + xCorrection - cardWidthPx / 2).roundToInt(),
                    y = (dragY - dragStartOffsetY.value - cardHeightPx / 2).roundToInt()
                )

                key(draggingIndex) {
                    Box(
                        modifier = Modifier
                            .offset { offset }
                            .size(
                                with(density) { cardWidthPx.toDp() },
                                with(density) { cardHeightPx.toDp() }
                            )
                            .zIndex(50f)
                    ) {
                        Image(
                            bitmap = loadCardImage("images/${draggedCard.expansion}/${draggedCard.image}"),
                            contentDescription = draggedCard.name,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
