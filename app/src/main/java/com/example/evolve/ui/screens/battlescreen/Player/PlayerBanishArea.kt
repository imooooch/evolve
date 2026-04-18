package com.example.evolve.ui.screens.battlescreen.Player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evolve.model.CardData
import com.example.evolve.ui.screens.loadBackgroundImage
import com.example.evolve.ui.utils.loadCardImage
import com.example.evolve.ui.screens.getCardClassColor
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerBanishArea(
    modifier: Modifier = Modifier,
    hasCard: Boolean,
    lastCard: CardData? = null,
    banishCards: List<CardData> = emptyList()
) {
    var showOverlay by remember { mutableStateOf(false) }
    var selectedCard by remember { mutableStateOf<CardData?>(null) }
    var selectedCardImage by remember { mutableStateOf<CardData?>(null) }

    val banishBitmap = loadBackgroundImage("images/battle/Banished.png")

    if (hasCard && lastCard != null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clickable { showOverlay = true }
        ) {
            Image(
                bitmap = loadCardImage("images/${lastCard.expansion}/${lastCard.image}"),
                contentDescription = lastCard.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
    } else {
    Box(modifier = modifier) {
        Image(
            bitmap = banishBitmap,
            contentDescription = "Banish Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}

if (showOverlay) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable {
                showOverlay = false
                selectedCard = null
                selectedCardImage = null
            }
            .zIndex(500f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clickable(enabled = true, onClick = {
                    selectedCard = null
                    selectedCardImage = null
                })
                .fillMaxHeight(0.8f)
                .background(Color.DarkGray.copy(alpha = 0.9f))
                .padding(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                selectedCardImage?.let { card ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .zIndex(600f)
                            .clickable {
                                selectedCardImage = null
                                selectedCard = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = loadCardImage("images/${card.expansion}/${card.image}"),
                            contentDescription = card.name,
                            modifier = Modifier.size(400.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                ) {
                    val groupedCards = banishCards.groupBy { it.card }.map { (cardId, cards) ->
                        val representative = cards.first()
                        val count = cards.size
                        representative to count
                    }
                    items(groupedCards) { (card, count) ->
                        val isSelected = selectedCard?.card == card.card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) getCardClassColor(card.cardclass).copy(alpha = 0.6f)
                                    else getCardClassColor(card.cardclass)
                                )
                                .combinedClickable(
                                    onClick = { selectedCard = card },
                                    onLongClick = {
                                        selectedCard = card
                                        selectedCardImage = card
                                    }
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "$count x",
                                color = if (isSelected) Color.LightGray else Color.Black,
                                fontSize = 16.sp,
                                modifier = Modifier.width(32.dp)
                            )
                            Image(
                                bitmap = loadCardImage("images/${card.expansion}/${card.image}"),
                                contentDescription = card.name,
                                modifier = Modifier.size(64.dp),
                                contentScale = ContentScale.Fit
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = card.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.LightGray else Color.Black,
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "コスト: ${card.cost}　攻撃力: ${card.power}　体力: ${card.hp}",
                                    color = if (isSelected) Color.LightGray else Color.Black,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "種別: ${card.kind}",
                                    color = if (isSelected) Color.LightGray else Color.DarkGray,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "タイプ: ${card.type}",
                                    color = if (isSelected) Color.LightGray else Color.DarkGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                if (selectedCardImage == null) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .zIndex(700f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedCard != null) {
                            Button(
                                onClick = {
                                    showOverlay = false
                                    selectedCard = null
                                    selectedCardImage = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.DarkGray.copy(alpha = 0.9f),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("OK")
                            }
                        }

                        Button(
                            onClick = {
                                showOverlay = false
                                selectedCard = null
                                selectedCardImage = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray.copy(alpha = 0.9f),
                                contentColor = Color.White
                            )
                        ) {
                            Text("閉じる")
                        }
                    }
                }
            }
        }
    }
}
}

