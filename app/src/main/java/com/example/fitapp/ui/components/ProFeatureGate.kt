package com.example.fitapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Component that gates PRO features with upgrade prompts
 */
@Composable
fun ProFeatureGate(
    isPro: Boolean,
    featureName: String,
    description: String,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (isPro) {
        content()
    } else {
        ProUpgradePrompt(
            featureName = featureName,
            description = description,
            onUpgradeClick = onUpgradeClick,
            modifier = modifier
        )
    }
}

@Composable
fun ProUpgradePrompt(
    featureName: String,
    description: String,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "$featureName - PRO Feature",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(20.dp))
            
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Upgrade,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Upgrade to PRO")
            }
        }
    }
}

/**
 * Simplified check for PRO features
 * In a real app, this would check user subscription status
 */
object ProFeatureManager {
    // For demo purposes, some features are free, others require PRO
    fun isFeatureAvailable(feature: ProFeature): Boolean {
        return when (feature) {
            ProFeature.RECIPE_CREATION -> true // Free
            ProFeature.BASIC_COOKING_MODE -> true // Free
            ProFeature.OFFICIAL_RECIPE_DATABASE -> false // PRO
            ProFeature.ADVANCED_FILTERS -> false // PRO
            ProFeature.GROCERY_LIST_CREATION -> false // PRO
            ProFeature.RECIPE_SHARING -> false // PRO
            ProFeature.DETAILED_NUTRITION_ANALYSIS -> false // PRO
            ProFeature.STEP_BY_STEP_COOKING_MODE -> true // Free (enhanced version)
            ProFeature.WEEKLY_RECIPE_UPDATES -> false // PRO
            ProFeature.RECIPE_COLLECTIONS -> false // PRO
        }
    }
    
    fun getFeatureDescription(feature: ProFeature): String {
        return when (feature) {
            ProFeature.OFFICIAL_RECIPE_DATABASE -> "Access to 2,900+ expert-curated recipes with weekly updates"
            ProFeature.ADVANCED_FILTERS -> "Filter recipes by calories, prep time, difficulty, dietary preferences and more"
            ProFeature.GROCERY_LIST_CREATION -> "Smart grocery lists with store layout categorization and ingredient merging"
            ProFeature.RECIPE_SHARING -> "Share your favorite recipes with friends and export as PDF"
            ProFeature.DETAILED_NUTRITION_ANALYSIS -> "Detailed nutrition breakdown including micronutrients, fatty acids, and vitamins"
            ProFeature.WEEKLY_RECIPE_UPDATES -> "Get new recipe recommendations every week based on your preferences"
            ProFeature.RECIPE_COLLECTIONS -> "Organize recipes into custom collections and meal plans"
            else -> "This premium feature enhances your cooking and nutrition experience"
        }
    }
}

enum class ProFeature {
    RECIPE_CREATION,
    BASIC_COOKING_MODE,
    OFFICIAL_RECIPE_DATABASE,
    ADVANCED_FILTERS,
    GROCERY_LIST_CREATION,
    RECIPE_SHARING,
    DETAILED_NUTRITION_ANALYSIS,
    STEP_BY_STEP_COOKING_MODE,
    WEEKLY_RECIPE_UPDATES,
    RECIPE_COLLECTIONS
}