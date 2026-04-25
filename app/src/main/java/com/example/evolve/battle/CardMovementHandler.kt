package com.example.evolve.battle

import com.example.evolve.model.CardData

class CardMovementHandler {

    fun resetCardState(card: CardData): CardData {
        return card.copy(
            act = false,
            rotation = 0f,
            isEvolved = false,
            originalCard = null
        )
    }
}