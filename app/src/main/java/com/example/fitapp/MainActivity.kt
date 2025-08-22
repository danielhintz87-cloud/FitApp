package com.example.fitapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.fitapp.ui.FitApp
import com.example.fitapp.ui.theme.FitAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-Edge aktivieren
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FitAppTheme { // steuert Dark/Light & Dynamic Color
                // Systembar-Icons passend zum Theme einfärben
                val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !isDark   // true => dunkle Icons aus, helle an
                    isAppearanceLightNavigationBars = !isDark
                }

                FitApp()
            }
        }
    }
}
