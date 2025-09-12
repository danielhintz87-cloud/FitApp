package com.example.fitapp.ui.preview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fitapp.ui.screens.EnhancedAnalyticsScreen
import com.example.fitapp.ui.theme.FitAppTheme

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EnhancedAnalyticsScreenPreview() {
    FitAppTheme {
        EnhancedAnalyticsScreen(
            contentPadding = PaddingValues(0.dp),
            navController = null,
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun EnhancedAnalyticsScreenCompactPreview() {
    FitAppTheme {
        EnhancedAnalyticsScreen(
            contentPadding = PaddingValues(16.dp),
            navController = null,
        )
    }
}
