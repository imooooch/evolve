    package com.example.evolve.ui.screens

    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.remember
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.asImageBitmap
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.platform.LocalDensity
    import androidx.navigation.NavController
    import android.graphics.BitmapFactory
    import android.util.Log
    import androidx.compose.ui.unit.dp
    import androidx.compose.material3.Text
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.sp
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.animation.AnimatedVisibility
    import androidx.compose.foundation.gestures.detectTapGestures
    import androidx.compose.runtime.*
    import androidx.compose.ui.graphics.ImageBitmap
    import androidx.compose.ui.graphics.graphicsLayer
    import androidx.compose.ui.input.pointer.pointerInput
    import androidx.compose.ui.platform.LocalConfiguration
    import androidx.compose.ui.zIndex
    import com.example.evolve.ui.viewmodel.BattleViewModel
    import androidx.lifecycle.viewmodel.compose.viewModel
    import com.example.evolve.ui.components.SingleClickButton
    import com.example.evolve.model.CardData
    import com.example.evolve.ui.utils.loadCardImage
    import com.example.evolve.ui.screens.battlescreen.Player.*
    import com.example.evolve.ui.screens.battlescreen.Opponent.*

    val exAreaWidthRatio = 0.65f // EXエリア全体の幅
    val exAreaHeightRatio = 0.08f // EXエリアの高さ
    val exCardSpacingRatio = 0.01f // EXカード間の間隔
    val exCardCount = 5
    val exCardWidthRatio = (exAreaWidthRatio - (exCardSpacingRatio * (exCardCount - 1))) / exCardCount
    // 各カードサイズ比率
    val cardWidthRatio = 0.12f
    val cardHeightRatio = 0.08f
    // 間隔比率
    val horizontalSpacingRatio = 0.1f
    val verticalSpacingRatio = 0.01f

    @Composable
    fun BattleScreen(
        navController: NavController,
        deck1Name: String,
        deck2Name: String,
        isFirstPlayer: Boolean,
        viewModel: BattleViewModel = viewModel()
    ) {
        val context = LocalContext.current

        LaunchedEffect(deck1Name, deck2Name) {
            Log.d("BattleScreen", "BattleScreen で loadBattle() を呼び出し: deck1Name=$deck1Name, deck2Name=$deck2Name")
            if (deck1Name.isNotEmpty() && deck2Name.isNotEmpty()) {
                viewModel.loadBattle(context)
            } else {
                Log.e("BattleScreen", "デッキ名が正しく渡されていません")
            }
        }
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val viewModel: BattleViewModel = viewModel()
            val screenWidth = constraints.maxWidth.toFloat()
            val screenHeight = constraints.maxHeight.toFloat()
            val density = LocalDensity.current
            val areaWidthRatio = 0.12f
            val areaHeightRatio = 0.08f
            val textBoxWidthRatio = 0.12f
            val textBoxHeightRatio = 0.035f
            val CountWidthRatio = 0.12f
            val CountHeightRatio = 0.025f
            val battleState by viewModel.battleState.collectAsState()
            val playerEpCount = battleState?.player1?.ep ?: 0
            val opponentEpCount = battleState?.player2?.ep ?: 0
            val playerHand = battleState?.player1?.hand ?: emptyList()
            val opponentHand = battleState?.player2?.hand ?: emptyList()
            val playerLeaderHp = battleState?.player1?.leaderHp ?: 0
            val opponentLeaderHp = battleState?.player2?.leaderHp ?: 0
            val playerCurrentPP = battleState?.player1?.currentPP ?: 0
            val playerMaxPP = battleState?.player1?.maxPP ?: 0
            val opponentCurrentPP = battleState?.player2?.currentPP ?: 0
            val opponentMaxPP = battleState?.player2?.maxPP ?: 0
            val playerDeckCount = battleState?.player1?.deck?.size ?: 0
            val playerBanishCount = battleState?.player1?.banished?.size ?: 0
            val playerGraveyardCount = battleState?.player1?.graveyard?.size ?: 0
            val playerEvolveDeckCount = battleState?.player1?.evolveDeck?.size ?: 0
            val opponentDeckCount = battleState?.player2?.deck?.size ?: 0
            val opponentBanishCount = battleState?.player2?.banished?.size ?: 0
            val opponentGraveyardCount = battleState?.player2?.graveyard?.size ?: 0
            val opponentEvolveDeckCount = battleState?.player2?.evolveDeck?.size ?: 0
            val isEvolveDeckOccupied = battleState?.player1?.evolveDeck?.isNotEmpty() == true
            val isGraveyardOccupied = battleState?.player1?.graveyard?.isNotEmpty() == true
            val isBanishOccupied = battleState?.player1?.banished?.isNotEmpty() == true
            val isLeaderOccupied = true // 表示用trueで可
            val isOpponentEvolveDeckOccupied = battleState?.player2?.evolveDeck?.isNotEmpty() == true
            val isOpponentGraveyardOccupied = battleState?.player2?.graveyard?.isNotEmpty() == true
            val isOpponentBanishOccupied = battleState?.player2?.banished?.isNotEmpty() == true
            val isOpponentLeaderOccupied = true
            val playerHandCards = remember { mutableStateListOf<CardData>() }
            val deckBackBitmap = remember {
                val inputStream = context.assets.open("images/battle/Back.jpg")
                BitmapFactory.decodeStream(inputStream).asImageBitmap()
            }
            val selectedImagePath by viewModel.selectedImagePath.collectAsState()
            val selectedHandIndex by viewModel.selectedHandIndex.collectAsState()
            val graveyardCards = battleState?.player1?.graveyard ?: emptyList()
            val lastCard = graveyardCards.lastOrNull()
            val evolveDeck = battleState?.player1?.evolveDeck?: emptyList()
            val evolveCards = battleState?.player1?.evolveDeck?: emptyList()
            val banishCards = battleState?.player1?.banish?: emptyList()
            val lastBanishCard = banishCards.lastOrNull()

            // ✅ 共有画像表示エリア（他機能でも利用可能）
            Image(
                bitmap = remember {
                    val inputStream = context.assets.open("images/battle/table.png")
                    BitmapFactory.decodeStream(inputStream)
                }.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
            EPStatusArea(
                modifier = Modifier
                    .offset(x = 5.dp, y = with(density) { (screenHeight * 0.49f).toDp() })
                    .align(Alignment.TopStart),
                epCount = playerEpCount,
                isVisibleList = List(playerEpCount) { true }
                )
            EPStatusArea(
                modifier = Modifier
                    .offset(x = -5.dp, y = with(density) { (screenHeight * 0.454f).toDp() })
                    .align(Alignment.TopEnd),
                epCount = opponentEpCount,
                isVisibleList = List(opponentEpCount) { true },
                rotation = 180f
            )
            OpponentHandArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * 0.7f).toDp() },
                        height = with(density) { (screenHeight * 0.12f).toDp() }
                    )
                    .offset(y = with(density) { (screenHeight * 0.07f).toDp() })
                    .align(Alignment.TopCenter),
                cards = opponentHand
            )
            OpponentEXArea(
                modifier = Modifier
                    .offset(y = with(density) { (screenHeight * 0.196f).toDp() })
                    .align(Alignment.TopCenter)
            )
            OpponentFieldArea(
                modifier = Modifier
                    .offset(y = with(density) { (screenHeight * 0.308f).toDp() })
                    .align(Alignment.TopCenter),
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )
            OpponentGraveyardArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * areaWidthRatio).toDp() },
                        height = with(density) { (screenHeight * areaHeightRatio).toDp() }
                    )
                    .offset(x = 10.dp, y = with(density) { (screenHeight * 0.123f).toDp() })
                    .align(Alignment.TopStart),
                hasCard = isOpponentGraveyardOccupied
            )
            OpponentBanishArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * areaWidthRatio).toDp() },
                        height = with(density) { (screenHeight * areaHeightRatio).toDp() }
                    )
                    .offset(x = -10.dp, y = with(density) { (screenHeight * 0.123f).toDp() })
                    .align(Alignment.TopEnd),
                hasCard = isOpponentBanishOccupied
            )
            OpponentDeckArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * 0.12f).toDp() },
                        height = with(density) { (screenHeight * 0.08f).toDp() }
                    )
                    .offset(x = 10.dp, y = with(density) { (screenHeight * 0.239f).toDp() })
                    .align(Alignment.TopStart),
                count = opponentDeckCount
            )
            OpponentEvolveDeckArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * areaWidthRatio).toDp() },
                        height = with(density) { (screenHeight * areaHeightRatio).toDp() }
                    )
                    .offset(x = -10.dp, y = with(density) { (screenHeight * 0.239f).toDp() })
                    .align(Alignment.TopEnd),
                hasCard = isOpponentEvolveDeckOccupied
            )
            OpponentPPStatusArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * CountWidthRatio).toDp() },
                        height = with(density) { (screenHeight * CountHeightRatio).toDp() }
                    )
                    .offset(x = 10.dp, y = with(density) { (screenHeight * 0.328f).toDp() })
                    .align(Alignment.TopStart)
                    .background(Color.Red.copy(alpha = 0.7f)),
                currentPP = opponentCurrentPP, maxPP = opponentMaxPP
            )
            OpponentLeaderArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * areaWidthRatio).toDp() },
                        height = with(density) { (screenHeight * areaHeightRatio).toDp() }
                    )
                    .offset(x = 10.dp, y = with(density) { (screenHeight * 0.36f).toDp() })
                    .align(Alignment.TopStart),
                hasCard = isOpponentLeaderOccupied
            )
            OpponentHealthArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * textBoxWidthRatio).toDp() },
                        height = with(density) { (screenHeight * textBoxHeightRatio).toDp() }
                    )
                    .offset(x = 10.dp, y = with(density) { (screenHeight * 0.445f).toDp() })
                    .align(Alignment.TopStart)
                    .background(Color.Yellow.copy(alpha = 0.7f)),
                health = opponentLeaderHp
            )
            OpponentDeckCountArea(
                modifier = Modifier
                    .size(
                        width = with(LocalDensity.current) { (screenWidth * CountWidthRatio).toDp() },
                        height = with(LocalDensity.current) { (screenHeight * CountHeightRatio).toDp() }
                    )
                    .offset(x = 5.dp, y = with(LocalDensity.current) { (screenHeight * 0.215f).toDp() }) // 相手用の位置調整
                    .align(Alignment.TopStart),
                count = opponentDeckCount
            )
            OpponentGraveyardCountArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * CountWidthRatio).toDp() },
                        height = with(density) { (screenHeight * CountHeightRatio).toDp() }
                    )                .offset(x = 5.dp, y = with(density) { (screenHeight * 0.1f).toDp() })
                    .align(Alignment.TopStart),
                count = opponentGraveyardCount
            )
            OpponentBanishCountArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * CountWidthRatio).toDp() },
                        height = with(density) { (screenHeight * CountHeightRatio).toDp() }
                    )                .offset(x = -5.dp, y = with(density) { (screenHeight * 0.1f).toDp() })
                    .align(Alignment.TopEnd),
                count = opponentBanishCount
            )
            OpponentEvolveDeckCountArea(
                modifier = Modifier
                    .size(
                        width = with(LocalDensity.current) { (screenWidth * CountWidthRatio).toDp() },
                        height = with(LocalDensity.current) { (screenHeight * CountHeightRatio).toDp() }
                    )
                    .offset(x = -5.dp, y = with(LocalDensity.current) { (screenHeight * 0.215f).toDp() }) // 相手用の位置調整
                    .align(Alignment.TopEnd),
                count = opponentEvolveDeckCount
            )
            PlayerHealthArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * textBoxWidthRatio).toDp() },
                        height = with(density) { (screenHeight * textBoxHeightRatio).toDp() }
                    )
                    .offset(x = -10.dp, y = with(density) { (screenHeight * 0.492f).toDp() })
                    .align(Alignment.TopEnd)
                    .background(Color.Yellow.copy(alpha = 0.5f)),
                health = playerLeaderHp
            )
            PlayerLeaderArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * areaWidthRatio).toDp() },
                        height = with(density) { (screenHeight * areaHeightRatio).toDp() }
                    )
                    .offset(x = -10.dp, y = with(density) { (screenHeight * 0.532f).toDp() })
                    .align(Alignment.TopEnd),
                hasCard = isLeaderOccupied
            )
            PlayerPPStatusArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * CountWidthRatio).toDp() },
                        height = with(density) { (screenHeight * CountHeightRatio).toDp() }
                    )
                    .offset(x = -10.dp, y = with(density) { (screenHeight * 0.619f).toDp() })
                    .align(Alignment.TopEnd)
                    .background(Color.Red.copy(alpha = 0.7f)),
                currentPP = playerCurrentPP, maxPP = playerMaxPP
            )
            PlayerFieldArea(
                modifier = Modifier
                    .offset(y = with(density) { (screenHeight * 0.495f).toDp() })
                    .align(Alignment.TopCenter),
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                fieldCards = viewModel.battleState.value?.player1?.field ?: emptyList(),
                viewModel = viewModel // ✅ 忘れずに渡す
            )
            PlayerEvolveDeckArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * areaWidthRatio).toDp() },
                        height = with(density) { (screenHeight * areaHeightRatio).toDp() }
                    )
                    .offset(x = 10.dp, y = with(density) { (screenHeight * 0.653f).toDp() })
                    .align(Alignment.TopStart),
                hasCard = evolveCards.isNotEmpty(),
                evolveCards = evolveCards
            )
            PlayerDeckArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * 0.12f).toDp() },
                        height = with(density) { (screenHeight * 0.08f).toDp() }
                    )
                    .offset(x = -10.dp, y = with(density) { (screenHeight * 0.653f).toDp() })
                    .align(Alignment.TopEnd),
                count = playerDeckCount
            )

            val facedownCount = evolveCards.count { !it.isFaceUp }
            PlayerEvolveDeckCountArea(
                modifier = Modifier
                    .size(
                        width = with(LocalDensity.current) { (screenWidth * CountWidthRatio).toDp() },
                        height = with(LocalDensity.current) { (screenHeight * CountHeightRatio).toDp() }
                    )
                    .offset(x = 5.dp, y = with(LocalDensity.current) { (screenHeight * 0.734f).toDp() })
                    .align(Alignment.TopStart),
                facedownCount = facedownCount
            )


            PlayerDeckCountArea(
                modifier = Modifier
                    .size(
                        width = with(LocalDensity.current) { (screenWidth * CountWidthRatio).toDp() },
                        height = with(LocalDensity.current) { (screenHeight * CountHeightRatio).toDp() }
                    )
                    .offset(x = -5.dp, y = with(LocalDensity.current) { (screenHeight * 0.734f).toDp() })
                    .align(Alignment.TopEnd),
                count = playerDeckCount
            )
            PlayerEXArea(
                viewModel = viewModel,  // ✅ 追加
                modifier = Modifier
                    .offset(y = with(density) { (screenHeight * 0.699f).toDp() })
                    .align(Alignment.TopCenter),
                exCards = viewModel.battleState.value?.player1?.exArea ?: emptyList(),
            )

            PlayerBanishArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * areaWidthRatio).toDp() },
                        height = with(density) { (screenHeight * areaHeightRatio).toDp() }
                    )
                    .offset(x = 10.dp, y = with(density) { (screenHeight * 0.771f).toDp() })
                    .align(Alignment.TopStart),

                hasCard = banishCards.isNotEmpty(),
                lastCard = lastBanishCard,
                banishCards = banishCards
            )

            PlayerGraveyardArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * areaWidthRatio).toDp() },
                        height = with(density) { (screenHeight * areaHeightRatio).toDp() }
                    )
                    .offset(x = -10.dp, y = with(density) { (screenHeight * 0.771f).toDp() })
                    .align(Alignment.TopEnd),
                hasCard = graveyardCards.isNotEmpty(),
                lastCard = lastCard,
                graveyardCards = graveyardCards
            )
            PlayerBanishCountArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * CountWidthRatio).toDp() },
                        height = with(density) { (screenHeight * CountHeightRatio).toDp() }
                    )                .offset(x = 5.dp, y = with(density) { (screenHeight * 0.850f).toDp() })
                    .align(Alignment.TopStart),
                count = banishCards.size
            )
            PlayerGraveyardCountArea(
                modifier = Modifier
                    .size(
                        width = with(density) { (screenWidth * CountWidthRatio).toDp() },
                        height = with(density) { (screenHeight * CountHeightRatio).toDp() }
                    )                .offset(x = -5.dp, y = with(density) { (screenHeight * 0.850f).toDp() })
                    .align(Alignment.TopEnd),
                count = playerGraveyardCount
            )
            PlayerHandArea(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomCenter),
                cards = playerHand,
                viewModel = viewModel,
                onCardPlayed = { index ->
                    viewModel.playCardFromHand(index)
                    viewModel.clearImageAndMenu()
                }
            )

            if (selectedImagePath.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.45f) // ✅ 画面の上半分だけタップをブロック
                        .zIndex(100f)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                viewModel.clearImageAndMenu()
                            }
                        }
                ) {
                    Image(
                        bitmap = loadCardImage(selectedImagePath),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(400.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = 16.dp)
                    )
                }
            }

            // 🔲 ここに表示画像を切り替えられるようにする（例: 選択中カード、能力説明など）
            // Image(bitmap = ..., contentDescription = ..., modifier = Modifier.size(...))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color(0xFF4E2C2E))
            ) {
                // ✅ ターン終了ボタン
                SingleClickButton(
                    onClick = {
                        viewModel.endTurn()
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                ) {
                    Text("ターン終了")
                }
            }
        }
    }





    @Composable
    fun OpponentGraveyardArea(modifier: Modifier = Modifier, hasCard: Boolean) {
        val graveyardBitmap = loadBackgroundImage("images/battle/Graveyard.png")
        Box(
            modifier = modifier.graphicsLayer(rotationZ = 180f)
        ) {
            Image(
                bitmap = graveyardBitmap,
                contentDescription = "Opponent Graveyard Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            if (hasCard) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // カード表示処理
                }
            }
        }
    }
    @Composable
    fun OpponentBanishArea(modifier: Modifier = Modifier, hasCard: Boolean) {
        val banishBitmap = loadBackgroundImage("images/battle/Banished.png")
        Box(
            modifier = modifier.graphicsLayer(rotationZ = 180f)
        ) {
            Image(
                bitmap = banishBitmap,
                contentDescription = "Opponent Banish Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            if (hasCard) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                }
            }
        }
    }
    @Composable
    fun OpponentDeckArea(modifier: Modifier = Modifier, count: Int) {
        val deckBackBitmap = loadBackgroundImage("images/battle/Back.jpg")
        val deckBitmap = loadBackgroundImage("images/battle/Deck.png")

        Box(modifier = modifier.graphicsLayer(rotationZ = 180f)) {
            Image(
                bitmap = if (count > 0) deckBackBitmap else deckBitmap,
                contentDescription = "Opponent Deck Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
    }
    @Composable
    fun OpponentEvolveDeckArea(modifier: Modifier = Modifier, hasCard: Boolean) {
        val evolveBitmap = loadBackgroundImage("images/battle/Evolve.png")
        Box(
            modifier = modifier.graphicsLayer(rotationZ = 180f)
        ) {
            Image(
                bitmap = evolveBitmap,
                contentDescription = "Opponent Evolve Deck Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            if (hasCard) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // カード表示処理
                }
            }
        }
    }
    @Composable
    fun OpponentLeaderArea(modifier: Modifier = Modifier, hasCard: Boolean) {
        val leaderBitmap = loadBackgroundImage("images/battle/Leader.png")
        Box(
            modifier = modifier.graphicsLayer(rotationZ = 180f)
        ) {
            Image(
                bitmap = leaderBitmap,
                contentDescription = "Opponent Leader Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            if (hasCard) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // カード表示処理
                }
            }
        }
    }
    @Composable
    fun PlayerDeckArea(modifier: Modifier = Modifier, count: Int) {
        val deckBackBitmap = loadBackgroundImage("images/battle/Back.jpg")
        val deckBitmap = loadBackgroundImage("images/battle/Deck.png")
        Box(modifier = modifier) {
            Image(
                bitmap = if (count > 0) deckBackBitmap else deckBitmap,
                contentDescription = "Player Deck Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
    }


    @Composable
    fun PlayerLeaderArea(modifier: Modifier = Modifier, hasCard: Boolean) {
        val leaderBitmap = loadBackgroundImage("images/battle/Leader.png")
        Box(modifier = modifier) {
            Image(
                bitmap = leaderBitmap,
                contentDescription = "Leader Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            if (hasCard) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // カード表示処理
                }
            }
        }
    }
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
    @Composable
    fun OpponentFieldArea(
        modifier: Modifier = Modifier,
        screenWidth: Float,
        screenHeight: Float
    ) {
        val density = LocalDensity.current
        val cardWidth = with(density) { (screenWidth * cardWidthRatio).toDp() }
        val cardHeight = with(density) { (screenHeight * cardHeightRatio).toDp() }
        val horizontalSpacing = with(density) { (screenWidth * horizontalSpacingRatio).toDp() }
        val verticalSpacing = with(density) { (screenHeight * verticalSpacingRatio).toDp() }
        Column(
            modifier = modifier
                .graphicsLayer(rotationZ = 180f),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { // 上段 1,2,3
            Row(
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(cardWidth, cardHeight)
                            .background(Color.White.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) { //text
                    }
                }
            } // 下段 4,5
            Row(
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                repeat(2) { index ->
                    Box(
                        modifier = Modifier
                            .size(cardWidth, cardHeight)
                            .background(Color.LightGray.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        //text
                    }
                }
            }
        }
    }
    @Composable
    fun OpponentHealthArea(modifier: Modifier = Modifier, health: Int = 20) {
        Box(
            modifier = modifier.graphicsLayer(rotationZ = 180f), // 追加
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$health",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
    @Composable
    fun PlayerHealthArea(modifier: Modifier = Modifier, health: Int = 20) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$health",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
    @Composable
    fun OpponentPPStatusArea(
        modifier: Modifier = Modifier,
        currentPP: Int,
        maxPP: Int
    ) {
        Box(
            modifier = modifier.graphicsLayer(rotationZ = 180f), // 追加
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$currentPP / $maxPP",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
    @Composable
    fun PlayerPPStatusArea(
        modifier: Modifier = Modifier,
        currentPP: Int,
        maxPP: Int
    ) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$currentPP / $maxPP",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
    @Composable
    fun OpponentGraveyardCountArea(modifier: Modifier = Modifier, count: Int) {
        Box(
            modifier = modifier.graphicsLayer(rotationZ = 180f), // 追加
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$count",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
    @Composable
    fun OpponentBanishCountArea(modifier: Modifier = Modifier, count: Int) {
        Box(
            modifier = modifier.graphicsLayer(rotationZ = 180f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$count",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }

    // EP表示共通Composable
    @Composable
    fun EPStatusArea(
        modifier: Modifier = Modifier,
        epCount: Int,
        isVisibleList: List<Boolean>,
        rotation: Float = 0f
    ) {
        val context = LocalContext.current
        val epBitmap = remember {
            val inputStream = context.assets.open("images/battle/ep.png")
            BitmapFactory.decodeStream(inputStream).asImageBitmap()
        }
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            isVisibleList.forEach { isVisible ->
                AnimatedVisibility(visible = isVisible) {
                    Image(
                        bitmap = epBitmap,
                        contentDescription = "EP",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(rotationZ = rotation)
                    )
                }
            }
        }
    }
    @Composable
    fun loadBackgroundImage(assetPath: String): ImageBitmap {
        val context = LocalContext.current
        return remember(assetPath) {
            context.assets.open(assetPath).use { inputStream ->
                BitmapFactory.decodeStream(inputStream).asImageBitmap()
            }
        }
    }
    @Composable
    fun BattleScreenWithDispatcher(
        navController: NavController,
        deck1Name: String,
        deck2Name: String,
        isFirstPlayer: Boolean
    ) {
        val dispatcher = remember { com.example.evolve.ui.screens.input.TouchEventDispatcher() }

        CompositionLocalProvider(
            com.example.evolve.ui.screens.input.LocalTouchEventDispatcher provides dispatcher
        ) {
            com.example.evolve.ui.screens.BattleScreen(
                navController = navController,
                deck1Name = deck1Name,
                deck2Name = deck2Name,
                isFirstPlayer = isFirstPlayer
            )
        }
    }
    