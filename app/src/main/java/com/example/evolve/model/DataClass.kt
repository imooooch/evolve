package com.example.evolve.model


import kotlinx.serialization.Serializable

data class CardInfo(
    val index: Int,
    val offsetX: Float,
    val offsetY: Float,
    val rotation: Float,
    val card: CardData
)

@Serializable
data class Deck(
    val name: String,
    val cards: List<CardData>
)

@Serializable
data class CardData(
    val card: String,
    val expansion: String,
    val cardclass: String,
    val name: String,
    val rare: String?,
    val kind: String,
    val type: String,
    val cost: Int?,
    val power: Int?,
    val hp: Int?,
    val ability: String?,
    val evolve: String?,
    val advance: String?,
    val image: String?,
    var count: Int,
    val isActed: Boolean = false,
    val act: Boolean = false,
    val isEvolved: Boolean = false,
    val baseCard: CardData? = null,
    var isEvolvedCard: Boolean = false,         // このカードが進化後であるか
    var originalCard: CardData? = null,         // 進化前のカードデータ
    var isFaceUp: Boolean = false,              // 表向きか（画像表示制御用）
    val baseAbilities: List<AbilityType> = emptyList(),
    val addedAbilities: List<AbilityType> = emptyList(),
    val removedAbilities: List<AbilityType> = emptyList(),
    val basePower: Int? = null,
    val baseHp: Int? = null,
    val damage: Int = 0,
    val powerModifier: Int = 0,
    val hpModifier: Int = 0,
    var counter: Int? = null
)

enum class AbilityType(
    val displayName: String,
    val iconFile: String
) {
    Assail("指定攻撃", "Assail"),
    Aura("オーラ", "Aura"),
    Bane("必殺", "Bane"),
    Drain("ドレイン", "Drain"),
    Intimidate("威圧", "Intimidate"),
    Ward("守護", "Ward")
}

data class CardFilter(
    val maxCost: Int? = null,
    val kindContains: String? = null
)

data class CardDataCount(
    val card: CardData,
    val count: Int
)