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
    modifier: Modifier = Modifier
) {
    val originalPower = card.originalCard?.power ?: card.power
    val originalHp = card.originalCard?.hp ?: card.hp

    val powerBgColor = when {
        card.power == null || originalPower == null -> Color(0xFF0D47A1)
        card.power > originalPower -> Color(0xFF2E7D32) // 上昇
        card.power < originalPower -> Color(0xFF6A1B9A) // 低下
        else -> Color(0xFF0D47A1)
    }

    val hpBgColor = when {
        card.hp == null || originalHp == null -> Color(0xFF8E0000)
        card.hp < originalHp -> Color(0xFFD50000) // ダメージ
        card.hp > originalHp -> Color(0xFF2E7D32) // 増加
        else -> Color(0xFF8E0000)
    }

    Box(modifier = modifier) {
        Image(
            bitmap = loadCardImage("images/${card.expansion}/${card.image}"),
            contentDescription = card.name,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = card.power?.toString() ?: "",
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

        Text(
            text = card.hp?.toString() ?: "",
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