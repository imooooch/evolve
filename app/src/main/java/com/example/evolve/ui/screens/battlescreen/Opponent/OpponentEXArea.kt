package com.example.evolve.ui.screens.battlescreen.Opponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.evolve.ui.screens.exAreaHeightRatio
import com.example.evolve.ui.screens.exAreaWidthRatio
import com.example.evolve.ui.screens.exCardSpacingRatio
import com.example.evolve.ui.screens.exCardWidthRatio

@Composable
fun OpponentEXArea(
    modifier: Modifier = Modifier,
    exCardCount: Int = 5
) {
    val screenWidth = LocalDensity.current.run { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val screenHeight = LocalDensity.current.run { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val density = LocalDensity.current
    val exCardWidth = with(density) { (screenWidth * exCardWidthRatio).toDp() }
    val exCardHeight = with(density) { (screenHeight * exAreaHeightRatio).toDp() }
    val exCardSpacing = with(density) { (screenWidth * exCardSpacingRatio).toDp() }

    Row(
        modifier = modifier
            .fillMaxWidth(exAreaWidthRatio)
            .height(exCardHeight)
            .graphicsLayer(rotationZ = 180f),
        horizontalArrangement = Arrangement.spacedBy(exCardSpacing)
    ) {
        repeat(exCardCount) { index ->
            Box(
                modifier = Modifier
                    .size(exCardWidth, exCardHeight)
                    .background(Color.Magenta.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
            }
        }
    }
}