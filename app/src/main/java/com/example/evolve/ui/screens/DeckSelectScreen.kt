package com.example.evolve.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import androidx.compose.ui.graphics.Color
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.evolve.ui.components.SingleClickButton
import com.example.evolve.model.CardData
import com.example.evolve.model.Deck
import com.example.evolve.model.DeckModel
import com.example.evolve.model.CardModel
import com.example.evolve.ui.screens.loadCardsFromJson
import kotlinx.serialization.encodeToString

fun loadDecks(context: Context): List<Deck> {
    val filesDir = context.getExternalFilesDir(null)
    val deckFiles = filesDir?.listFiles { _, name -> name.endsWith(".json") }
        ?.sortedByDescending { it.lastModified() } ?: emptyList()

    return deckFiles.mapNotNull { file ->
        try {
            Json.decodeFromString<Deck>(file.readText())
        } catch (e: Exception) {
            null
        }
    }
}

fun loadAndRefreshDeck(context: Context, deckName: String): Deck? {
    val filesDir = context.getExternalFilesDir(null) ?: return null
    val file = File(filesDir, "$deckName.json")
    if (!file.exists()) return null

    val originalDeck = try {
        Json.decodeFromString<Deck>(file.readText())
    } catch (e: Exception) {
        return null
    }

    Log.d("DeckUpdate", "対象デッキ: $deckName, カード数: ${originalDeck.cards.size}")
    originalDeck.cards.forEach {
        Log.d("DeckUpdate", "元カード: ${it.card}, count=${it.count}")
    }

    val updatedCards = originalDeck.cards.mapNotNull { deckCard ->
        val cardId = deckCard.card
        val expansion = cardId.substringBefore("-").uppercase()
        val baseCards = loadCardsFromJson(context, expansion)
        Log.d("DeckUpdate", "展開セット: $expansion, 読み込んだカード数: ${baseCards.size}")

        val fullData = baseCards.firstOrNull { it.card == cardId }
        fullData?.let { card ->
            CardData(
                card = card.card,
                expansion = card.expansion,
                cardclass = card.cardclass,
                name = card.name,
                rare = card.rare ?: "",
                kind = card.kind,
                type = card.type,
                cost = card.cost ?: 0,
                power = card.power ?: 0,
                hp = card.hp ?: 0,
                ability = card.ability ?: "",
                evolve = card.evolve ?: "",
                advance = card.advance ?: "",
                image = card.image ?: "",
                count = deckCard.count,
                rotation = 0f
            )
        }
    }
    val updatedDeck = Deck(name = deckName, cards = updatedCards)
    if (updatedCards.isNotEmpty()) {
        file.writeText(Json.encodeToString(updatedDeck))
    } else {
        Log.e("DeckUpdate", "更新後のカードリストが空のため、保存をスキップしました")
        return null
    }
    return updatedDeck
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckSelectScreen(navController: NavController) {
    val context = LocalContext.current
    var savedDecks by remember { mutableStateOf(loadDecks(context)) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val filterClasses = listOf("Elf", "Royal", "Witch", "Dragon", "Nightmare", "Bishop")
    var selectedClasses by rememberSaveable { mutableStateOf(listOf<String>()) }
    val displayedDecks = savedDecks.filter { deck ->
        if (selectedClasses.isEmpty()) true
        else deck.cards.any { selectedClasses.contains(it.cardclass) }
    }
    val selectedDecks = remember { mutableStateListOf<Deck?>(null, null) }
    var selectedSlotIndex by remember { mutableStateOf<Int?>(null) }
    var isFirstPlayer by remember { mutableStateOf(true) }




    LaunchedEffect(Unit) {
        val tempFile = File(context.cacheDir, "temp_deck.json")
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                CenterAlignedTopAppBar(title = { Text("保存デッキ一覧") })
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
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                items(displayedDecks) { deck ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        navController.navigate("deck_detail/${deck.name}")
                                    }
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = deck.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 150.dp) // ✅ 必要に応じて調整
                                )
                                Row(
                                    verticalAlignment = Alignment.Bottom // ✅ 揃え変更
                                ) {
                                    deck.cards.map { it.cardclass }
                                        .distinct()
                                        .forEach { cardclass ->
                                            AsyncImage(
                                                model = "file:///android_asset/images/icon/icon_${cardclass.lowercase()}.png",
                                                contentDescription = "Cardclass Icon",
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .padding(end = 4.dp)
                                            )
                                        }
                                    Spacer(modifier = Modifier.width(4.dp))

                                    val normalCount = deck.cards.filter {
                                        !(it.kind.contains("エボルヴ") || it.kind.contains("アドバンス"))
                                    }.sumOf { it.count }

                                    val evolveCount = deck.cards.filter {
                                        it.kind.contains("エボルヴ") || it.kind.contains("アドバンス")
                                    }.sumOf { it.count }

                                    val normalCountColor = if (normalCount in 40..50) Color.Black else Color.Red
                                    val normalCountFontWeight = if (normalCount in 40..50) FontWeight.Normal else FontWeight.Bold

                                    val evolveCountColor = if (evolveCount in 0..10) Color.Black else Color.Red
                                    val evolveCountFontWeight = if (evolveCount in 0..10) FontWeight.Normal else FontWeight.Bold

                                    Text(
                                        text = "$normalCount",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = normalCountFontWeight
                                        ),
                                        color = normalCountColor,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = " / ",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Black,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "$evolveCount 枚",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = evolveCountFontWeight
                                        ),
                                        color = evolveCountColor,
                                        maxLines = 1
                                    )
                                }

                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val normalCount = deck.cards.filter {
                                    !(it.kind.contains("エボルヴ") || it.kind.contains("アドバンス"))
                                }.sumOf { it.count }

                                val evolveCount = deck.cards.filter {
                                    it.kind.contains("エボルヴ") || it.kind.contains("アドバンス")
                                }.sumOf { it.count }

                                val isNormalCountError = normalCount !in 40..50
                                val isEvolveCountError = evolveCount !in 0..10
                                val isCountError = isNormalCountError || isEvolveCountError
                                Button(
                                    onClick = {
                                        if (selectedSlotIndex != null) {
                                            selectedDecks[selectedSlotIndex!!] = deck
                                            selectedSlotIndex = null // ✅ 選択完了後にリセット
                                        } else if (selectedDecks[0] == null) {
                                            selectedDecks[0] = deck
                                            // ✅ 先攻/後攻の変更なし（ユーザーの選択を保持）
                                        } else if (selectedDecks[1] == null) {
                                            selectedDecks[1] = deck
                                            // ✅ 先攻/後攻の変更なし（ユーザーの選択を保持）
                                        }
                                    },
                                    enabled = !isCountError,
                                    colors = ButtonDefaults.buttonColors(containerColor = if (!isCountError) Color.Gray else Color.LightGray),
                                    modifier = Modifier.size(50.dp, 35.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("選択", color = if (!isCountError) Color.White else Color.LightGray)
                                }

                                SingleClickButton(
                                    onClick = {
                                        val sourceFile =
                                            File(context.getExternalFilesDir(null), "${deck.name}.json")
                                        val tempFile = File(context.cacheDir, "temp_deck.json")
                                        sourceFile.copyTo(tempFile, overwrite = true)

                                        // temp_deck.jsonからexpansionを取得
                                        val tempDeck = Json.decodeFromString<Deck>(tempFile.readText())
                                        val expansion =
                                            tempDeck.cards.firstOrNull()?.expansion ?: "BP01"

                                        navController.navigate("deck_edit/$expansion/${deck.name}/deck_select")
                                    },
                                    modifier = Modifier.size(50.dp, 35.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)

                                ) {
                                    Text("編集", color = Color.White)
                                }

                                SingleClickButton(
                                    onClick = {
                                        val file =
                                            File(context.getExternalFilesDir(null), "${deck.name}.json")
                                        if (file.exists()) {
                                            file.delete()
                                            savedDecks = loadDecks(context)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("デッキ '${deck.name}' を削除しました")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    modifier = Modifier.size(50.dp, 35.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("削除", color = Color.White)
                                }
                            }
                        }
                    }

                }
            }

            // ✅ デッキ登録枠
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFirstPlayer) "　先攻" else "　後攻",
                    color = Color.White,
                    modifier = Modifier.padding(end = 20.dp),
                    fontSize = 24.sp
                )
                Switch(
                    checked = isFirstPlayer,
                    onCheckedChange = { isFirstPlayer = it } // ✅ スイッチの操作でのみ変更
                )
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                selectedDecks.forEachIndexed { index, deck ->
                    val isSelectedSlot = selectedSlotIndex == index
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .height(60.dp)
                            .clickable { selectedSlotIndex = index },
                        border = BorderStroke(1.dp, Color.Black),
                        colors = CardDefaults.cardColors
                            (
                            containerColor = if (isSelectedSlot) Color.LightGray else Color.White
                        )
                    )
                    {
                        if (deck != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                AsyncImage(
                                    model = "file:///android_asset/images/icon/icon_${deck.cards.firstOrNull()?.cardclass?.lowercase()}.png",
                                    contentDescription = "Cardclass Icon",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    color = Color.Black,
                                    text = deck.name,
                                    style = MaterialTheme.typography.labelLarge, // ✅ 大きめに
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(color = Color.Black,
                                    text = "未登録")
                            }
                        }
                    }
                }
            }
            // ✅ 戦闘開始ボタン
            SingleClickButton(
                onClick = {
                    if (selectedDecks[0] != null && selectedDecks[1] != null) {
                        // 🔄 各デッキを最新カードデータで更新して保存
                        val deck1Name = selectedDecks.getOrNull(0)?.name ?: ""
                        val deck2Name = selectedDecks.getOrNull(1)?.name ?: ""

                        loadAndRefreshDeck(context, deck1Name)
                        loadAndRefreshDeck(context, deck2Name)


                        if (deck1Name.isNotEmpty() && deck2Name.isNotEmpty()) {
                            navController.navigate("BattleScreen/$deck1Name/$deck2Name/$isFirstPlayer")
                        } else {
                            Log.e("DeckSelectScreen", "Error: One or both selected decks are null. Deck1: $deck1Name, Deck2: $deck2Name")
                        }

                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text("ゲーム開始")
            }
        }
    }
}

