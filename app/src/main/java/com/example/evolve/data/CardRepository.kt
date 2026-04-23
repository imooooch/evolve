package com.example.evolve.data

import android.content.Context
import com.example.evolve.model.CardModel
import kotlinx.serialization.json.Json

class CardRepository(private val context: Context) {

    fun loadCardsFromJson(setName: String): List<CardModel> {
        return try {
            val jsonString = context.assets
                .open("json/$setName.json")
                .bufferedReader()
                .use { it.readText() }

            Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAllCards(): List<CardModel> {
        return try {
            val setFiles = context.assets.list("json")
                ?.filter { it.endsWith(".json") }
                ?.map { it.removeSuffix(".json") }
                ?.filter { it != "effect" }
                ?: emptyList()

            setFiles.flatMap { setName ->
                loadCardsFromJson(setName)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getCardById(setName: String, cardId: String): CardModel? {
        return loadCardsFromJson(setName).find { it.card == cardId }
    }
}