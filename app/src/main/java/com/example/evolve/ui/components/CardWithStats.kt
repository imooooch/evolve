package com.example.evolve.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
    val basePower = card.basePower ?: card.power
    val baseHp = card.baseHp ?: card.hp

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
    Box(modifier = modifier) {
        Image(
            bitmap = loadCardImage("images/${card.expansion}/${card.image}"),
            contentDescription = card.name,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        if (showAbilities) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-36).dp, y = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (extraColumn.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        extraColumn.forEach { ability ->
                            Image(
                                bitmap = loadCardImage("images/icon/${ability.iconFile}.png"),
                                contentDescription = ability.displayName,
                                modifier = Modifier.size(16.dp),
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
                            modifier = Modifier.size(16.dp),
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
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 2.dp, bottom = 1.dp)
                    .background(powerBgColor, shape = RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF46484A), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        if (showHp) {
            Text(
                text = displayHp?.toString() ?: "",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 1.dp)
                    .background(hpBgColor, shape = RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF46484A), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
