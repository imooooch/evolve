package com.example.evolve.battle

import android.content.Context
import com.example.evolve.model.CardData
import kotlinx.serialization.json.Json
import java.io.File
import com.example.evolve.model.Deck


class DeckLoader {

    fun loadDeck(context: Context, deckName: String): Deck? {
        val file = File(context.getExternalFilesDir(null), "$deckName.json")

        return if (file.exists()) {
            try {
                val text = file.readText()
                Json { ignoreUnknownKeys = true }
                    .decodeFromString<Deck>(text)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun expandDeck(cards: List<CardData>): List<CardData> {
        return cards.flatMap { card ->
            List(card.count) {
                card.copy(
                    count = 1,
                    isFaceUp = false
                )
            }
        }
    }
}