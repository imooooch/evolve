package com.example.evolve.ui.screens.input

import androidx.compose.runtime.compositionLocalOf

/**
 * TouchEventDispatcher を CompositionLocal として提供するための定義
 */
val LocalTouchEventDispatcher = compositionLocalOf<TouchEventDispatcher> {
    error("TouchEventDispatcher is not provided")
}
