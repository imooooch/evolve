package com.example.evolve.effect

import com.example.evolve.battle.BattleState
import com.example.evolve.model.CardData
import com.example.evolve.effect.CardEffect.*

object CardEffectHandler {

    /**
     * 与えられた効果リストを順に処理し、状態を更新して返す
     * 選択対象が必要な場合は別途 PendingEffect で管理される前提
     */
    fun applyEffects(
        effects: List<CardEffect>,
        state: BattleState,
        targets: List<CardData> = emptyList()
    ): BattleState {
        var newState = state

        effects.forEach { effect ->
            when (effect) {
                is FollowerSelect -> {
                    // 対象が足りなければ効果発動不可
                    val validTargets = getOpponent(newState).field
                    if (validTargets.size < effect.count) {
                        return state // 発動せず終了
                    }
                }

                is Damage -> {
                    val updatedField = getOpponent(newState).field.mapNotNull { card ->
                        if (targets.any { it.card == card.card }) {
                            val newHp = (card.hp ?: 0) - effect.amount
                            if (newHp <= 0) {
                                // 墓場に送る
                                getOpponent(newState).graveyard.add(card)
                                null // フィールドから除外
                            } else {
                                card.copy(hp = newHp)
                            }
                        } else card
                    }

                    newState = if (newState.turnPlayer == newState.player1.name) {
                        newState.copy(player2 = newState.player2.copy(field = updatedField.toMutableList()))
                    } else {
                        newState.copy(player1 = newState.player1.copy(field = updatedField.toMutableList()))
                    }
                }

                is Draw -> {
                    val player = getTurnPlayer(newState)
                    repeat(effect.count) {
                        if (player.deck.isNotEmpty()) {
                            val drawn = player.deck.removeAt(0)
                            if (player.hand.size < 7) {
                                player.hand.add(drawn)
                            } else {
                                player.graveyard.add(drawn)
                            }
                        } else {
                            player.leaderHp = 0 // 敗北：デッキ切れ
                        }
                    }
                }
            }
        }

        return newState
    }

    private fun getTurnPlayer(state: BattleState) =
        if (state.turnPlayer == state.player1.name) state.player1 else state.player2

    private fun getOpponent(state: BattleState) =
        if (state.turnPlayer == state.player1.name) state.player2 else state.player1
}
