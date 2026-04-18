package com.example.evolve.ui.screens.input

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.positionChangeConsumed
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.zIndex
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.example.evolve.ui.screens.input.*

@Composable
fun TouchInputLayer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dispatcher = LocalTouchEventDispatcher.current

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        val position = change.position
                        val type = when {
                            change.changedToUp() -> TouchType.Release
                            change.pressed -> TouchType.Drag
                            else -> TouchType.Tap
                        }
                        dispatcher.dispatch(
                            TouchEvent(
                                position = position,
                                type = type,
                                pointerId = change.id
                            )
                        )
                    }
                }
            }
            .zIndex(Float.MAX_VALUE) // 最前面でタッチを拾う
    ) {
        content()
    }
}
