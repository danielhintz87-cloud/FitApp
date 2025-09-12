package com.example.fitapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FitApp") }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "unified_dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("unified_dashboard") { 
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("Unified Dashboard")
                }
            }
            composable("nutrition") { 
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("Nutrition")
                }
            }
            composable("plan") { 
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("Plan")
                }
            }
            composable("progress") { 
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("Progress")
                }
            }
        }
    }
}
