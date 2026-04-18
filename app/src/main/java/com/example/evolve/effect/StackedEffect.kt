package com.example.evolve.effect

import com.example.evolve.effect.CardEffect

/**
 * チェックタイミングで処理される誘発効果のスタック。
 */
data class StackedEffect(
    val sourceCardId: String,       // 効果の元となったカードID
    val owner: String,              // 所有者（プレイヤー名など）
    val effects: List<CardEffect>   // 発動する効果一覧
)
