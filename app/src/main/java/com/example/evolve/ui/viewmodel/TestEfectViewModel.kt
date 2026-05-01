package com.example.evolve.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evolve.battle.BattleState
import com.example.evolve.battle.PlayerState
import com.example.evolve.effect.CardEffect
import com.example.evolve.effect.CardEffectHandler
import com.example.evolve.effect.CardEffectLoader
import com.example.evolve.effect.PendingEffect
import com.example.evolve.model.CardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TestEffectViewModel : ViewModel() {

    private val _battleState = MutableStateFlow(createInitialState())
    val battleState: StateFlow<BattleState> = _battleState

    private val _pendingEffect = MutableStateFlow<PendingEffect>(PendingEffect.None)
    val pendingEffect: StateFlow<PendingEffect> = _pendingEffect

    fun loadAndApplyEffects(context: Context, cardId: String, expansion: String) {
        viewModelScope.launch {
            val effects = CardEffectLoader.loadEffectsForCard(context, cardId, expansion)

            val firstSelect = effects.firstOrNull { it is CardEffect.FollowerSelect }
            if (firstSelect is CardEffect.FollowerSelect) {
                _pendingEffect.value = PendingEffect.WaitingForTarget(
                    sourceCardId = cardId,
                    effect = firstSelect,
                    remainingEffects = effects.drop(1)
                )
            } else {
                val updated = CardEffectHandler.applyEffects(effects, _battleState.value)
                _battleState.value = updated
            }
        }
    }

    fun resolveTargetSelection(selected: List<CardData>) {
        val current = _pendingEffect.value
        if (current is PendingEffect.WaitingForTarget) {
            val allEffects = listOf(current.effect) + current.remainingEffects
            val result = CardEffectHandler.applyEffects(allEffects, _battleState.value, selected)
            _battleState.value = result
            _pendingEffect.value = PendingEffect.None
        }
    }

    private fun createInitialState(): BattleState {
        val dummyCard = CardData(
            card = "DUMMY-001",
            expansion = "BP14",
            cardclass = "Elf",
            name = "テストフォロワー",
            rare = "GR",
            kind = "フォロワー",
            type = "テスト種族",
            cost = 2,
            power = 2,
            hp = 7,
            ability = "",
            evolve = null,
            advance = null,
            image = null,
            count = 1,
            isActed = false
        )

        val player = PlayerState(name = "Player").apply {
            deck.addAll(List(5) { dummyCard.copy(card = "DRAW-${it}") })
        }

        val opponent = PlayerState(name = "Enemy").apply {
            field.addAll(
                listOf(
                    dummyCard.copy(card = "ENEMY-001", name = "敵フォロワー1"),
                    dummyCard.copy(card = "ENEMY-002", name = "敵フォロワー2")
                )
            )
        }

        return BattleState(player1 = player, player2 = opponent)
    }
}