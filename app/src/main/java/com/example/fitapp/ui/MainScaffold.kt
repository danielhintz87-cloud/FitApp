package com.example.fitapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.fitapp.core.navigation.NavigationRegistry

@ExperimentalMaterial3Api
@Composable
fun MainScaffold(navController: NavHostController? = null) {
    val nav = navController ?: rememberNavController()
    val navigationRegistry = remember { NavigationRegistry.getInstance() }
    
    Scaffold { paddingValues ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                Text("FitApp Home - Navigation wird repariert")
            }
            // Registriere Navigation Routes  
            navigationRegistry.registerAllNavigations(this, nav)
        }
    }
}
