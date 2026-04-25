package com.example.evolve.battle

class CardStateHandler {

    fun rotateFieldCardRight(
        state: BattleState,
        index: Int
    ): BattleState {
        val current =
            if (state.turnPlayer == state.player1.name) state.player1 else state.player2

        val field = current.field.toMutableList()
        if (index !in field.indices) return state

        field[index] = field[index].copy(rotation = 90f)

        val updatedPlayer = current.copy(field = field)

        return if (state.turnPlayer == state.player1.name) {
            state.copy(player1 = updatedPlayer)
        } else {
            state.copy(player2 = updatedPlayer)
        }
    }

    fun setFieldCardAct(
        state: BattleState,
        index: Int,
        act: Boolean
    ): BattleState {
        val current =
            if (state.turnPlayer == state.player1.name) state.player1 else state.player2

        val field = current.field.toMutableList()
        if (index !in field.indices) return state

        field[index] = field[index].copy(act = act)

        val updatedPlayer = current.copy(field = field)

        return if (state.turnPlayer == state.player1.name) {
            state.copy(player1 = updatedPlayer)
        } else {
            state.copy(player2 = updatedPlayer)
        }
    }
}