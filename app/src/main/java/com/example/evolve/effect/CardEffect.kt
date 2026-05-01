package com.example.evolve.effect



// 効果の発動タイミングを定義
enum class EffectTiming {
    Passive,       // 永続効果
    Played,        // プレイしたとき
    fanfare,       // ファンファーレ、場に出たとき
    Activated,     // 起動能力 （進化能力含む）
    Triggered,     // 誘発（進化時など）
    OnAttack,      // 攻撃時
    OnEngagement,  // 交戦時
    OnDamaged,     // ダメージを受けたとき
    Lastwords,     // ラストワード、破壊されたとき
    Destroyed,     // 破壊したとき
    OnEndPhase     // エンドフェイズ時
}

// カード効果の基底 sealed class
sealed class CardEffect {
    abstract val timing: EffectTiming

    data class FollowerSelect(
        val count: Int,
        override val timing: EffectTiming
    ) : CardEffect()

    data class Damage(
        val amount: Int,
        override val timing: EffectTiming
    ) : CardEffect()

    data class Draw(
        val count: Int,
        override val timing: EffectTiming
    ) : CardEffect()

    data class AddCounterEffect(
        val amount: Int,
        override val timing: EffectTiming
    ) : CardEffect()
// 今後の拡張: Heal, Boost, Summon, Buff など
}

