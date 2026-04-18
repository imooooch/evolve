package com.example.evolve.effect

import com.example.evolve.model.CardData

/**
 * UIによる対象選択が必要な効果を一時的に保持するための構造。
 */

sealed class PendingEffect {
    data class WaitingForTarget(
        val sourceCardId: String,
        val effect: CardEffect.FollowerSelect,
        val remainingEffects: List<CardEffect>
    ) : PendingEffect()

    data class Resolved(
        val resolvedEffects: List<CardEffect>
    ) : PendingEffect()

    object None : PendingEffect()
}
