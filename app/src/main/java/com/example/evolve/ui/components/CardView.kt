package com.example.evolve.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.evolve.model.CardData

@Composable
fun CardView(card: CardData) {
    val context = LocalContext.current
    val imageBitmap = remember {
        val inputStream = context.assets.open("images/cards/${card.image}")
        BitmapFactory.decodeStream(inputStream).asImageBitmap()
    }

    Image(
        bitmap = imageBitmap,
        contentDescription = card.name,
        modifier = Modifier
            .width(80.dp)
            .height(120.dp),
        contentScale = ContentScale.FillBounds
    )
}
