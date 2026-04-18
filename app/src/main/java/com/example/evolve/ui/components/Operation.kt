package com.example.evolve.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.Dp

@Composable
fun SingleClickButton(
    onClick: () -> Unit,
    debounceTimeMillis: Long = 500L,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    border: BorderStroke? = null,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    content: @Composable RowScope.() -> Unit
) {
    var isClickable by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            if (isClickable && enabled) {
                isClickable = false
                scope.launch {
                    try {
                        onClick()
                    } finally {
                        delay(debounceTimeMillis)
                        isClickable = true
                    }
                }
            }
        },
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        border = border,
        elevation = elevation,
        content = content
    )
}
