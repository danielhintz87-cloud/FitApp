package com.example.fitapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.fitapp.ai.AppAi
import com.example.fitapp.ui.MainScaffold
import com.example.fitapp.ui.theme.FitAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize AI system
        AppAi.initialize(this)
        
        // Edge-to-edge Darstellung aktivieren
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FitAppTheme {
                // StatusBar/NavigationBar-Icons an Hell/Dunkel-Modus anpassen
                val isDark = isSystemInDarkTheme()
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
                // Haupt-Gerüst der App (Navigation + Screens)
                val nav = rememberNavController()
                MainScaffold(nav)
            }
        }
    }
}
