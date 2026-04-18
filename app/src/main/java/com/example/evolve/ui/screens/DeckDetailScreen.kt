package com.example.evolve.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.serialization.json.Json
import java.io.File
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.evolve.model.CardData
import com.example.evolve.model.Deck
import com.example.evolve.model.CardInfo
import com.example.evolve.model.CardModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(navController: NavController, deckName: String) {
    val context = LocalContext.current
    val deck = remember { loadDeckFromJson(context, deckName) }

    val normalCards = deck?.cards?.filter {
        !(it.kind.contains("エボルヴ") || it.kind.contains("アドバンス"))
    } ?: emptyList()
    val evolveCards = deck?.cards?.filter {
        it.kind.contains("エボルヴ") || it.kind.contains("アドバンス")
    } ?: emptyList()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(deckName) })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)        ) {
            item {
                Text(
                    text = "Normal",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }
            items(normalCards) { card ->
                Text(
                    text = "${card.count}  ${card.card}  ${card.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    navController.navigate("card_detail/${card.expansion}/${card.image}")
                                }
                            )
                        },
                )
            }

        item {
            Text(
                text = "Evolve",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
        items(evolveCards) { card ->
                    Text(
                        text = "${card.count}  ${card.card}  ${card.name}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        navController.navigate("card_detail/${card.expansion}/${card.image}")
                                    }
                                )
                            },
                    )
                }
            }
        }
    }


fun loadDeckFromJson(context: Context, deckName: String): Deck? {
    val file = File(context.getExternalFilesDir(null), "$deckName.json")
    return if (file.exists()) {
        try {
            Json.decodeFromString<Deck>(file.readText())
        } catch (e: Exception) {
            null
        }
    } else {
        null
    }
}
