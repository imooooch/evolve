package com.example.evolve.battle

import com.example.evolve.model.CardData

class EvolveHandler {

    fun evolveCard(
        state: BattleState,
        index: Int,
        baseCard: CardData,
        evolvedCardData: CardData,
        originalBaseCard: CardData
    ): BattleState {
        val player =
            if (state.turnPlayer == state.player1.name) state.player1 else state.player2

        if (index !in player.field.indices) return state
        if (player.field[index].card != baseCard.card) return state
        if (player.field[index].isEvolved) return state

        val newEvolveDeck = player.evolveDeck.toMutableList()

        val removeIndex = newEvolveDeck.indexOfFirst {
            it.card == evolvedCardData.card && !it.isFaceUp
        }

        if (removeIndex == -1) return state

        newEvolveDeck.removeAt(removeIndex)

        val evolvedCard = evolvedCardData.copy(
            isEvolved = true,
            originalCard = originalBaseCard,
            isFaceUp = true,
            isActed = baseCard.isActed
        )

        val newField = player.field.toMutableList().apply {
            set(index, evolvedCard)
        }

        val updatedPlayer = player.copy(
            field = newField,
            evolveDeck = newEvolveDeck
        )

        return if (state.turnPlayer == state.player1.name) {
            state.copy(player1 = updatedPlayer)
        } else {
            state.copy(player2 = updatedPlayer)
        }
    }
}