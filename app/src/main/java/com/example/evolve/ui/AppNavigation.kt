package com.example.evolve.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.evolve.ui.screens.*
import com.example.evolve.ui.screens.input.*
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                onNavigateToDecks = { navController.navigate("deck_select") },
                onNavigateToDeckSelect = { navController.navigate("deck_select") },
                onNavigateToCards = { navController.navigate("test_effect") },
                onNavigateToDeckEdit = { navController.navigate("new_deck_edit/BP01/home") }
            )
        }

        composable("deck_select") {
            DeckSelectScreen(navController = navController)
        }

        composable("deck_detail/{deckName}") { backStackEntry ->
            val deckName = backStackEntry.arguments?.getString("deckName") ?: return@composable
            DeckDetailScreen(navController = navController, deckName = deckName)
        }

// 新規作成用
        composable("new_deck_edit/{expansion}/{from}") { backStackEntry ->
            val expansion = backStackEntry.arguments?.getString("expansion") ?: "BP01"
            val from = backStackEntry.arguments?.getString("from") ?: "home"
            DeckEditScreen(
                navController = navController,
                selectedSet = expansion,
                initialDeckName = "",
                from = from
            )
        }
// 編集用
        composable("deck_edit/{expansion}/{deckName}/{from}") { backStackEntry ->
            val expansion = backStackEntry.arguments?.getString("expansion") ?: "BP01"
            val deckName = backStackEntry.arguments?.getString("deckName") ?: ""
            val from = backStackEntry.arguments?.getString("from") ?: "home"
            DeckEditScreen(
                navController = navController,
                selectedSet = expansion,
                initialDeckName = deckName,
                from = from
            )
        }
        composable("cards") {
            // カード一覧画面
        }
        composable("card_detail/{expansion}/{cardId}") { backStackEntry ->
            val expansion = backStackEntry.arguments?.getString("expansion") ?: return@composable
            val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
            CardDetailScreen(expansion = expansion, cardId = cardId, onDismiss = { navController.popBackStack() })
        }

        composable(
            route = "BattleScreen/{deck1Name}/{deck2Name}/{isFirstPlayer}",
            arguments = listOf(
                navArgument("deck1Name") { type = NavType.StringType },
                navArgument("deck2Name") { type = NavType.StringType },
                navArgument("isFirstPlayer") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val deck1Name = backStackEntry.arguments?.getString("deck1Name") ?: ""
            val deck2Name = backStackEntry.arguments?.getString("deck2Name") ?: ""
            val isFirstPlayer = backStackEntry.arguments?.getBoolean("isFirstPlayer") ?: false

            BattleScreenWithDispatcher(
                navController = navController,
                deck1Name = deck1Name,
                deck2Name = deck2Name,
                isFirstPlayer = isFirstPlayer
            )

        }
        composable("test_effect") {
            TestEffectScreen()
        }
    }

}
