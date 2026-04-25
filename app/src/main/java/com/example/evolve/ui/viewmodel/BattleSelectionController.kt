package com.example.evolve.ui.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BattleSelectionController {

    private val _selectedHandIndex = MutableStateFlow<Int?>(null)
    val selectedHandIndex: StateFlow<Int?> = _selectedHandIndex.asStateFlow()

    private val _selectedCardIndex = MutableStateFlow<Int?>(null)
    val selectedCardIndex: StateFlow<Int?> = _selectedCardIndex.asStateFlow()

    private val _selectedExCardIndex = MutableStateFlow<Int?>(null)
    val selectedExCardIndex: StateFlow<Int?> = _selectedExCardIndex.asStateFlow()

    private val _highlightCardIndex = MutableStateFlow<Int?>(null)
    val highlightCardIndex: StateFlow<Int?> = _highlightCardIndex.asStateFlow()

    private val _highlightFieldCardIndex = MutableStateFlow<Int?>(null)
    val highlightFieldCardIndex: StateFlow<Int?> = _highlightFieldCardIndex.asStateFlow()

    private val _highlightExCardIndex = MutableStateFlow<Int?>(null)
    val highlightExCardIndex: StateFlow<Int?> = _highlightExCardIndex.asStateFlow()

    fun selectHandCard(index: Int?) {
        _selectedHandIndex.value = index
    }

    fun clearSelectedHandCard() {
        _selectedHandIndex.value = null
    }

    fun setSelectedCardIndex(index: Int?) {
        _selectedCardIndex.value = index
    }

    fun clearSelectedCardIndex() {
        _selectedCardIndex.value = null
    }

    fun setSelectedExCardIndex(index: Int?) {
        _selectedExCardIndex.value = index
    }

    fun clearSelectedExCardIndex() {
        _selectedExCardIndex.value = null
    }

    fun highlightCard(index: Int?) {
        _highlightCardIndex.value = index
    }

    fun clearHighlight() {
        _highlightCardIndex.value = null
    }

    fun highlightFieldCard(index: Int?) {
        _highlightFieldCardIndex.value = index
    }

    fun clearFieldHighlight() {
        _highlightFieldCardIndex.value = null
    }

    fun highlightExCard(index: Int?) {
        _highlightExCardIndex.value = index
    }

    fun clearHighlightExCard() {
        _highlightExCardIndex.value = null
    }

    fun clearAllSelections() {
        _selectedHandIndex.value = null
        _selectedCardIndex.value = null
        _selectedExCardIndex.value = null
        _highlightCardIndex.value = null
        _highlightFieldCardIndex.value = null
        _highlightExCardIndex.value = null
    }
}