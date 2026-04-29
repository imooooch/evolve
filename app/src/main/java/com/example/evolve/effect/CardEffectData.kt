package com.example.evolve.effect

import com.example.evolve.model.AbilityType
import kotlinx.serialization.Serializable

@Serializable
data class CardEffectData(
    val card: String,
    val baseAbilities: List<AbilityType> = emptyList(),
    val effects: List<EffectEntry> = emptyList()
)

@Serializable
data class EffectEntry(
    val timing: String? = null,
    val type: String? = null,
    val optional: Boolean = false
)