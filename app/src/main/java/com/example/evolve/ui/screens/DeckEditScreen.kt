package com.example.evolve.ui.screens


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
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.io.File
import kotlinx.coroutines.launch
import com.example.evolve.model.CardModel
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.Alignment
import java.nio.charset.StandardCharsets
import java.net.URLEncoder
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.RectangleShape
import coil.compose.AsyncImage
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import com.example.evolve.ui.components.SingleClickButton
import com.example.evolve.model.Deck
import com.example.evolve.model.CardData
import androidx.compose.foundation.combinedClickable

fun refreshCountsFromTemp(context: Context, evolveCount: MutableState<Int>, normalCount: MutableState<Int>) {
    val tempDeck = loadTempDeck(context)

    val total = tempDeck.cards.sumOf { it.count }
    val evolve = tempDeck.cards
        .filter { it.kind.contains("エボルヴ") || it.kind.contains("アドバンス") }
        .sumOf { it.count }

    evolveCount.value = evolve
    normalCount.value = total - evolve
}

fun updateTempDeck(context: Context, cardCounts: Map<String, Int>, deckCards: List<CardModel>) {
    val tempFile = File(context.cacheDir, "temp_deck.json")

    val tempDeck = try {
        Json.decodeFromString<Deck>(tempFile.readText())
    } catch (e: Exception) {
        Deck(name = "temp", cards = emptyList())
    }

    val updatedCards = tempDeck.cards.toMutableList()

    cardCounts.forEach { (cardId, count) ->
        val existingCard = updatedCards.find { it.card == cardId }
        if (count > 0) {
            if (existingCard != null) {
                existingCard.count = count
            } else {
                deckCards.find { it.card == cardId }?.let { card ->
                    updatedCards.add(CardData(
                        card = card.card,
                        expansion = card.expansion,
                        cardclass = card.cardclass,
                        name = card.name,
                        rare = card.rare ?: "",
                        kind = card.kind,
                        type = card.type,
                        cost = card.cost ?:0,
                        power = card.power ?:0,
                        hp = card.hp ?:0,
                        ability = card.ability ?: "",
                        evolve = card.evolve ?: "",
                        advance = card.advance ?: "",
                        image = card.image ?: "",
                        count = count,
                        rotation = 0f
                    ))
                }
            }
        } else {
            updatedCards.removeAll { it.card == cardId }
        }
    }

    val updatedDeck = Deck(name = "temp", cards = updatedCards)
    tempFile.writeText(Json.encodeToString(updatedDeck))
}
fun loadSetNamesFromCsv(context: Context): Map<String, String> {
    return try {
        val inputStream = context.assets.open("config/set_names.csv")
        val csvLines = inputStream.bufferedReader().use { it.readLines() }
        csvLines.associate { line ->
            val parts = line.split(",")
            parts[0].trim() to parts.getOrElse(1) { parts[0] }.trim()        }
    } catch (e: Exception) {
        emptyMap()
    }
}

fun loadTempDeck(context: Context): Deck {
    val tempFile = File(context.cacheDir, "temp_deck.json")
    return try {
        Json.decodeFromString<Deck>(tempFile.readText())
    } catch (e: Exception) {
        Deck(name = "temp", cards = emptyList())
    }
}


fun loadCardsFromJson(context: Context, setName: String): List<CardModel> {
    return try {
        val jsonString = context.assets.open("json/$setName.json").bufferedReader().use { it.readText() }
        Json.decodeFromString(jsonString)
    } catch (e: Exception) {
        emptyList()
    }
}


fun getCardClassColor(cardclass: String): Color {
    return when (cardclass) {
        "Elf" -> Color(0xFFDAF2D0)
        "Royal" -> Color(0xFFFFFFCC)
        "Witch" -> Color(0xFFC0E6F5)
        "Dragon" -> Color(0xFFFFDEBD)
        "Nightmare" -> Color(0xFFFFB9B9)
        "Bishop" -> Color(0xFFF2F2F2)
        "Neutral" -> Color(0xFFD0D0D0)
        else -> Color(0xFFBDBDBD) // デフォルト
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DeckEditScreen(navController: NavController, selectedSet: String, initialDeckName: String, from: String)
{
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var deckCards by remember { mutableStateOf<List<CardModel>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    var currentSet by rememberSaveable { mutableStateOf(selectedSet) }
    val displayNames = loadSetNamesFromCsv(context)
    var deckName by rememberSaveable { mutableStateOf(initialDeckName) }
    val cardCounts = remember { mutableStateMapOf<String, Int>() }
    val evolveCount = remember { mutableStateOf(0) }
    val normalCount = remember { mutableStateOf(0) }
    var isInitialized by remember { mutableStateOf(false) }
    val filterClasses = listOf("Elf", "Royal", "Witch", "Dragon", "Nightmare", "Bishop", "Neutral")
    var selectedClasses by rememberSaveable { mutableStateOf(listOf<String>()) }
    val filteredDeckCards = if (selectedClasses.isEmpty()) {
        deckCards.filter {
            !it.kind.contains("トークン") && !it.kind.contains("リーダー")
        }
    } else {
        deckCards.filter { selectedClasses.contains(it.cardclass) }
            .filter {
                !it.kind.contains("トークン") && !it.kind.contains("リーダー")
            }
    }
    val focusManager = LocalFocusManager.current



    LaunchedEffect(currentSet) {
        isInitialized = false

        // ✅ まずリストをクリアして描画更新
        deckCards = emptyList()

        // ✅ 少し遅延を入れるとより確実に描画が反映される
        kotlinx.coroutines.delay(3)

        // ✅ 新しいセットを読み込み
        deckCards = loadCardsFromJson(context, currentSet)

        val tempDeck = loadTempDeck(context)
        cardCounts.clear()
        tempDeck.cards.forEach { tempCard ->
            cardCounts[tempCard.card] = tempCard.count
        }

        refreshCountsFromTemp(context, evolveCount, normalCount)

        isInitialized = true
    }
    fun updateDeckAndRefresh() {
        updateTempDeck(context, cardCounts, deckCards)
        refreshCountsFromTemp(context, evolveCount, normalCount)
    }



    fun saveDeck(context: Context, deckName: String) {
        if (deckName.isEmpty()) {
            println("デッキ名を入力してください。")
            return
        }
        val tempDeck = loadTempDeck(context)
        val deck = Deck(name = deckName, cards = tempDeck.cards)
        val jsonData = Json.encodeToString(deck)
        val file = File(context.getExternalFilesDir(null), "$deckName.json")
        file.writeText(jsonData)

    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { CenterAlignedTopAppBar(title = { Text("デッキ編集") }) },
        bottomBar = {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)) {
                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    label = { Text("デッキ名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true, // ✅ 改行を無効化
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus() // ✅ キーボードを閉じる
                        }
                    )
                )


                Text(
                    text = "デッキ ${normalCount.value}/50枚　エボルヴ ${evolveCount.value}/10枚",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                SingleClickButton(
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9933),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        coroutineScope.launch {
                            if (deckName.isBlank()) {
                                withTimeoutOrNull(1500) { // ✅ 1.5秒で消える
                                    snackbarHostState.showSnackbar("デッキ名を入力してください")
                                }
                                return@launch
                            }
                            saveDeck(context, deckName)
                            withTimeoutOrNull(800) { // ✅ 0.8秒で消える
                                snackbarHostState.showSnackbar("デッキ '$deckName' を作成しました")
                            }
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("デッキを保存")
                }
            }
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val setOptions: List<String> = try {
                val options = context.assets.list("json")?.map { it.removeSuffix(".json") } ?: emptyList()
                options.filter { it in displayNames.keys }
            } catch (e: Exception) {
                emptyList()
            }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it } // ✅ `expanded` を直接変更
            ) {
                OutlinedTextField(
                    value = displayNames[currentSet] ?: currentSet,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("カードセットを選択") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(), // ✅ `menuAnchor()` を追加
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors()
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    setOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(displayNames[option] ?: option) },
                            onClick = {
                                currentSet = option
                                expanded = false // ✅ `expanded = false` を適用
                            }
                        )
                    }
                }

            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                filterClasses.forEach { cardclass ->
                    val isSelected = selectedClasses.contains(cardclass)
                    IconButton(
                        onClick = {
                            selectedClasses = if (isSelected) {
                                selectedClasses - cardclass
                            } else {
                                selectedClasses + cardclass
                            }
                        },
                        modifier = Modifier
                            .background(
                                if (isSelected) Color.LightGray else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        AsyncImage(
                            model = "file:///android_asset/images/icon/icon_${cardclass.lowercase()}.png",
                            contentDescription = cardclass,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredDeckCards) { card ->                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${cardCounts[card.card] ?: 0}",

                            modifier = Modifier
                                .width(32.dp)
                                .align(Alignment.CenterVertically),
                            textAlign = TextAlign.Center,
                        )
                        Column(
                            modifier = Modifier.padding(start = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                border = BorderStroke(2.dp, Color.White),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFC4C4C4
                                    ), contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                onClick = {
                                    if ((cardCounts[card.card] ?: 0) < 3) {
                                        cardCounts[card.card] = (cardCounts[card.card] ?: 0) + 1
                                        updateDeckAndRefresh()
                                    }
                                },
                                modifier = Modifier.size(30.dp, 20.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "+",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(Alignment.CenterVertically)
                                )
                            }

                            Button(
                                border = BorderStroke(2.dp, Color.White),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFC4C4C4
                                    ), contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                onClick = {
                                    if ((cardCounts[card.card] ?: 0) > 0) {
                                        cardCounts[card.card] = (cardCounts[card.card] ?: 0) - 1
                                        updateDeckAndRefresh()
                                    }
                                },
                                modifier = Modifier.size(30.dp, 20.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "-",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(Alignment.CenterVertically)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(getCardClassColor(card.cardclass))
                            .combinedClickable(
                                onClick = {
                                    if ((cardCounts[card.card] ?: 0) < 3) {
                                        cardCounts[card.card] = (cardCounts[card.card] ?: 0) + 1
                                        updateDeckAndRefresh()
                                    }
                                },
                                onLongClick = {
                                    if (isInitialized) {
                                        val encodedSet = URLEncoder.encode(
                                            currentSet,
                                            StandardCharsets.UTF_8.toString()
                                        )
                                        val encodedCardId = URLEncoder.encode(
                                            card.card,
                                            StandardCharsets.UTF_8.toString()
                                        )

                                        navController.navigate("card_detail/$encodedSet/$encodedCardId")
                                    }
                                }
                            )
                    ) {
                        Column {
                            val kindPrefix = buildString {
                                if (card.kind.contains("エボルヴ")) append("E   ")
                                if (card.kind.contains("アドバンス")) append("A  ")
                            }
                            Text(
                                text = "$kindPrefix${card.name}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "コスト: ${card.cost}",
                                color = Color.Black,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    }
                }
            }
        }
    }
}
