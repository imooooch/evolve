package com.example.evolve.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evolve.data.CardRepository
import com.example.evolve.model.CardModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeckEditViewModel(private val repository: CardRepository) : ViewModel() {
    private val _deckCards = MutableStateFlow<List<CardModel>>(emptyList())
    val deckCards: StateFlow<List<CardModel>> = _deckCards

    fun loadDeckCards(setName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cards = repository.loadCardsFromJson(setName)

            withContext(Dispatchers.Main) {
                _deckCards.value = cards
            }
        }
    }
}