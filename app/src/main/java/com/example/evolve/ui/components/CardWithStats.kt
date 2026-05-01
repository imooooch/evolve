package com.example.evolve.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evolve.model.CardData
import com.example.evolve.ui.utils.loadCardImage

@Composable
fun CardWithStats(
    card: CardData,
    modifier: Modifier = Modifier,
    showAbilities: Boolean = true   // ← 追加
) {
    val basePower = card.basePower ?: card.originalCard?.power ?: card.power
    val baseHp = card.baseHp ?: card.originalCard?.hp ?: card.hp

    val displayPower = basePower?.let {
        it + card.powerModifier
    }

    val displayHp = baseHp?.let {
        it + card.hpModifier - card.damage
    }
    val displayAbilities =
        (card.baseAbilities + card.addedAbilities)
            .filterNot { it in card.removedAbilities }
            .distinct()
    val mainColumn = displayAbilities.take(4)
    val extraColumn = displayAbilities.drop(4).take(4)

    val powerBgColor = when {
        displayPower == null || basePower == null -> Color(0xFF0D47A1)
        displayPower > basePower -> Color(0xFF2E7D32) // 上昇
        displayPower < basePower -> Color(0xFF6A1B9A) // 低下
        else -> Color(0xFF0D47A1)
    }

    val hpBgColor = when {
        displayHp == null || baseHp == null -> Color(0xFF8E0000)
        displayHp < baseHp -> Color(0xFFD50000) // ダメージ/低下
        displayHp > baseHp -> Color(0xFF2E7D32) // 増加
        else -> Color(0xFF8E0000)
    }

    val isAmulet = card.kind.contains("アミュレット")

    val showPower = if (isAmulet) {
        displayPower != null && displayPower != 0
    } else {
        displayPower != null
    }

    val showHp = if (isAmulet) {
        displayHp != null && displayHp != 0
    } else {
        displayHp != null
    }



    BoxWithConstraints(modifier = modifier) {
        val iconSize = maxWidth * 0.28f
        val statFontSize = (maxWidth.value * 0.32f).sp
        val cornerSize = maxWidth * 0.09f
        val statHorizontalPadding = maxWidth * 0.12f
        val statVerticalPadding = maxHeight * 0.03f
        val iconOffsetX = -(iconSize * 2.2f)
        val showCounter = card.counter != null
        val counterSize = maxWidth * 0.28f
        val counterFontSize = (maxWidth.value * 0.20f).sp
        Image(
            bitmap = loadCardImage("images/${card.expansion}/${card.image}"),
            contentDescription = card.name,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        if (showCounter) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = maxHeight * 0.02f, end = maxWidth * 0.02f)
                    .size(counterSize)
                    .background(
                        color = Color(0xFF2E7D32),
                        shape = CircleShape
                    )
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.counter.toString(),
                    color = Color.White,
                    fontSize = counterFontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (showAbilities) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = iconOffsetX, y = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(iconSize * 0.12f)
            ) {
                if (extraColumn.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        extraColumn.forEach { ability ->
                            Image(
                                bitmap = loadCardImage("images/icon/${ability.iconFile}.png"),
                                contentDescription = ability.displayName,
                                modifier = Modifier.size(iconSize),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    mainColumn.forEach { ability ->
                        Image(
                            bitmap = loadCardImage("images/icon/${ability.iconFile}.png"),
                            contentDescription = ability.displayName,
                            modifier = Modifier.size(iconSize),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
        if (showPower) {
            Text(
                text = displayPower?.toString() ?: "",
                color = Color.White,
                fontSize = statFontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = maxWidth * 0.02f, bottom = maxHeight * 0.01f)
                    .background(powerBgColor, RoundedCornerShape(cornerSize))
                    .border(1.dp, Color(0xFF46484A), RoundedCornerShape(cornerSize))
                    .padding(
                        horizontal = statHorizontalPadding,
                        vertical = statVerticalPadding
                    )
            )
        }

        if (showHp) {
            Text(
                text = displayHp?.toString() ?: "",
                color = Color.White,
                fontSize = statFontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = maxWidth * 0.02f, bottom = maxHeight * 0.01f)
                    .background(hpBgColor, RoundedCornerShape(cornerSize))
                    .border(1.dp, Color(0xFF46484A), RoundedCornerShape(cornerSize))
                    .padding(
                        horizontal = statHorizontalPadding,
                        vertical = statVerticalPadding
                    )
            )
        }
    }
}
