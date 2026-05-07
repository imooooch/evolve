package com.example.evolve.ui.screens.battlescreen.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.evolve.model.CardData

@Composable
fun EvolveDeckListDialog(
    cards: List<CardData>,
    onDismiss: () -> Unit,
    emptyMessage: String = "表向きのカードがありません"
) {
    if (cards.isEmpty()) {
        androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
            Box(
                modifier = Modifier
                    .size(width = 320.dp, height = 220.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(emptyMessage)
            }
        }
    } else {
        // ここに PlayerEvolveDeckArea で使っている
        // 既存のリスト表示処理をそのまま移動する
    }
}