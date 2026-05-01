package com.example.evolve.effect

// カード効果の基底 sealed class
sealed class CardEffect {
    abstract val trigger: EffectTrigger

    data class FollowerSelect(
        val count: Int,
        override val trigger: EffectTrigger
    ) : CardEffect()

    data class Damage(
        val amount: Int,
        override val trigger: EffectTrigger
    ) : CardEffect()

    data class Draw(
        val count: Int,
        override val trigger: EffectTrigger
    ) : CardEffect()

    data class AddCounterEffect(
        val amount: Int,
        override val trigger: EffectTrigger
    ) : CardEffect()

// 今後の拡張: Heal, Boost, Summon, Buff など
}

