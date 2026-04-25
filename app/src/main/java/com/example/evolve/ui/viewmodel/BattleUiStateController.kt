package com.example.evolve.ui.viewmodel

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BattleUiStateController(
    private val scope: CoroutineScope
) {
    private val _selectedImagePath = MutableStateFlow("")
    val selectedImagePath: StateFlow<String> = _selectedImagePath.asStateFlow()

    private val _tapEffectOffset = MutableStateFlow<Offset?>(null)
    val tapEffectOffset: StateFlow<Offset?> = _tapEffectOffset.asStateFlow()

    fun showImage(path: String) {
        _selectedImagePath.value = path
    }

    fun clearImage() {
        _selectedImagePath.value = ""
    }

    fun showImageFromCard(expansion: String, image: String?) {
        if (!image.isNullOrBlank()) {
            _selectedImagePath.value = "images/$expansion/$image"
        }
    }

    fun spawnTapEffect(position: Offset) {
        _tapEffectOffset.value = position
        scope.launch {
            delay(300L)
            _tapEffectOffset.value = null
        }
    }
}