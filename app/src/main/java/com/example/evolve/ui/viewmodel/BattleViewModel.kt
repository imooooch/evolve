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
import com.example.evolve.battle.AttackHandler
import com.example.evolve.battle.BattleInitializer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.example.evolve.battle.BattleState
import com.example.evolve.battle.BattleStateMachine
import com.example.evolve.battle.BattleTurnController
import com.example.evolve.battle.CardMovementHandler
import com.example.evolve.battle.CardPlayHandler
import com.example.evolve.battle.CardStateHandler
import com.example.evolve.battle.DeckLoader
import com.example.evolve.battle.EvolveHandler
import com.example.evolve.battle.BattleViewMode
import com.example.evolve.battle.ImageDisplaySide
import com.example.evolve.battle.ViewSide
import kotlinx.coroutines.flow.asStateFlow

class BattleViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val deckLoader = DeckLoader()
    private val battleInitializer = BattleInitializer(deckLoader)
    private val turnController = BattleTurnController()
    private val cardStateHandler = CardStateHandler()
    private val cardPlayHandler = CardPlayHandler()
    private val cardMovementHandler = CardMovementHandler()
    private val evolveHandler = EvolveHandler()
    private val attackHandler = AttackHandler()
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

    private val _viewSide = MutableStateFlow(ViewSide.Player1)
    val viewSide = _viewSide.asStateFlow()

    private val _viewMode = MutableStateFlow(BattleViewMode.Versus)
    val viewMode = _viewMode.asStateFlow()

    private val _imageDisplaySide = MutableStateFlow(ImageDisplaySide.Top)
    val imageDisplaySide = _imageDisplaySide.asStateFlow()

    private fun applyFieldMoveByViewSide(
        state: BattleState,
        move: (BattleState) -> BattleState
    ): BattleState {
        val originalTurnPlayer = state.turnPlayer

        val targetPlayerName = when (_viewSide.value) {
            ViewSide.Player1 -> state.player1.name
            ViewSide.Player2 -> state.player2.name
        }

        val tempState = state.copy(
            turnPlayer = targetPlayerName
        )

        return move(tempState).copy(
            turnPlayer = originalTurnPlayer
        )
    }

    fun showImageFromCardOnSide(
        card: CardData,
        side: ImageDisplaySide
    ) {
        _imageDisplaySide.value = side
        showImageFromCard(card)
    }
    fun toggleViewSide() {
        _viewSide.value =
            if (_viewSide.value == ViewSide.Player1) {
                ViewSide.Player2
            } else {
                ViewSide.Player1
            }
    }

    fun toggleViewMode() {
        _viewMode.value =
            if (_viewMode.value == BattleViewMode.Versus) {
                BattleViewMode.Debug
            } else {
                BattleViewMode.Versus
            }
    }

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
        val isFirstPlayer = savedStateHandle.get<Boolean>("isFirstPlayer") ?: false

        val state = battleInitializer.createInitialState(
            context = context,
            deck1Name = deck1Name,
            deck2Name = deck2Name,
            isFirstPlayer = isFirstPlayer
        ) ?: return

        machine = BattleStateMachine(state, context)
        _battleState.value = state
    }

    val playerEvolveDeck: StateFlow<List<CardData>>
        get() = _battleState.map { state ->
            if (state == null) {
                emptyList()
            } else {
                when (_viewSide.value) {
                    ViewSide.Player1 -> state.player1.evolveDeck
                    ViewSide.Player2 -> state.player2.evolveDeck
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun startTurn() {
        _battleState.value?.let {
            _battleState.value = turnController.startTurn(machine)
        }
    }

    fun endTurn() {
        _battleState.value = turnController.endTurn(machine)
    }

    fun evolveCard(
        index: Int,
        baseCard: CardData,
        evolvedCardData: CardData,
        originalBaseCard: CardData
    ) {
        _battleState.update { state ->
            state ?: return@update null

            when (_viewSide.value) {
                ViewSide.Player1 -> {
                    val player = state.player1
                    if (index !in player.field.indices) return@update state
                    if (player.field[index].card != baseCard.card) return@update state
                    if (player.field[index].isEvolved) return@update state

                    val newEvolveDeck = player.evolveDeck.toMutableList()
                    val removeIndex = newEvolveDeck.indexOfFirst {
                        it.card == evolvedCardData.card && !it.isFaceUp
                    }
                    if (removeIndex == -1) return@update state

                    newEvolveDeck.removeAt(removeIndex)

                    val evolvedCard = evolvedCardData.copy(
                        isEvolved = true,
                        originalCard = originalBaseCard,
                        isFaceUp = true,
                        isActed = player.field[index].isActed
                    )

                    val newField = player.field.toMutableList().apply {
                        set(index, evolvedCard)
                    }

                    state.copy(
                        player1 = player.copy(
                            field = newField,
                            evolveDeck = newEvolveDeck
                        )
                    )
                }

                ViewSide.Player2 -> {
                    val player = state.player2
                    if (index !in player.field.indices) return@update state
                    if (player.field[index].card != baseCard.card) return@update state
                    if (player.field[index].isEvolved) return@update state

                    val newEvolveDeck = player.evolveDeck.toMutableList()
                    val removeIndex = newEvolveDeck.indexOfFirst {
                        it.card == evolvedCardData.card && !it.isFaceUp
                    }
                    if (removeIndex == -1) return@update state

                    newEvolveDeck.removeAt(removeIndex)

                    val evolvedCard = evolvedCardData.copy(
                        isEvolved = true,
                        originalCard = originalBaseCard,
                        isFaceUp = true,
                        isActed = player.field[index].isActed
                    )

                    val newField = player.field.toMutableList().apply {
                        set(index, evolvedCard)
                    }

                    state.copy(
                        player2 = player.copy(
                            field = newField,
                            evolveDeck = newEvolveDeck
                        )
                    )
                }
            }
        }
    }

    fun attackWith(card: CardData) {
        _battleState.value = attackHandler.attackWith(machine, card)
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

    fun playCardFromHand(
        index: Int,
        owner: ViewSide
    ) {
        _battleState.update { state ->
            if (state == null) return@update null

            when (owner) {
                ViewSide.Player1 -> {
                    val player = state.player1
                    if (index !in player.hand.indices) return@update state

                    if (player.field.size >= 5) return@update state

                    val card = player.hand[index]
                    val newHand = player.hand.toMutableList().apply {
                        removeAt(index)
                    }

                    val newField = player.field.toMutableList().apply {
                        add(card)
                    }

                    state.copy(
                        player1 = player.copy(
                            hand = newHand,
                            field = newField
                        )
                    )
                }

                ViewSide.Player2 -> {
                    val player = state.player2
                    if (index !in player.hand.indices) return@update state

                    if (player.field.size >= 5) return@update state

                    val card = player.hand[index]
                    val newHand = player.hand.toMutableList().apply {
                        removeAt(index)
                    }

                    val newField = player.field.toMutableList().apply {
                        add(card)
                    }

                    state.copy(
                        player2 = player.copy(
                            hand = newHand,
                            field = newField
                        )
                    )
                }
            }
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

    fun actFieldCard(index: Int) {
        setFieldCardAct(index, true)
    }

    fun setFieldCardAct(index: Int, act: Boolean) {
        _battleState.update { state ->
            state ?: return@update null

            when (_viewSide.value) {
                ViewSide.Player1 -> {
                    val field = state.player1.field.toMutableList()
                    if (index !in field.indices) return@update state

                    field[index] = field[index].copy(isActed = act)

                    state.copy(
                        player1 = state.player1.copy(field = field)
                    )
                }

                ViewSide.Player2 -> {
                    val field = state.player2.field.toMutableList()
                    if (index !in field.indices) return@update state

                    field[index] = field[index].copy(isActed = act)

                    state.copy(
                        player2 = state.player2.copy(field = field)
                    )
                }
            }
        }
    }

    fun moveFieldCardToEX(index: Int) {
        _battleState.update { state ->
            state ?: return@update null

            val targetPlayer = when (_viewSide.value) {
                ViewSide.Player1 -> state.player1
                ViewSide.Player2 -> state.player2
            }

            // EXエリアが5枚以上なら移動しない
            if (targetPlayer.exArea.size >= 5) {
                return@update state
            }

            applyFieldMoveByViewSide(state) { tempState ->
                cardMovementHandler.moveFieldCardToEX(tempState, index)
            }
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

            applyFieldMoveByViewSide(state) { tempState ->
                cardPlayHandler.playCardFromExArea(tempState, index)
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


    fun moveFieldCardToGrave(index: Int) {
        _battleState.update { state ->
            state ?: return@update null

            applyFieldMoveByViewSide(state) { tempState ->
                cardMovementHandler.moveFieldCardToGrave(tempState, index)
            }
        }
    }

    fun moveFieldCardToBanish(index: Int) {
        _battleState.update { state ->
            state ?: return@update null

            applyFieldMoveByViewSide(state) { tempState ->
                cardMovementHandler.moveFieldCardToBanish(tempState, index)
            }
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

