package com.example.fitapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.fitapp.ui.FitApp
import com.example.fitapp.ui.theme.FitAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitAppTheme {
                FitApp()
            }
        }
    }
}
