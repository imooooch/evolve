package com.example.evolve.battle

class BattleTurnController {

    fun startTurn(machine: BattleStateMachine): BattleState {
        machine.startTurn()
        return machine.state
    }

    fun endTurn(machine: BattleStateMachine): BattleState {
        machine.endTurnPhase()
        return machine.state
    }
}