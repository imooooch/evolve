package com.example.evolve.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.evolve.battle.*
import com.example.evolve.model.CardData
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


@Serializable
data class Deck(val name: String, val cards: List<CardData>)

class BattleViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // ✅ 画像表示用の状態
    private val _selectedImagePath = MutableStateFlow("")
    val selectedImagePath: StateFlow<String> = _selectedImagePath

    fun showImage(path: String) {
        _selectedImagePath.value = path
    }

    fun clearImage() {
        _selectedImagePath.value = ""
    }

    private val _battleState = MutableStateFlow<BattleState?>(null)
    val battleState: StateFlow<BattleState?> = _battleState

    private lateinit var machine: BattleStateMachine

    fun loadBattle(context: Context) {
        val deck1Name = savedStateHandle.get<String>("deck1Name") ?: return
        val deck2Name = savedStateHandle.get<String>("deck2Name") ?: return
        val deck1 = loadDeck(context, deck1Name)
        val deck2 = loadDeck(context, deck2Name)

        if (deck1 != null && deck2 != null) {
            val shuffled1 = deck1.cards
            val shuffled2 = deck2.cards
            val player1 = PlayerState(name = "Player").apply {
                deck.addAll(expandDeck(deck1.cards.filterNot {
                    it.kind.contains("エボルヴ") || it.kind.contains("アドバンス")
                }).shuffled())
                evolveDeck.addAll(expandDeck(shuffled1.filter {
                    it.kind.contains("エボルヴ") || it.kind.contains("アドバンス")
                }))
            }
            val player2 = PlayerState(name = "Opponent").apply {
                deck.addAll(expandDeck(deck2.cards.filterNot {
                    it.kind.contains("エボルヴ") || it.kind.contains("アドバンス")
                }).shuffled())
                evolveDeck.addAll(expandDeck(shuffled2.filter {
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

    fun evolveCard(index: Int, baseCard: CardData, evolvedCardData: CardData) {
        val evolvedCard = evolvedCardData.copy(
            isEvolved = true,
            originalCard = baseCard,
            isFaceUp = true,
            act = baseCard.act,              // ← act状態を引き継ぐ
            rotation = baseCard.rotation     // ← rotationも引き継ぐ
        )


        _battleState.update { state ->
            state ?: return@update null
            val player =
                if (state.turnPlayer == state.player1.name) state.player1 else state.player2

            if (index !in player.field.indices) return@update state
            if (player.field[index].card != baseCard.card || player.field[index].isEvolved) return@update state

            val newField = player.field.toMutableList().apply {
                set(index, evolvedCard)
            }

            val newEvolveDeck = player.evolveDeck.toMutableList().apply {
                removeIf { it.card == evolvedCard.card }
            }

            val updatedPlayer = player.copy(
                field = newField,
                evolveDeck = newEvolveDeck
            )

            return@update if (state.turnPlayer == state.player1.name) {
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

    private fun expandDeck(cards: List<CardData>): List<CardData> {
        return cards.flatMap { card -> List(card.count) { card.copy() } }
    }

    private fun loadDeck(context: Context, deckName: String): Deck? {
        val file = File(context.getExternalFilesDir(null), "$deckName.json")
        return if (file.exists()) {
            try {
                val text = file.readText()
                Json { ignoreUnknownKeys = true }.decodeFromString<Deck>(text)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    private val _selectedHandIndex = MutableStateFlow(-1)
    val selectedHandIndex: StateFlow<Int> = _selectedHandIndex

    fun selectHandCard(index: Int) {
        _selectedHandIndex.value = index
    }

    fun clearSelectedCard() {
        _selectedHandIndex.value = -1
    }

    fun showImageFromCard(card: CardData) {
        Log.d("拡大画像", "showImageFromCard 拡大表示: ${card.name}")
        _selectedImagePath.value = "images/${card.expansion}/${card.image}"
    }

    fun playCardFromHand(index: Int) {
        _battleState.update { state ->
            state ?: return@update null

            val hand = state.player1.hand.toMutableList()
            if (index !in hand.indices) return@update state

            val card = hand[index]
            hand.removeAt(index)

            val field = state.player1.field.toMutableList()
            if ((card.kind.contains("フォロワー") || card.kind.contains("アミュレット")) && field.size < 5) {
                field.add(card) // 同種のカードでOKなためそのまま追加
            }

            state.copy(
                player1 = state.player1.copy(
                    hand = hand,
                    field = field
                )
            )
        }
    }

    private val _tapEffectOffset = MutableStateFlow<Offset?>(null)
    val tapEffectOffset: StateFlow<Offset?> = _tapEffectOffset.asStateFlow()


    fun spawnTapEffect(position: Offset) {
        _tapEffectOffset.value = position
        viewModelScope.launch {
            delay(300L)
            _tapEffectOffset.value = null
        }
    }

    private val _selectedCardIndex = MutableStateFlow(-1)
    val selectedCardIndex: StateFlow<Int> = _selectedCardIndex.asStateFlow()

    fun setSelectedCardIndex(index: Int) {
        _selectedCardIndex.value = index
    }

    fun clearSelectedCardIndex() {
        _selectedCardIndex.value = -1
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

            val current =
                if (state.turnPlayer == state.player1.name) state.player1 else state.player2

            if (index !in current.field.indices) return@update state

            val fieldCard = current.field[index]
            if (fieldCard.isEvolved) {
                return@update moveEvolvedCardFromField(index, "Ex", state) // ← ✅ これに変更
            }

            val field = current.field.toMutableList()
            val ex = current.exArea.toMutableList()

            val rawCard = field.removeAt(index)
            val card = resetCardState(rawCard)
            ex.add(card)

            val updatedPlayer = current.copy(field = field, exArea = ex)

            return@update if (state.turnPlayer == state.player1.name) {
                state.copy(player1 = updatedPlayer)
            } else {
                state.copy(player2 = updatedPlayer)
            }
        }
    }

    private val _selectedExCardIndex = MutableStateFlow(-1)
    val selectedExCardIndex = _selectedExCardIndex.asStateFlow()

    fun setSelectedExCardIndex(index: Int) {
        _selectedExCardIndex.value = index
    }

    fun clearSelectedExCardIndex() {
        _selectedExCardIndex.value = -1
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
        return card.copy(
            act = false,
            rotation = 0f,
            isEvolved = false,
            originalCard = null, // ← ここで常に originalCard を切り離す
            // 必要に応じて他の状態もリセット
        )
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
    private val _highlightCardIndex = MutableStateFlow(-1)
    val highlightCardIndex = _highlightCardIndex.asStateFlow()
    fun highlightCard(index: Int) {
        _highlightCardIndex.value = index
    }

    fun clearHighlight() {
        _highlightCardIndex.value = -1
    }

    //PlayerFieldArea
    private val _highlightFieldCardIndex = MutableStateFlow(-1)
    val highlightFieldCardIndex: StateFlow<Int> = _highlightFieldCardIndex
    fun highlightFieldCard(index: Int) {
        _highlightFieldCardIndex.value = index
    }

    fun clearFieldHighlight() {
        _highlightFieldCardIndex.value = -1
    }

    //PlayerExArea
    private val _highlightExCardIndex = MutableStateFlow(-1)
    val highlightExCardIndex = _highlightExCardIndex.asStateFlow()
    fun highlightExCard(index: Int) {
        _highlightExCardIndex.value = index
    }

    fun clearHighlightExCard() {
        _highlightExCardIndex.value = -1
    }


    //PlayerGraveyardへ移動
    fun moveFieldCardToGrave(index: Int) {
        _battleState.update { state ->
            state ?: return@update null

            val currentPlayer =
                if (state.turnPlayer == state.player1.name) state.player1 else state.player2

            if (index !in currentPlayer.field.indices) return@update state

            val fieldCard = currentPlayer.field[index]
            if (fieldCard.isEvolved) {
                return@update moveEvolvedCardFromField(index, "Graveyard", state) // ← ✅ これに変更
                return@update state
            }

            val updatedField = currentPlayer.field.toMutableList()
            val updatedGraveyard = currentPlayer.graveyard.toMutableList()

            val originalCard = updatedField.removeAt(index)
            val resetCard = resetCardState(originalCard)
            updatedGraveyard.add(resetCard)

            val updatedPlayer = currentPlayer.copy(
                field = updatedField,
                graveyard = updatedGraveyard
            )

            return@update if (state.turnPlayer == state.player1.name) {
                state.copy(player1 = updatedPlayer)
            } else {
                state.copy(player2 = updatedPlayer)
            }
        }
    }

    //PlayerBanishAreaへ移動
    fun moveFieldCardToBanish(index: Int) {
        _battleState.update { state ->
            state ?: return@update null

            val currentPlayer =
                if (state.turnPlayer == state.player1.name) state.player1 else state.player2

            if (index !in currentPlayer.field.indices) return@update state

            val fieldCard = currentPlayer.field[index]

            if (fieldCard.isEvolved || fieldCard.originalCard != null) {
                Log.d("カード移動", "進化カード、${fieldCard.name} を BanishArea に移動")

                return@update moveEvolvedCardFromField(index, "Banish", state) // ← ✅ これに変更
            } else if (!(fieldCard.isEvolved || fieldCard.originalCard != null)) {
                val updatedField = currentPlayer.field.toMutableList()
                val updatedBanish = currentPlayer.banish.toMutableList()

                val originalCard = updatedField.removeAt(index)
                val resetCard = resetCardState(originalCard)
                updatedBanish.add(resetCard)

                Log.d("カード移動", "未進化カード、${resetCard.name} を BanishArea に移動")
                val updatedPlayer = currentPlayer.copy(
                    field = updatedField,
                    banish = updatedBanish
                )

                return@update if (state.turnPlayer == state.player1.name) {
                    state.copy(player1 = updatedPlayer)
                } else {
                    state.copy(player2 = updatedPlayer)
                }
            } else {
                return@update state
            }
        }
    }

    fun overlayEvolveCardOnField(index: Int, evolveCard: CardData) {
        _battleState.update { state ->
            state ?: return@update null

            val currentPlayer =
                if (state.turnPlayer == state.player1.name) state.player1 else state.player2

            if (index !in currentPlayer.field.indices) return@update state

            val updatedField = currentPlayer.field.toMutableList()
            val updatedEvolveDeck = currentPlayer.evolveDeck.toMutableList()

            val baseCard = updatedField[index]
            val evolved = baseCard.copy(
                name = evolveCard.name,
                image = evolveCard.image,
                power = evolveCard.power,
                hp = evolveCard.hp,
                kind = evolveCard.kind,
                type = evolveCard.type,
                evolve = null,
                isEvolved = true // ✅ 進化状態をフラグで管理
            )

            updatedField[index] = evolved
            updatedEvolveDeck.remove(evolveCard)

            val updatedPlayer = currentPlayer.copy(
                field = updatedField,
                evolveDeck = updatedEvolveDeck
            )

            return@update if (state.turnPlayer == state.player1.name) {
                state.copy(player1 = updatedPlayer)
            } else {
                state.copy(player2 = updatedPlayer)
            }
        }
    }

    fun moveEvolvedCardToEvolveDeck(index: Int) {
        _battleState.update { state ->
            state ?: return@update null

            val player =
                if (state.turnPlayer == state.player1.name) state.player1 else state.player2
            val fieldCard = player.field.getOrNull(index) ?: return@update state

            val updatedField = player.field.toMutableList().apply { removeAt(index) }
            val updatedEvolveDeck = player.evolveDeck.toMutableList().apply {
                add(
                    resetCardState(fieldCard).copy(
                        act = false,
                        rotation = 0f,
                        isFaceUp = true,
                        isEvolved = false,
                        originalCard = null
                    )
                )
            }

            val updatedPlayer =
                player.copy(field = updatedField, evolveDeck = updatedEvolveDeck)
            return@update if (state.turnPlayer == state.player1.name) {
                state.copy(player1 = updatedPlayer)
            } else {
                state.copy(player2 = updatedPlayer)
            }
        }
    }

    fun removeEvolvedCardFromField(index: Int, destination: String) {
        _battleState.update { state ->
            state ?: return@update null
            val player =
                if (state.turnPlayer == state.player1.name) state.player1 else state.player2
            val fieldCard = player.field.getOrNull(index) ?: return@update state

            // 進化状態かどうか確認（originalCardがあるかで判定）
            if (fieldCard.originalCard != null) {
                val baseCard = resetCardState(fieldCard.originalCard!!) // 進化前カード
                val evolvedCard = fieldCard.copy(
                    act = false,
                    rotation = 0f,
                    isEvolvedCard = false,
                    originalCard = null,
                    isFaceUp = true
                )

                val updatedField = player.field.toMutableList().apply { removeAt(index) }
                val updatedEvolveDeck =
                    player.evolveDeck.toMutableList().apply { add(evolvedCard) }

                val updatedPlayer = when (destination) {
                    "Graveyard" -> player.copy(
                        field = updatedField,
                        evolveDeck = updatedEvolveDeck,
                        graveyard = player.graveyard.toMutableList().apply { add(baseCard) }
                    )

                    "Banish" -> player.copy(
                        field = updatedField,
                        evolveDeck = updatedEvolveDeck,
                        banish = player.banish.toMutableList().apply { add(baseCard) }
                    )

                    "Deck" -> player.copy(
                        field = updatedField,
                        evolveDeck = updatedEvolveDeck,
                        deck = player.deck.toMutableList().apply { add(baseCard) }
                    )

                    "Hand" -> player.copy(
                        field = updatedField,
                        evolveDeck = updatedEvolveDeck,
                        hand = player.hand.toMutableList().apply { add(baseCard) }
                    )

                    "Ex" -> player.copy(
                        field = updatedField,
                        evolveDeck = updatedEvolveDeck,
                        exArea = player.exArea.toMutableList().apply { add(baseCard) }
                    )


                    else -> player.copy(field = updatedField, evolveDeck = updatedEvolveDeck)
                }

                return@update if (state.turnPlayer == state.player1.name) {
                    state.copy(player1 = updatedPlayer)
                } else {
                    state.copy(player2 = updatedPlayer)
                }
            } else {
                state // 通常カードなので何もせず
            }
        }
    }

    fun moveEvolvedCardFromField(index: Int, destination: String, state: BattleState): BattleState {
        val currentPlayer =
            if (state.turnPlayer == state.player1.name) state.player1 else state.player2
        val fieldCard = currentPlayer.field.getOrNull(index) ?: return state

        if (!fieldCard.isEvolved || fieldCard.originalCard == null) return state

        val originalCard = resetCardState(fieldCard.originalCard!!)
        val evolvedCard = resetCardState(fieldCard).copy(
            act = false,
            rotation = 0f,
            isFaceUp = true,
            isEvolved = false,
            originalCard = null
        )

        val updatedEvolveDeck = currentPlayer.evolveDeck.toMutableList().apply {
            add(evolvedCard.copy(isFaceUp = true)) // 表向きで追加
        }

        val updatedPlayerBeforeField = when (destination) {
            "Graveyard" -> currentPlayer.copy(
                evolveDeck = updatedEvolveDeck,
                graveyard = currentPlayer.graveyard.toMutableList().apply { add(originalCard) }
            )

            "Banish" -> currentPlayer.copy(
                evolveDeck = updatedEvolveDeck,
                banish = currentPlayer.banish.toMutableList().apply { add(originalCard) }
            )

            "Deck" -> currentPlayer.copy(
                evolveDeck = updatedEvolveDeck,
                deck = currentPlayer.deck.toMutableList().apply { add(originalCard) }
            )

            "Hand" -> currentPlayer.copy(
                evolveDeck = updatedEvolveDeck,
                hand = currentPlayer.hand.toMutableList().apply { add(originalCard) }
            )

            "Ex" -> currentPlayer.copy(
                evolveDeck = updatedEvolveDeck,
                exArea = currentPlayer.exArea.toMutableList().apply { add(originalCard) }
            )

            else -> currentPlayer.copy(evolveDeck = updatedEvolveDeck)
        }

        val updatedField = updatedPlayerBeforeField.field.toMutableList().apply {
            removeAt(index)
        }

        val finalPlayer = updatedPlayerBeforeField.copy(field = updatedField)

        return if (state.turnPlayer == state.player1.name) {
            state.copy(player1 = finalPlayer)
        } else {
            state.copy(player2 = finalPlayer)
        }
    }
    val playerEvolveDeckFacedown: StateFlow<List<CardData>?>
        get() = _battleState.map { state ->
            val deck = if (state?.turnPlayer == state?.player1?.name) {
                state?.player1?.evolveDeck
            } else {
                state?.player2?.evolveDeck
            } ?: emptyList()
            deck.filter { !it.isFaceUp } // 裏向きのみ
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

}

