package com.example.evolve.ui.screens.input


import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId

/**
 * タッチイベントの種類
 */
enum class TouchType {
    Tap,
    Drag,
    Release
}

/**
 * タッチイベントの情報
 */
data class TouchEvent(
    val position: Offset,
    val type: TouchType,
    val pointerId: PointerId,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * タッチイベントを管理・配信するディスパッチャ
 */
class TouchEventDispatcher {
    private val listeners = mutableStateListOf<(TouchEvent) -> Boolean>()

    fun register(listener: (TouchEvent) -> Boolean) {
        listeners.add(listener)
    }

    fun unregister(listener: (TouchEvent) -> Boolean) {
        listeners.remove(listener)
    }

    /**
     * 登録されたリスナーにイベントを配信する（最後に登録されたものから順に処理）
     * true を返したリスナーがいれば、それ以降には配信しない
     */
    fun dispatch(event: TouchEvent): Boolean {
        for (listener in listeners.reversed()) {
            if (listener(event)) return true
        }
        return false
    }
}
