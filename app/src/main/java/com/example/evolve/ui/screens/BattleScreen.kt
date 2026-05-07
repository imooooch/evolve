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
    import androidx.compose.foundation.interaction.MutableInteractionSource
    import androidx.compose.material3.Button
    import androidx.compose.material3.ButtonDefaults
    import androidx.compose.runtime.*
    import androidx.compose.ui.graphics.ImageBitmap
    import androidx.compose.ui.graphics.RectangleShape
    import androidx.compose.ui.graphics.graphicsLayer
    import androidx.compose.ui.input.pointer.pointerInput
    import androidx.compose.ui.platform.LocalConfiguration
    import androidx.compose.ui.zIndex
    import com.example.evolve.ui.viewmodel.BattleViewModel
    import androidx.lifecycle.viewmodel.compose.viewModel
    import com.example.evolve.battle.ImageDisplaySide
    import com.example.evolve.battle.ViewSide
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
            val viewSide by viewModel.viewSide.collectAsState()
            val viewMode by viewModel.viewMode.collectAsState()
            val state = battleState ?: return@BoxWithConstraints

            val bottomPlayer = when (viewSide) {
                ViewSide.Player1 -> state.player1
                ViewSide.Player2 -> state.player2
            }

            val topPlayer = when (viewSide) {
                ViewSide.Player1 -> state.player2
                ViewSide.Player2 -> state.player1
            }
            val playerEpCount = bottomPlayer.ep
            val opponentEpCount = topPlayer.ep

            val playerHand = bottomPlayer.hand
            val opponentHand = topPlayer.hand

            val playerLeaderHp = bottomPlayer.leaderHp
            val opponentLeaderHp = topPlayer.leaderHp

            val playerCurrentPP = bottomPlayer.currentPP
            val playerMaxPP = bottomPlayer.maxPP
            val opponentCurrentPP = topPlayer.currentPP
            val opponentMaxPP = topPlayer.maxPP

            val playerDeckCount = bottomPlayer.deck.size
            val opponentDeckCount = topPlayer.deck.size

            val playerBanishCount = bottomPlayer.banished.size
            val opponentBanishCount = topPlayer.banished.size

            val playerGraveyardCount = bottomPlayer.graveyard.size
            val opponentGraveyardCount = topPlayer.graveyard.size

            val playerFaceDownEvolveCount =
                bottomPlayer.evolveDeck.count { !it.isFaceUp }
            val opponentFaceDownEvolveCount =
                topPlayer.evolveDeck.count { !it.isFaceUp }

            val isEvolveDeckOccupied = bottomPlayer.evolveDeck.isNotEmpty()
            val isOpponentEvolveDeckOccupied = topPlayer.evolveDeck.isNotEmpty()

            val isGraveyardOccupied = bottomPlayer.graveyard.isNotEmpty()
            val isOpponentGraveyardOccupied = topPlayer.graveyard.isNotEmpty()

            val isBanishOccupied = bottomPlayer.banished.isNotEmpty()
            val isOpponentBanishOccupied = topPlayer.banished.isNotEmpty()

            val graveyardCards = bottomPlayer.graveyard
            val evolveCards = bottomPlayer.evolveDeck
            val banishCards = bottomPlayer.banish

            val isLeaderOccupied = true // 表示用trueで可
            val isOpponentLeaderOccupied = true

            val playerHandCards = remember { mutableStateListOf<CardData>() }
            val deckBackBitmap = remember {
                val inputStream = context.assets.open("images/battle/Back.jpg")
                BitmapFactory.decodeStream(inputStream).asImageBitmap()
            }
            val selectedImagePath by viewModel.selectedImagePath.collectAsState()
            val selectedHandIndex by viewModel.selectedHandIndex.collectAsState()
            val lastCard = graveyardCards.lastOrNull()
            val lastBanishCard = banishCards.lastOrNull()

            val imageDisplaySide by viewModel.imageDisplaySide.collectAsState()
            val interactionSource = remember { MutableInteractionSource() }
            val topSide = when (viewSide) {
                ViewSide.Player1 -> ViewSide.Player2
                ViewSide.Player2 -> ViewSide.Player1
            }
            // ✅ 共有画像表示エリア（他機能でも利用可能）
// 背景
            Image(
                bitmap = remember {
                    val inputStream = context.assets.open("images/battle/table.png")
                    BitmapFactory.decodeStream(inputStream)
                }.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedImagePath) {
                        detectTapGestures {
                            if (selectedImagePath.isNotEmpty()) {
                                viewModel.clearImageAndMenu()
                            }
                        }
                    }
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
                cards = opponentHand,
                onCardPlayed = { index ->
                    viewModel.playCardFromHand(index, topSide)
                    viewModel.clearImageAndMenu()
                }
            )
            OpponentEXArea(
                modifier = Modifier
                    .offset(y = with(density) { (screenHeight * 0.196f).toDp() })
                    .align(Alignment.TopCenter),
                exCards = topPlayer.exArea,
                viewModel = viewModel
            )
            OpponentFieldArea(
                modifier = Modifier
                    .offset(y = with(density) { (screenHeight * 0.298f).toDp() })
                    .align(Alignment.TopCenter),
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                fieldCards = topPlayer.field,
                viewModel = viewModel,
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
                hasCard = isOpponentEvolveDeckOccupied,
                evolveCards = topPlayer.evolveDeck
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
                    .offset(x = -5.dp, y = with(LocalDensity.current) { (screenHeight * 0.215f).toDp() })
                    .align(Alignment.TopEnd),
                count = opponentFaceDownEvolveCount
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
                    .offset(y = with(density) { (screenHeight * 0.505f).toDp() })
                    .align(Alignment.TopCenter),
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                fieldCards = bottomPlayer.field,
                viewModel = viewModel
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
                facedownCount = playerFaceDownEvolveCount
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
                exCards = bottomPlayer.exArea,
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
                    viewModel.playCardFromHand(index, viewSide)
                    viewModel.clearImageAndMenu()
                }
            )

            if (selectedImagePath.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(90f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.45f)
                            .align(
                                if (imageDisplaySide == ImageDisplaySide.Top) {
                                    Alignment.TopCenter
                                } else {
                                    Alignment.BottomCenter
                                }
                            )
                    ) {
                        Button(
                            onClick = {
                                viewModel.clearImageAndMenu()
                            },
                            shape = RectangleShape,
                            interactionSource = interactionSource, // ←追加
                            modifier = Modifier
                                .size(400.dp)
                                .align(
                                    if (imageDisplaySide == ImageDisplaySide.Top) {
                                        Alignment.TopCenter
                                    } else {
                                        Alignment.BottomCenter
                                    }
                                )
                                .offset(
                                    y = if (imageDisplaySide == ImageDisplaySide.Top) {
                                        16.dp
                                    } else {
                                        (-55).dp
                                    }
                                ),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            Image(
                                bitmap = loadCardImage(selectedImagePath),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
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
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SingleClickButton(
                        onClick = {
                            viewModel.toggleViewSide()
                            viewModel.clearImageAndMenu()
                        }
                    ) {
                        Text("視点切替")
                    }

                    SingleClickButton(
                        onClick = {
                            viewModel.endTurn()
                        }
                    ) {
                        Text("ターン終了")
                    }
                }
            }
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
    