package com.example.evolve.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.evolve.data.CardRepository
import com.example.evolve.model.CardModel

class DeckEditViewModel(private val repository: CardRepository) : ViewModel() {
    private val _deckCards = MutableStateFlow<List<CardModel>>(emptyList())
    val deckCards: StateFlow<List<CardModel>> = _deckCards

    fun loadDeckCards() {
        viewModelScope.launch(Dispatchers.IO) { // `Dispatchers.IO` でバックグラウンド処理
            val cards = repository.getAllCards()

            withContext(Dispatchers.Main) {
                if (cards.isEmpty()) {
                }
                _deckCards.value = cards
            }
        }
    }

}
