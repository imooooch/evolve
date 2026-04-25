package com.example.evolve.battle

import android.content.Context

class BattleInitializer(
    private val deckLoader: DeckLoader
) {
    fun createInitialState(
        context: Context,
        deck1Name: String,
        deck2Name: String,
        isFirstPlayer: Boolean
    ): BattleState? {
        val deck1 = deckLoader.loadDeck(context, deck1Name)
        val deck2 = deckLoader.loadDeck(context, deck2Name)

        if (deck1 == null || deck2 == null) return null

        val player1 = PlayerState(name = "Player").apply {
            deck.addAll(deckLoader.expandDeck(deck1.cards.filterNot {
                it.kind.contains("エボルヴ") || it.kind.contains("アドバンス")
            }).shuffled())

            evolveDeck.addAll(deckLoader.expandDeck(deck1.cards.filter {
                it.kind.contains("エボルヴ") || it.kind.contains("アドバンス")
            }))
        }

        val player2 = PlayerState(name = "Opponent").apply {
            deck.addAll(deckLoader.expandDeck(deck2.cards.filterNot {
                it.kind.contains("エボルヴ") || it.kind.contains("アドバンス")
            }).shuffled())

            evolveDeck.addAll(deckLoader.expandDeck(deck2.cards.filter {
                it.kind.contains("エボルヴ") || it.kind.contains("アドバンス")
            }))
        }

        if (isFirstPlayer) {
            player1.ep = 0
            player2.ep = 3
        } else {
            player1.ep = 3
            player2.ep = 0
        }

        repeat(18) {
            if (player1.deck.isNotEmpty()) player1.hand.add(player1.deck.removeAt(0))
            if (player2.deck.isNotEmpty()) player2.hand.add(player2.deck.removeAt(0))
        }

        return BattleState(player1, player2)
    }
}