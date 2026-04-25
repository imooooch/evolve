package com.example.evolve.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.evolve.model.CardData
import kotlinx.coroutines.flow.update
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.example.evolve.battle.BattleState
import com.example.evolve.battle.BattleStateMachine
import com.example.evolve.battle.CardMovementHandler
import com.example.evolve.battle.CardPlayHandler
import com.example.evolve.battle.DeckLoader
import com.example.evolve.battle.PlayerState

class BattleViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val cardPlayHandler = CardPlayHandler()
    private val cardMovementHandler = CardMovementHandler()
    private val deckLoader = DeckLoader()
    private val _battleState = MutableStateFlow<BattleState?>(null)
    val battleState: StateFlow<BattleState?> = _battleState
    private val uiStateController = BattleUiStateController(viewModelScope)
    val selectedImagePath = uiStateController.selectedImagePath
    val tapEffectOffset = uiStateController.tapEffectOffset
    private val selectionController = BattleSelectionController()

    val selectedHandIndex = selectionController.selectedHandIndex
    val selectedCardIndex = selectionController.selectedCardIndex
    val selectedExCardIndex = selectionController.selectedExCardIndex

    val highlightCardIndex = selectionController.highlightCardIndex
    val highlightFieldCardIndex = selectionController.highlightFieldCardIndex
    val highlightExCardIndex = selectionController.highlightExCardIndex

    fun showImage(path: String) {
        uiStateController.showImage(path)
    }

    fun clearImage() {
        uiStateController.clearImage()
    }


    private lateinit var machine: BattleStateMachine

    fun loadBattle(context: Context) {
        val deck1Name = savedStateHandle.get<String>("deck1Name") ?: return
        val deck2Name = savedStateHandle.get<String>("deck2Name") ?: return
        val deck1 = deckLoader.loadDeck(context, deck1Name)
        val deck2 = deckLoader.loadDeck(context, deck2Name)

        if (deck1 != null && deck2 != null) {
            val shuffled1 = deck1.cards
            val shuffled2 = deck2.cards
            val player1 = PlayerState(name = "Player").apply {
                deck.addAll(deckLoader.expandDeck(deck1.cards.filterNot {
                    it.kind.contains("エボルヴ") || it.kind.contains("アドバンス")
                }).shuffled())
                evolveDeck.addAll(deckLoader.expandDeck(shuffled1.filter {
                    it.kind.contains("エボルヴ") || it.kind.contains("アドバンス")
                }))
            }
            val player2 = PlayerState(name = "Opponent").apply {
                deck.addAll(deckLoader.expandDeck(deck2.cards.filterNot {
                    it.kind.contains("エボルヴ") || it.kind.contains("アドバンス")
                }).shuffled())
                evolveDeck.addAll(deckLoader.expandDeck(shuffled2.filter {
                    it.kind.contains("エボルヴ") || it.kind.contains("アドバンス")
                }))
            }
            val state = BattleState(player1, player2)
            val isFirstPlayer = savedStateHandle.get<Boolean>("isFirstPlayer") ?: false

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

            machine = BattleStateMachine(state, context)
            _battleState.value = state
        }
    }

    val playerEvolveDeck: StateFlow<List<CardData>?>
        get() = _battleState.map { state ->
            if (state?.turnPlayer == state?.player1?.name) {
                state?.player1?.evolveDeck
            } else {
                state?.player2?.evolveDeck ?: emptyList()
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun startTurn() {
        _battleState.value?.let {
            machine.startTurn()
            _battleState.value = machine.state
        }
    }

    fun playCard(index: Int) {
        _battleState.update { state ->
            state ?: return@update null

            val current =
                if (state.turnPlayer == state.player1.name) state.player1 else state.player2
            val card = current.hand.getOrNull(index) ?: return@update state

            val newHand = current.hand.toMutableList().apply { removeAt(index) }
            val newField = current.field.toMutableList().apply {
                if (size < 5) add(card)
            }

            val updatedPlayer = current.copy(hand = newHand, field = newField)

            return@update if (state.turnPlayer == state.player1.name) {
                state.copy(player1 = updatedPlayer)
            } else {
                state.copy(player2 = updatedPlayer)
            }
        }
    }

    fun evolveCard(
        index: Int,
        baseCard: CardData,
        evolvedCardData: CardData,
        originalBaseCard: CardData
    ) {
        _battleState.update { state ->
            state ?: return@update null

            val player =
                if (state.turnPlayer == state.player1.name) state.player1 else state.player2

            if (index !in player.field.indices) return@update state
            if (player.field[index].card != baseCard.card) return@update state
            if (player.field[index].isEvolved) return@update state

            val newEvolveDeck = player.evolveDeck.toMutableList()

            val removeIndex = newEvolveDeck.indexOfFirst {
                it.card == evolvedCardData.card && !it.isFaceUp
            }

            if (removeIndex == -1) {
                return@update state
            }

            newEvolveDeck.removeAt(removeIndex)

            val evolvedCard = evolvedCardData.copy(
                isEvolved = true,
                originalCard = originalBaseCard,
                isFaceUp = true,
                act = baseCard.act,
                rotation = baseCard.rotation
            )

            val newField = player.field.toMutableList().apply {
                set(index, evolvedCard)
            }

            val updatedPlayer = player.copy(
                field = newField,
                evolveDeck = newEvolveDeck
            )

            if (state.turnPlayer == state.player1.name) {
                state.copy(player1 = updatedPlayer)
            } else {
                state.copy(player2 = updatedPlayer)
            }
        }
    }

    fun attackWith(card: CardData) {
        machine.attackWith(card)
        _battleState.value = machine.state
    }

    fun endTurn() {
        machine.endTurnPhase()
        _battleState.value = machine.state
    }

    fun selectHandCard(index: Int?) {
        selectionController.selectHandCard(index)
    }

    fun clearSelectedCard() {
        selectionController.clearSelectedHandCard()
    }

    fun showImageFromCard(card: CardData) {
        uiStateController.showImageFromCard(card.expansion, card.image)
    }

    fun playCardFromHand(index: Int) {
        _battleState.update { state ->
            state ?: return@update null
            cardPlayHandler.playCardFromHand(state, index)
        }
    }

    fun spawnTapEffect(position: Offset) {
        uiStateController.spawnTapEffect(position)
    }

    fun setSelectedCardIndex(index: Int?) {
        selectionController.setSelectedCardIndex(index)
    }

    fun clearSelectedCardIndex() {
        selectionController.clearSelectedCardIndex()
    }


    fun rotateFieldCardRight(index: Int) {
        _battleState.update { state ->
            state ?: return@update null

            val current =
                if (state.turnPlayer == state.player1.name) state.player1 else state.player2
            val field = current.field.toMutableList()
            if (index !in field.indices) return@update state

            val rotatedCard = field[index].copy(rotation = 90f)
            field[index] = rotatedCard

            val updatedPlayer = current.copy(field = field)

            return@update if (state.turnPlayer == state.player1.name) {
                state.copy(player1 = updatedPlayer)
            } else {
                state.copy(player2 = updatedPlayer)
            }
        }
    }

    fun setFieldCardAct(index: Int, act: Boolean) {
        _battleState.update { state ->
            state ?: return@update null

            val current =
                if (state.turnPlayer == state.player1.name) state.player1 else state.player2
            val field = current.field.toMutableList()
            if (index !in field.indices) return@update state

            val updatedCard = field[index].copy(act = act)
            field[index] = updatedCard

            val updatedPlayer = current.copy(field = field)

            return@update if (state.turnPlayer == state.player1.name) {
                state.copy(player1 = updatedPlayer)
            } else {
                state.copy(player2 = updatedPlayer)
            }
        }
    }

    fun moveFieldCardToEX(index: Int) {
        _battleState.update { state ->
            state ?: return@update null
            cardMovementHandler.moveFieldCardToEX(state, index)
        }
    }

    fun setSelectedExCardIndex(index: Int?) {
        selectionController.setSelectedExCardIndex(index)
    }

    fun clearSelectedExCardIndex() {
        selectionController.clearSelectedExCardIndex()
    }

    fun playCardFromExArea(index: Int) {
        _battleState.update { state ->
            state ?: return@update null
            val current =
                if (state.turnPlayer == state.player1.name) state.player1 else state.player2
            val exArea = current.exArea.toMutableList()
            val field = current.field.toMutableList()

            if (index !in exArea.indices) return@update state
            if (field.size >= 5) return@update state // 例: フィールド最大5枚

            val card = exArea.removeAt(index)
            field.add(card)

            val updatedPlayer = current.copy(exArea = exArea, field = field)
            return@update if (state.turnPlayer == state.player1.name) {
                state.copy(player1 = updatedPlayer)
            } else {
                state.copy(player2 = updatedPlayer)
            }
        }
    }

    //カード状態のリセット
    fun resetCardState(card: CardData): CardData {
        return cardMovementHandler.resetCardState(card)
    }


    fun clearImageAndMenu() { //メニューや協調表示の全解除
        clearImage()
        clearSelectedCardIndex()
        clearSelectedExCardIndex()
        clearHighlight()
        clearHighlightExCard()
        clearFieldHighlight()
    }

    //PlayerHandArea
    fun highlightCard(index: Int?) {
        selectionController.highlightCard(index)
    }
    fun clearHighlight() {
        selectionController.clearHighlight()
    }

    //PlayerFieldArea
    fun highlightFieldCard(index: Int?) {
        selectionController.highlightFieldCard(index)
    }

    fun clearFieldHighlight() {
        selectionController.clearFieldHighlight()
    }
    //PlayerExArea
    fun highlightExCard(index: Int?) {
        selectionController.highlightExCard(index)
    }

    fun clearHighlightExCard() {
        selectionController.clearHighlightExCard()
    }


    //PlayerGraveyardへ移動
    fun moveFieldCardToGrave(index: Int) {
        _battleState.update { state ->
            state ?: return@update null
            cardMovementHandler.moveFieldCardToGrave(state, index)
        }
    }

    //PlayerBanishAreaへ移動
    fun moveFieldCardToBanish(index: Int) {
        _battleState.update { state ->
            state ?: return@update null
            cardMovementHandler.moveFieldCardToBanish(state, index)
        }
    }

    fun moveEvolvedCardFromField(
        index: Int,
        destination: String,
        state: BattleState
    ): BattleState {
        return cardMovementHandler.moveEvolvedCardFromField(index, destination, state)
    }
}

