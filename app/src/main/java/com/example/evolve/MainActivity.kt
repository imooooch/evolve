package com.example.evolve

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.evolve.ui.AppNavigation
import com.example.evolve.ui.theme.EvolveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EvolveTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}
