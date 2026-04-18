package com.example.evolve.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evolve.effect.PendingEffect
import com.example.evolve.ui.viewmodel.TestEffectViewModel
import com.example.evolve.model.DeckModel
import com.example.evolve.model.CardModel
import com.example.evolve.model.Deck
import com.example.evolve.model.CardData

@Composable
fun TestEffectScreen(viewModel: TestEffectViewModel = viewModel()) {
    val context = LocalContext.current
    val battleState by viewModel.battleState.collectAsState()
    val pendingEffect by viewModel.pendingEffect.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("[🧪 効果テスト画面]", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        Text("Player HP: ${battleState.player1.leaderHp}")
        Text("Deck: ${battleState.player1.deck.size}")
        Text("Hand: ${battleState.player1.hand.size}")
        Spacer(Modifier.height(8.dp))

        Text("Opponent Field:")
        battleState.player2.field.forEach { card ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color.LightGray),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${card.name} (HP: ${card.hp})", modifier = Modifier.padding(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            viewModel.loadAndApplyEffects(
                context = context,
                cardId = "BP14-001",
                expansion = "BP14"
            )
        }) {
            Text("▶ BP14-001 効果を発動")
        }

        Spacer(Modifier.height(16.dp))

        if (pendingEffect is PendingEffect.WaitingForTarget) {
            val effect = pendingEffect as PendingEffect.WaitingForTarget
            Text("🕐 対象を${effect.effect.count}体選んでください：")
            effectAvailableTargets(
                opponentCards = battleState.player2.field,
                onSelect = { selected ->
                    viewModel.resolveTargetSelection(selected)
                }
            )
        }
    }
}

@Composable
fun effectAvailableTargets(
    opponentCards: List<CardData>,
    onSelect: (List<CardData>) -> Unit
) {
    var selectedCard by remember { mutableStateOf<CardData?>(null) }

    Column {
        opponentCards.forEach { card ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clickable { selectedCard = card }
                    .background(if (selectedCard == card) Color.Yellow else Color.Transparent),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${card.name} (HP: ${card.hp})", modifier = Modifier.padding(8.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            selectedCard?.let { onSelect(listOf(it)) }
        }, enabled = selectedCard != null) {
            Text("✅ 選択して効果を適用")
        }
    }
}