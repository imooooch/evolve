package com.example.evolve.ui.screens.battlescreen.Player

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.evolve.model.CardData
import com.example.evolve.ui.screens.cardHeightRatio
import com.example.evolve.ui.screens.cardWidthRatio
import com.example.evolve.ui.screens.horizontalSpacingRatio
import com.example.evolve.ui.screens.verticalSpacingRatio
import com.example.evolve.ui.utils.loadCardImage
import com.example.evolve.ui.viewmodel.BattleViewModel
import kotlin.math.roundToInt
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.example.evolve.ui.components.CardWithStats

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FieldCardSlot(
    index: Int,
    currentCard: CardData?,
    cardWidth: androidx.compose.ui.unit.Dp,
    cardHeight: androidx.compose.ui.unit.Dp,
    isImageShown: Boolean,
    selectedCardIndex: Int?, // ← Int? に変更
    viewModel: BattleViewModel,
    onClickCard: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    fun logFieldCardInfo(card: CardData?) {
        if (card == null) {
            Log.d("FieldCardCheck", "カードなし")
            return
        }

        Log.d(
            "FieldCardCheck",
            """
        表示カード:
        card=${card.card}
        name=${card.name}
        cost=${card.cost}
        kind=${card.kind}
        isEvolved=${card.isEvolved}

        originalCard:
        card=${card.originalCard?.card}
        name=${card.originalCard?.name}
        cost=${card.originalCard?.cost}
        kind=${card.originalCard?.kind}
        """.trimIndent()
        )
    }
    Box(
        modifier = Modifier

            .size(cardWidth, cardHeight)
            .graphicsLayer { rotationZ = currentCard?.rotation ?: 0f }
            .combinedClickable(
                        onClick = {
                    coroutineScope.launch {
                        viewModel.clearImageAndMenu()
                        onClickCard()

                        if (currentCard != null) {
                            logFieldCardInfo(currentCard)

                            Log.d("クリック", "PlayerFieldArea ${index + 1}番 クリック：${currentCard.name}")
                            viewModel.showImageFromCard(currentCard)
                        }
                    }
                },
                onLongClick = {
                    if (currentCard != null) {
                        logFieldCardInfo(currentCard)

                        viewModel.highlightFieldCard(index)
                        Log.d("長押し", "PlayerFieldArea 長押し：${currentCard.name}")
                        viewModel.showImageFromCard(currentCard)
                    }
                }
            )
    ) {
        currentCard?.let { card ->
            val highlightIndex by viewModel.highlightFieldCardIndex.collectAsState()

            if (selectedCardIndex == index || highlightIndex == index) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .zIndex(10f)
                        .border(1.dp, Color.White.copy(alpha = 0.8f))
                )
            }

            CardWithStats(
                card = card,
                modifier = Modifier.fillMaxSize(),
                showAbilities = true
            )
        }
    }
}

@Composable
fun PlayerFieldArea(
    modifier: Modifier = Modifier,
    screenWidth: Float,
    screenHeight: Float,
    fieldCards: List<CardData>,
    viewModel: BattleViewModel
) {
    val tapEffectOffset by viewModel.tapEffectOffset.collectAsState()
    val selectedImagePath by viewModel.selectedImagePath.collectAsState()
    val selectedCardIndex by viewModel.selectedCardIndex.collectAsState()

    tapEffectOffset?.let { position ->
        Log.d("エフェクト座標", "表示位置: x=${position.x}, y=${position.y}")
        Box(
            modifier = Modifier.fillMaxSize().zIndex(100f)
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                    .size(40.dp)
                    .background(Color.Red.copy(alpha = 0.5f), shape = RectangleShape)
            )
        }
    }

    val density = LocalDensity.current
    val cardWidth = with(density) { (screenWidth * cardWidthRatio).toDp() }
    val cardHeight = with(density) { (screenHeight * cardHeightRatio).toDp() }
    val horizontalSpacing = with(density) { (screenWidth * horizontalSpacingRatio).toDp() }
    val verticalSpacing = with(density) { (screenHeight * verticalSpacingRatio).toDp() }
    val isImageShown = selectedImagePath.isNotEmpty()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier.fillMaxSize().pointerInput(isImageShown) {
            detectTapGestures {
                if (isImageShown) {
                    Log.d("タップ", "拡大画像とメニューを非表示（全画面タップ）")
                    viewModel.clearImageAndMenu()
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)) {
                for (i in 0..2) {
                    FieldCardSlot(i, fieldCards.getOrNull(i), cardWidth, cardHeight, isImageShown, selectedCardIndex, viewModel) {
                        viewModel.setSelectedCardIndex(i)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)) {
                for (i in 3..4) {
                    FieldCardSlot(i, fieldCards.getOrNull(i), cardWidth, cardHeight, isImageShown, selectedCardIndex, viewModel) {
                        viewModel.setSelectedCardIndex(i)
                    }
                }
            }
        }

        selectedCardIndex?.let { fixedIndex ->
            if (fixedIndex in fieldCards.indices) {
                val row = if (fixedIndex < 3) 0 else 1
                val col = if (fixedIndex < 3) fixedIndex else fixedIndex - 3

                val cardBoxStartY =
                    (if (row == 0) 0f else (cardHeight + verticalSpacing).value - 240f).roundToInt()

                Row(
                    modifier = Modifier
                        .zIndex(200f)
                        .fillMaxWidth()
                        .offset { IntOffset(0, cardBoxStartY + cardHeight.roundToPx()) }
                        .zIndex(350f)
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("2Grave", "Evolve", "1Attack", "4toEX").forEach { label ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                        ) {
                            Button(
                                onClick = {
                                    Log.d("メニュー", "${fieldCards[fixedIndex].name} ${label} が押されました")

                                    when (label) {
                                        "1Attack" -> {
                                            viewModel.rotateFieldCardRight(fixedIndex)
                                            viewModel.setFieldCardAct(fixedIndex, true)
                                        }

                                        "2Grave" -> viewModel.moveFieldCardToGrave(fixedIndex)

                                        "3Banish" -> viewModel.moveFieldCardToBanish(fixedIndex)

                                        "4toEX" -> viewModel.moveFieldCardToEX(fixedIndex)

                                        "Evolve" -> {
                                            val selectedCard = fieldCards.getOrNull(fixedIndex)
                                            val evolveId = selectedCard?.evolve

                                            if (!evolveId.isNullOrBlank() && selectedCard != null) {
                                                Log.d(
                                                    "Evolve",
                                                    "選択カード: ${selectedCard.card} → 進化先: $evolveId"
                                                )
                                                val evolveCard = viewModel.playerEvolveDeck.value
                                                    ?.firstOrNull {
                                                        it.card == evolveId &&
                                                                !it.isFaceUp
                                                    }

                                                if (evolveCard != null) {
                                                    viewModel.evolveCard(
                                                        fixedIndex,
                                                        selectedCard,
                                                        evolveCard,
                                                        selectedCard
                                                    )
                                                } else {
                                                    Log.d("Evolve", "使用可能な裏向き進化カードが存在しません")
                                                }
                                            } else {
                                                Log.d("Evolve", "進化先IDが設定されていません")
                                            }
                                        }
                                    }

                                    coroutineScope.launch {
                                        delay(100)
                                        viewModel.clearImageAndMenu()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xB14349FF)
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .size(width = 95.dp, height = 46.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.8f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                                    ),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text(
                                    textAlign = TextAlign.Start,
                                    text = label,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
