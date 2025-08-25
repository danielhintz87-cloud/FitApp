package com.example.fitapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
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
    var showDropdownMenu by remember { mutableStateOf(false) }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    onNavigate = { destination ->
                        scope.launch {
                            drawerState.close()
                            nav.navigate(destination)
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0,0,0,0),
            topBar = {
                TopAppBar(
                    title = { Text("FitApp") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menü")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDropdownMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Mehr")
                        }
                        DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("AI Logs") },
                                onClick = {
                                    showDropdownMenu = false
                                    nav.navigate("ai_logs")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Einstellungen") },
                                onClick = {
                                    showDropdownMenu = false
                                    nav.navigate("settings")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Über") },
                                onClick = {
                                    showDropdownMenu = false
                                    nav.navigate("about")
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
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
}

@Composable
private fun DrawerContent(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "FitApp",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Divider()
        
        NavigationDrawerItem(
            label = { Text("Trainingsplan") },
            selected = false,
            onClick = { onNavigate(Dest.Plan.route) },
            icon = { Icon(painterResource(R.drawable.ic_plan), null) }
        )
        
        NavigationDrawerItem(
            label = { Text("Heute") },
            selected = false,
            onClick = { onNavigate(Dest.Today.route) },
            icon = { Icon(painterResource(R.drawable.ic_today), null) }
        )
        
        NavigationDrawerItem(
            label = { Text("Rezepte") },
            selected = false,
            onClick = { onNavigate(Dest.Recipes.route) },
            icon = { Icon(painterResource(R.drawable.ic_recipes), null) }
        )
        
        NavigationDrawerItem(
            label = { Text("Food Scan") },
            selected = false,
            onClick = { onNavigate(Dest.FoodScan.route) },
            icon = { Icon(painterResource(R.drawable.ic_camera), null) }
        )
        
        NavigationDrawerItem(
            label = { Text("Coach") },
            selected = false,
            onClick = { onNavigate(Dest.Coach.route) },
            icon = { Icon(painterResource(R.drawable.ic_coach), null) }
        )
        
        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(16.dp))
        
        NavigationDrawerItem(
            label = { Text("AI Logs") },
            selected = false,
            onClick = { onNavigate("ai_logs") },
            icon = { Icon(painterResource(R.drawable.ic_logs), null) }
        )
        
        NavigationDrawerItem(
            label = { Text("Einstellungen") },
            selected = false,
            onClick = { onNavigate("settings") },
            icon = { Icon(painterResource(R.drawable.ic_settings), null) }
        )
    }
}
