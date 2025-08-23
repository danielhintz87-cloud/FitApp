package com.example.fitapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

private enum class NavItem(val label: String) {
    Today("Heute"),
    Training("Training"),
    Nutrition("Ernährung"),
    Shopping("Einkauf"),
    Progress("Fortschritt")
}

@Composable
fun FitApp() {
    val items = NavItem.values().toList()
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = selectedIndex, pageCount = { items.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedIndex) { pagerState.animateScrollToPage(selectedIndex) }
    LaunchedEffect(pagerState.currentPage) { selectedIndex = pagerState.currentPage }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { idx, item ->
                    NavigationBarItem(
                        selected = selectedIndex == idx,
                        onClick = { scope.launch { pagerState.animateScrollToPage(idx) } },
                        icon = {},
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 1,
            modifier = Modifier.padding(innerPadding)
        ) { page ->
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
