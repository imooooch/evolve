package com.example.evolve.effect

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.example.evolve.effect.CardEffect.*
import kotlinx.serialization.json.contentOrNull
import com.example.evolve.model.AbilityType
import com.example.evolve.model.CardData


object CardEffectLoader {

    fun loadEffectsForCard(context: Context, cardId: String, expansion: String): List<CardEffect> {
        return loadEffectsForCardByTrigger(context, cardId, expansion, null)
    }

    fun loadEffectsForCardByTrigger(
        context: Context,
        cardId: String,
        expansion: String,
        trigger: EffectTrigger?
    ): List<CardEffect> {
        val effectList = mutableListOf<CardEffect>()

        try {
            val inputStream = context.assets.open("json/effect/${expansion}_effect.json")
            val jsonText = inputStream.bufferedReader().use { it.readText() }
            val root = Json.parseToJsonElement(jsonText).jsonObject
            val effects = root[cardId]?.jsonArray ?: return emptyList()

            for (entry in effects) {
                val obj = entry.jsonObject
                val type = obj["type"]?.jsonPrimitive?.content ?: continue
                val value = obj["value"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
                val triggerStr = obj["trigger"]?.jsonPrimitive?.content ?: "Triggered"
                val entryTrigger = EffectTrigger.valueOf(triggerStr)

                if (trigger != null && entryTrigger != trigger) continue

                val effect = when (type) {
                    "FollowerSelect" -> FollowerSelect(
                        count = value ?: 1,
                        trigger = entryTrigger
                    )

                    "Damage" -> Damage(
                        amount = value ?: 0,
                        trigger = entryTrigger
                    )

                    "Draw" -> Draw(
                        count = value ?: 1,
                        trigger = entryTrigger
                    )

                    "AddCounter" -> AddCounterEffect(
                        amount = obj["amount"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
                            ?: value
                            ?: 1,
                        trigger = entryTrigger
                    )

                    else -> null
                }
                effect?.let { effectList.add(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return effectList
    }
    fun loadBaseAbilities(
        context: Context,
        expansion: String
    ): Map<String, List<AbilityType>> {
        return try {
            val inputStream = context.assets.open("json/effect/${expansion}_effect.json")
            val jsonText = inputStream.bufferedReader().use { it.readText() }

            val root = Json.parseToJsonElement(jsonText).jsonArray

            root.associate { entry ->
                val obj = entry.jsonObject
                val cardId = obj["card"]?.jsonPrimitive?.content ?: ""

                val abilities = obj["baseAbilities"]
                    ?.jsonArray
                    ?.mapNotNull { abilityJson ->
                        val abilityName = abilityJson.jsonPrimitive.content
                        runCatching { AbilityType.valueOf(abilityName) }.getOrNull()
                    }
                    ?: emptyList()

                cardId to abilities
            }.filterKeys { it.isNotBlank() }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    fun applyBaseAbilitiesToCards(
        cards: List<CardData>,
        abilityMap: Map<String, List<AbilityType>>
    ): List<CardData> {
        return cards.map { card ->
            val abilities = abilityMap[card.card]
            if (abilities != null) {
                card.copy(baseAbilities = abilities)
            } else {
                card
            }
        }
    }
}
