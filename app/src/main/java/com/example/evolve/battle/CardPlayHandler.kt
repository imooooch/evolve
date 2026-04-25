package com.example.evolve.battle

class CardPlayHandler {

    fun playCardFromHand(
        state: BattleState,
        index: Int
    ): BattleState {

        val current =
            if (state.turnPlayer == state.player1.name) state.player1 else state.player2

        val hand = current.hand.toMutableList()
        if (index !in hand.indices) return state

        val card = hand[index]
        hand.removeAt(index)

        val field = current.field.toMutableList()
        if ((card.kind.contains("フォロワー") || card.kind.contains("アミュレット")) && field.size < 5) {
            field.add(card)
        }

        val updatedPlayer = current.copy(
            hand = hand,
            field = field
        )

        return if (state.turnPlayer == state.player1.name) {
            state.copy(player1 = updatedPlayer)
        } else {
            state.copy(player2 = updatedPlayer)
        }
    }
    fun playCardFromExArea(
        state: BattleState,
        index: Int
    ): BattleState {
        val current =
            if (state.turnPlayer == state.player1.name) state.player1 else state.player2

        val exArea = current.exArea.toMutableList()
        val field = current.field.toMutableList()

        if (index !in exArea.indices) return state
        if (field.size >= 5) return state

        val card = exArea.removeAt(index)
        field.add(card)

        val updatedPlayer = current.copy(
            exArea = exArea,
            field = field
        )

        return if (state.turnPlayer == state.player1.name) {
            state.copy(player1 = updatedPlayer)
        } else {
            state.copy(player2 = updatedPlayer)
        }
    }
}