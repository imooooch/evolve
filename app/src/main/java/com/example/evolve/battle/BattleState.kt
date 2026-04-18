package com.example.evolve.battle

import com.example.evolve.model.CardData
import com.example.evolve.effect.StackedEffect

// ゲームの状態を保持するデータクラス
// プレイヤーのリーダー体力、PP/EP、手札、デッキ、場、EXエリアなどを管理

data class PlayerState(
    val name: String,
    var leaderHp: Int = 20,
    var maxPP: Int = 0,
    var currentPP: Int = 0,
    var ep: Int = 0,
    val deck: MutableList<CardData> = mutableListOf(),
    val hand: MutableList<CardData> = mutableListOf(),
    val field: MutableList<CardData> = mutableListOf(),
    val evolveDeck: MutableList<CardData> = mutableListOf(),
    val exArea: MutableList<CardData> = mutableListOf(),
    val graveyard: MutableList<CardData> = mutableListOf(),
    val banished: MutableList<CardData> = mutableListOf(),
    val banish: MutableList<CardData> = mutableListOf()

)

data class BattleState(
    val player1: PlayerState,
    val player2: PlayerState,
    var turnPlayer: String = player1.name,
    var phase: Phase = Phase.START,
    val stack: MutableList<StackedEffect> = mutableListOf() // チェックタイミングで処理される誘発効果のスタック
)

enum class Phase {
    START,
    MAIN,
    ATTACK,
    END
}
