// app/src/main/java/com/example/fitapp/ui/FitApp.kt
package com.example.fitapp.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

private enum class NavItem(val label: String) {
    Today("Heute"),
    Training("Training"),
    Nutrition("Ernährung"),
    Shopping("Einkauf"),
    Progress("Fortschritt")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FitApp() {
    val items = remember { NavItem.values().toList() }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { items.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        icon = {},
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            HorizontalPager(state = pagerState) { page ->
                when (items[page]) {
                    NavItem.Today -> TodayScreen()
                    NavItem.Training -> TrainingSetupScreen()
                    NavItem.Nutrition -> NutritionScreen()
                    NavItem.Shopping -> ShoppingListScreen()
                    NavItem.Progress -> ProgressScreen()
                }
            }
        }
    }
}
