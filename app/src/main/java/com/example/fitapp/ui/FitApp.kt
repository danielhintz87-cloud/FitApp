package com.example.fitapp.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FitApp() {
    val tabs = FitAppTab.values()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Column {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(tab.title) }
                )
            }
        }
        HorizontalPager(state = pagerState) { page ->
            when (tabs[page]) {
                FitAppTab.Setup -> TrainingSetupScreen()
                FitAppTab.Workout -> DailyWorkoutScreen()
                FitAppTab.Calories -> CalorieScreen()
                FitAppTab.Nutrition -> NutritionScreen()
                FitAppTab.Shopping -> ShoppingListScreen()
                FitAppTab.Progress -> ProgressScreen()
            }
        }
    }
}

enum class FitAppTab(val title: String) {
    Setup("Setup"),
    Workout("Workout"),
    Calories("Kalorien"),
    Nutrition("Ernährung"),
    Shopping("Einkauf"),
    Progress("Fortschritt")
}
