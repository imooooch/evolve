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

        field[index] = field[index].copy(isActed = true)

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

        field[index] = field[index].copy(
            isActed = act
        )

        val updatedPlayer = current.copy(field = field)

        return if (state.turnPlayer == state.player1.name) {
            state.copy(player1 = updatedPlayer)
        } else {
            state.copy(player2 = updatedPlayer)
        }
    }

    fun standFieldCards(state: BattleState): BattleState {
        val current =
            if (state.turnPlayer == state.player1.name) state.player1 else state.player2

        val updatedField = current.field.map {
            it.copy(isActed = false)
        }.toMutableList()

        val updatedPlayer = current.copy(field = updatedField)

        return if (state.turnPlayer == state.player1.name) {
            state.copy(player1 = updatedPlayer)
        } else {
            state.copy(player2 = updatedPlayer)
        }
    }

}