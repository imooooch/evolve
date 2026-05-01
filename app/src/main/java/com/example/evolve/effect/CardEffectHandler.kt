package com.example.evolve.effect

import com.example.evolve.battle.BattleState
import com.example.evolve.model.CardData
import com.example.evolve.effect.CardEffect
import com.example.evolve.battle.PlayerState

object CardEffectHandler {

    private val counterHandler = CounterHandler()

    fun applyEffects(
        effects: List<CardEffect>,
        state: BattleState,
        targets: List<CardData> = emptyList()
    ): BattleState {
        var newState = state

        effects.forEach { effect ->
            newState = when (effect) {

                is CardEffect.FollowerSelect -> {
                    val validTargets = getOpponent(newState).field
                    if (validTargets.size < effect.count) {
                        newState
                    } else {
                        newState
                    }
                }

                is CardEffect.AddCounterEffect -> {
                    applyCounterToTargets(
                        state = newState,
                        targets = targets,
                        amount = effect.amount
                    )
                }

                is CardEffect.Damage -> {
                    applyDamageToTargets(
                        state = newState,
                        targets = targets,
                        amount = effect.amount
                    )
                }

                is CardEffect.Draw -> {
                    drawCards(
                        state = newState,
                        count = effect.count
                    )
                }
            }
        }

        return newState
    }

    private fun applyCounterToTargets(
        state: BattleState,
        targets: List<CardData>,
        amount: Int
    ): BattleState {
        val opponent = getOpponent(state)

        val updatedField = opponent.field.map { card ->
            if (targets.any { it.card == card.card }) {
                counterHandler.addCounter(card, amount)
            } else {
                card
            }
        }.toMutableList()

        return updateOpponentField(state, updatedField)
    }

    private fun applyDamageToTargets(
        state: BattleState,
        targets: List<CardData>,
        amount: Int
    ): BattleState {
        val opponent = getOpponent(state)
        val newGraveyard = opponent.graveyard.toMutableList()

        val updatedField = opponent.field.mapNotNull { card ->
            if (targets.any { it.card == card.card }) {
                val updatedCard = card.copy(
                    damage = card.damage + amount
                )

                val currentHp =
                    (updatedCard.baseHp ?: updatedCard.hp ?: 0) +
                            updatedCard.hpModifier -
                            updatedCard.damage

                if (currentHp <= 0) {
                    newGraveyard.add(updatedCard)
                    null
                } else {
                    updatedCard
                }
            } else {
                card
            }
        }.toMutableList()

        return updateOpponentFieldAndGraveyard(
            state = state,
            field = updatedField,
            graveyard = newGraveyard
        )
    }

    private fun drawCards(
        state: BattleState,
        count: Int
    ): BattleState {
        val player = getTurnPlayer(state)

        val newDeck = player.deck.toMutableList()
        val newHand = player.hand.toMutableList()
        val newGraveyard = player.graveyard.toMutableList()
        var newLeaderHp = player.leaderHp

        repeat(count) {
            if (newDeck.isNotEmpty()) {
                val drawn = newDeck.removeAt(0)
                if (newHand.size < 7) {
                    newHand.add(drawn)
                } else {
                    newGraveyard.add(drawn)
                }
            } else {
                newLeaderHp = 0
            }
        }

        val updatedPlayer = player.copy(
            deck = newDeck,
            hand = newHand,
            graveyard = newGraveyard,
            leaderHp = newLeaderHp
        )

        return updateTurnPlayer(state, updatedPlayer)
    }

    private fun getTurnPlayer(state: BattleState) =
        if (state.turnPlayer == state.player1.name) state.player1 else state.player2

    private fun getOpponent(state: BattleState) =
        if (state.turnPlayer == state.player1.name) state.player2 else state.player1

    private fun updateTurnPlayer(
        state: BattleState,
        updatedPlayer: PlayerState
    ): BattleState {
        return if (state.turnPlayer == state.player1.name) {
            state.copy(player1 = updatedPlayer)
        } else {
            state.copy(player2 = updatedPlayer)
        }
    }

    private fun updateOpponentField(
        state: BattleState,
        field: MutableList<CardData>
    ): BattleState {
        return if (state.turnPlayer == state.player1.name) {
            state.copy(player2 = state.player2.copy(field = field))
        } else {
            state.copy(player1 = state.player1.copy(field = field))
        }
    }

    private fun updateOpponentFieldAndGraveyard(
        state: BattleState,
        field: MutableList<CardData>,
        graveyard: MutableList<CardData>
    ): BattleState {
        return if (state.turnPlayer == state.player1.name) {
            state.copy(
                player2 = state.player2.copy(
                    field = field,
                    graveyard = graveyard
                )
            )
        } else {
            state.copy(
                player1 = state.player1.copy(
                    field = field,
                    graveyard = graveyard
                )
            )
        }
    }
}