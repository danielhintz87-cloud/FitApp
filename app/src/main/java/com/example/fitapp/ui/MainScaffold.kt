package com.example.fitapp.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fitapp.R
import com.example.fitapp.ui.nav.AppNavHost
import com.example.fitapp.ui.nav.Dest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(nav: NavHostController) {
    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        bottomBar = {
            NavigationBar(Modifier.navigationBarsPadding()) {
                val items = listOf(Dest.Plan, Dest.Today, Dest.Recipes, Dest.FoodScan, Dest.Coach)
                val backStack by nav.currentBackStackEntryAsState()
                val current = backStack?.destination?.route
                items.forEach { dest ->
                    NavigationBarItem(
                        selected = current == dest.route,
                        onClick = { if (current != dest.route) nav.navigate(dest.route) },
                        icon = {
                            Icon(
                                painterResource(id = when(dest){
                                    Dest.Plan -> R.drawable.ic_plan
                                    Dest.Today -> R.drawable.ic_today
                                    Dest.Recipes -> R.drawable.ic_recipes
                                    Dest.FoodScan -> R.drawable.ic_camera
                                    Dest.Coach -> R.drawable.ic_coach
                                }),
                                contentDescription = dest.label
                            )
                        },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { pad ->
        AppNavHost(
            nav = nav,
            modifier = Modifier
                .statusBarsPadding()
                .padding(pad)
        )
    }
}
