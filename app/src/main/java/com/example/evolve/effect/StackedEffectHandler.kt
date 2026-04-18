package com.example.evolve.effect

import com.example.evolve.battle.BattleState

object StackedEffectHandler {

    /**
     * チェックタイミングで呼び出され、すべての誘発効果スタックを処理する。
     * - ターンプレイヤー → 非ターンプレイヤーの順に解決
     * - 同一プレイヤー内の順番は仮に登録順（将来UI選択に対応可能）
     * - 解決中に新たなスタックが追加された場合も再度処理
     */
    fun resolveAll(state: BattleState): BattleState {
        var newState = state

        while (newState.stack.isNotEmpty()) {
            val turnPlayer = newState.turnPlayer
            val nonTurnPlayer = if (turnPlayer == newState.player1.name) newState.player2.name else newState.player1.name

            val (turnPlayerStack, other) = newState.stack.partition { it.owner == turnPlayer }
            val (nonTurnPlayerStack, remaining) = other.partition { it.owner == nonTurnPlayer }

            // ターンプレイヤーのスタックを解決（順番は現状固定）
            turnPlayerStack.forEach { stacked ->
                stacked.effects.forEach { effect ->
                    newState = CardEffectHandler.applyEffects(
                        effects = listOf(effect),
                        state = newState
                        // ※対象選択が必要な場合は PendingEffect による一時停止に拡張可能
                    )
                }
            }

            // 非ターンプレイヤーのスタックを解決
            nonTurnPlayerStack.forEach { stacked ->
                stacked.effects.forEach { effect ->
                    newState = CardEffectHandler.applyEffects(
                        effects = listOf(effect),
                        state = newState
                    )
                }
            }

            // 新たにスタックが積まれたか確認（再ループ）
            newState.stack.removeAll(turnPlayerStack + nonTurnPlayerStack)
        }

        return newState
    }
}