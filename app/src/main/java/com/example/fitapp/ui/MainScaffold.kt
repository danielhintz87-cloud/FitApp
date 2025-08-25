package com.example.fitapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fitapp.R
import com.example.fitapp.ui.nav.AppNavHost
import com.example.fitapp.ui.nav.Dest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(nav: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FitApp",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                        label = { Text("AI Logs") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            nav.navigate("ai_logs")
                        }
                    )
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Einstellungen") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            // TODO: Navigate to settings
                        }
                    )
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text("Über die App") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            // TODO: Navigate to about
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = { Text("FitApp") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        var showMenu by remember { mutableStateOf(false) }
                        
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("AI Logs") },
                                onClick = {
                                    showMenu = false
                                    nav.navigate("ai_logs")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Analytics, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Einstellungen") },
                                onClick = {
                                    showMenu = false
                                    // TODO: Navigate to settings
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                }
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(Modifier.navigationBarsPadding()) {
                    val items = listOf(Dest.Today, Dest.Plan, Dest.Recipes, Dest.FoodScan)
                    val backStack by nav.currentBackStackEntryAsState()
                    val current = backStack?.destination?.route
                    items.forEach { dest ->
                        NavigationBarItem(
                            selected = current == dest.route,
                            onClick = { if (current != dest.route) nav.navigate(dest.route) },
                            icon = {
                                Icon(
                                    painterResource(id = when(dest) {
                                        Dest.Plan -> R.drawable.ic_plan
                                        Dest.Today -> R.drawable.ic_today
                                        Dest.Recipes -> R.drawable.ic_recipes
                                        Dest.FoodScan -> R.drawable.ic_camera
                                        else -> R.drawable.ic_today
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
}
