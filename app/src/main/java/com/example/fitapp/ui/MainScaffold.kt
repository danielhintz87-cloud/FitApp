package com.example.fitapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(navController: NavHostController? = null) {
    val nav = navController ?: rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var showOverflowMenu by remember { mutableStateOf(false) }
    
    // 🚀 User Experience Management
    val userExperienceManager = remember { com.example.fitapp.services.UserExperienceManager.getInstance(ctx) }
    val userExperienceState by userExperienceManager.userExperienceState.collectAsState()
    
    // Determine start destination based on user experience
    val startDestination: String = remember(userExperienceState) {
        when {
            !userExperienceState.hasCompletedOnboarding -> "onboarding"
            else -> "unified_dashboard"
        }
    } { innerPadding ->
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
