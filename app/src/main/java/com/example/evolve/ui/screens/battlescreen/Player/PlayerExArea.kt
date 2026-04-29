package com.example.evolve.ui.screens.battlescreen.Player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.evolve.model.CardData
import com.example.evolve.ui.screens.exAreaHeightRatio
import com.example.evolve.ui.screens.exAreaWidthRatio
import com.example.evolve.ui.screens.exCardSpacingRatio
import com.example.evolve.ui.screens.exCardWidthRatio
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.input.pointer.pointerInput
import com.example.evolve.ui.viewmodel.BattleViewModel
import androidx.compose.runtime.getValue
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.example.evolve.ui.components.CardWithStats

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerEXArea(
    viewModel: BattleViewModel,
    modifier: Modifier = Modifier,
    exCards: List<CardData> = emptyList()
) {
    val screenWidth =
        LocalDensity.current.run { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val screenHeight =
        LocalDensity.current.run { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val density = LocalDensity.current
    val exCardWidth = with(density) { (screenWidth * exCardWidthRatio).toDp() }
    val exCardHeight = with(density) { (screenHeight * exAreaHeightRatio).toDp() }
    val exCardSpacing = with(density) { (screenWidth * exCardSpacingRatio).toDp() }
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth(exAreaWidthRatio)
            .height(exCardHeight),
        horizontalArrangement = Arrangement.spacedBy(exCardSpacing)
    ) {
        exCards.forEachIndexed { index, card ->
            Box(
                modifier = Modifier
                    .zIndex(100f)
                    .size(exCardWidth, exCardHeight)
                    .background(Color.Magenta.copy(alpha = 0.3f))
                    .combinedClickable(
                        onClick = {
                            viewModel.clearImageAndMenu()
                            viewModel.showImageFromCard(card)
                            viewModel.setSelectedExCardIndex(index)
                        },
                        onLongClick = {
                            viewModel.highlightExCard(index)
                            viewModel.showImageFromCard(card)
                        }
                    )
                    .pointerInput(card) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // ここでドラッグ処理を追加（追従画像など）
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val highlightIndex by viewModel.highlightExCardIndex.collectAsState()
                val selectedIndex by viewModel.selectedExCardIndex.collectAsState()
                if (highlightIndex == index || selectedIndex == index) {
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

                CardWithStats(
                    card = card,
                    modifier = Modifier.fillMaxSize(),
                    showAbilities = false
                )
            }
        }
    }
    val selectedIndex = viewModel.selectedExCardIndex.collectAsState().value

    if (selectedIndex != null && selectedIndex in exCards.indices) {
        val selectedCard = exCards[selectedIndex]

        val menuY = with(LocalDensity.current) {
            (LocalConfiguration.current.screenHeightDp.dp * 0.699f - exCardHeight - 8.dp).roundToPx()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, menuY) }
                .zIndex(200f)
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("1Play", "2bbb", "3ccc", "4ddd").forEach { label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                ) {
                    Button(
                        onClick = {
                            if (label == "1Play") {
                                viewModel.clearImageAndMenu()
                                viewModel.playCardFromExArea(selectedIndex)
                            }
                            viewModel.clearImageAndMenu()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xB14349FF)),
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
                            text = label,
                            color = Color.White,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

}
