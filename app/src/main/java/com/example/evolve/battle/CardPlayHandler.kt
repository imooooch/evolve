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
}