package com.example.evolve.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.evolve.ui.components.SingleClickButton
import java.io.File

@Composable
fun HomeScreen(
    onNavigateToDecks: () -> Unit,
    onNavigateToDeckSelect: () -> Unit,
    onNavigateToCards: () -> Unit,
    onNavigateToDeckEdit: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val tempFile = File(context.cacheDir, "temp_deck.json")
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App Title
            Text(
                text = "Shadowverse EVOLVE",
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            SingleClickButton(
                onClick = onNavigateToDecks,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA)),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("デッキ選択", fontSize = 24.sp, color = Color.White, textAlign = TextAlign.Center)
            }

// Deck Edit Button
            SingleClickButton(
                onClick = onNavigateToDeckEdit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5)),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("デッキ作成", fontSize = 24.sp, color = Color.White, textAlign = TextAlign.Center)
            }

// Card List Button
            SingleClickButton(
                onClick = onNavigateToCards,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF018786)),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("動作テスト用", fontSize = 24.sp, color = Color.White, textAlign = TextAlign.Center)
            }
        }
    }
}
