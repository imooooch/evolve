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

    fun moveEvolvedCardFromField(
        index: Int,
        destination: String,
        state: BattleState
    ): BattleState {
        val currentPlayer =
            if (state.turnPlayer == state.player1.name) state.player1 else state.player2

        val fieldCard = currentPlayer.field.getOrNull(index) ?: return state

        if (!fieldCard.isEvolved || fieldCard.originalCard == null) return state

        val originalCard = resetCardState(fieldCard.originalCard!!)

        val evolvedCard = resetCardState(fieldCard).copy(
            isFaceUp = true,
            isEvolved = false,
            originalCard = null
        )

        val updatedEvolveDeck = currentPlayer.evolveDeck.toMutableList().apply {
            add(evolvedCard)
        }

        val updatedPlayerBeforeField = when (destination) {
            "Graveyard" -> currentPlayer.copy(
                evolveDeck = updatedEvolveDeck,
                graveyard = currentPlayer.graveyard.toMutableList().apply { add(originalCard) }
            )

            "Banish" -> currentPlayer.copy(
                evolveDeck = updatedEvolveDeck,
                banish = currentPlayer.banish.toMutableList().apply { add(originalCard) }
            )

            "Deck" -> currentPlayer.copy(
                evolveDeck = updatedEvolveDeck,
                deck = currentPlayer.deck.toMutableList().apply { add(originalCard) }
            )

            "Hand" -> currentPlayer.copy(
                evolveDeck = updatedEvolveDeck,
                hand = currentPlayer.hand.toMutableList().apply { add(originalCard) }
            )

            "Ex" -> currentPlayer.copy(
                evolveDeck = updatedEvolveDeck,
                exArea = currentPlayer.exArea.toMutableList().apply { add(originalCard) }
            )

            else -> currentPlayer.copy(evolveDeck = updatedEvolveDeck)
        }

        val updatedField = updatedPlayerBeforeField.field.toMutableList().apply {
            removeAt(index)
        }

        val finalPlayer = updatedPlayerBeforeField.copy(field = updatedField)

        return if (state.turnPlayer == state.player1.name) {
            state.copy(player1 = finalPlayer)
        } else {
            state.copy(player2 = finalPlayer)
        }
    }
    fun moveFieldCardToGrave(
        state: BattleState,
        index: Int
    ): BattleState {
        val currentPlayer =
            if (state.turnPlayer == state.player1.name) state.player1 else state.player2

        if (index !in currentPlayer.field.indices) return state

        val fieldCard = currentPlayer.field[index]

        if (fieldCard.isEvolved) {
            return moveEvolvedCardFromField(index, "Graveyard", state)
        }

        val updatedField = currentPlayer.field.toMutableList()
        val updatedGraveyard = currentPlayer.graveyard.toMutableList()

        val originalCard = updatedField.removeAt(index)
        val resetCard = resetCardState(originalCard)
        updatedGraveyard.add(resetCard)

        val updatedPlayer = currentPlayer.copy(
            field = updatedField,
            graveyard = updatedGraveyard
        )

        return if (state.turnPlayer == state.player1.name) {
            state.copy(player1 = updatedPlayer)
        } else {
            state.copy(player2 = updatedPlayer)
        }
    }
    fun moveFieldCardToBanish(
        state: BattleState,
        index: Int
    ): BattleState {
        val currentPlayer =
            if (state.turnPlayer == state.player1.name) state.player1 else state.player2

        if (index !in currentPlayer.field.indices) return state

        val fieldCard = currentPlayer.field[index]

        if (fieldCard.isEvolved || fieldCard.originalCard != null) {
            return moveEvolvedCardFromField(index, "Banish", state)
        }

        val updatedField = currentPlayer.field.toMutableList()
        val updatedBanish = currentPlayer.banish.toMutableList()

        val originalCard = updatedField.removeAt(index)
        val resetCard = resetCardState(originalCard)
        updatedBanish.add(resetCard)

        val updatedPlayer = currentPlayer.copy(
            field = updatedField,
            banish = updatedBanish
        )

        return if (state.turnPlayer == state.player1.name) {
            state.copy(player1 = updatedPlayer)
        } else {
            state.copy(player2 = updatedPlayer)
        }
    }
}