package com.example.evolve.battle

import com.example.evolve.model.CardData

class AttackHandler {

    fun attackWith(
        machine: BattleStateMachine,
        card: CardData
    ): BattleState {
        machine.attackWith(card)
        return machine.state
    }
}