package com.example.evolve.effect

import com.example.evolve.model.CardData

class CounterHandler {

    fun addCounter(card: CardData, amount: Int): CardData {
        val current = card.counter ?: 0
        return card.copy(counter = (current + amount).coerceAtLeast(0))
    }

    fun removeCounter(card: CardData, amount: Int): CardData {
        val current = card.counter ?: 0
        return card.copy(counter = (current - amount).coerceAtLeast(0))
    }

    fun setCounter(card: CardData, value: Int): CardData {
        return card.copy(counter = value.coerceAtLeast(0))
    }

    fun clearCounter(card: CardData): CardData {
        return card.copy(counter = null)
    }
}