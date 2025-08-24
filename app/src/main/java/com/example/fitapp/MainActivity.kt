package com.example.fitapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.fitapp.ui.MainScaffold
import com.example.fitapp.ui.dialogs.ModelPickerDialog
import com.example.fitapp.ui.theme.FitAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FitAppTheme {
                val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }

                var showModelPicker by remember { mutableStateOf(false) }
                MainScaffold(
                    onOpenProfile = {},
                    onOpenSettings = {},
                    onOpenShopping = {},
                    onOpenModelPicker = { showModelPicker = true }
                )
                if (showModelPicker) {
                    ModelPickerDialog(
                        currentChat = "openai",
                        onPick = { },
                        onDismiss = { showModelPicker = false }
                    )
                }
            }
        }
    }
}
