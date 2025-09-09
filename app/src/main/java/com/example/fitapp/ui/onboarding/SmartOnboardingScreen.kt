package com.example.fitapp.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 🚀 Smart Onboarding Experience
 * 
 * Revolutionary introduction to FitApp's comprehensive features:
 * - Shows users the full power of the app
 * - Highlights unique features that set us apart from YAZIO
 * - Creates excitement about the AI-powered journey
 * - Guides users through their first setup
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartOnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    var currentPage by remember { mutableIntStateOf(0) }
    
    // Auto-advance pages (optional)
    LaunchedEffect(currentPage) {
        delay(5000) // 5 seconds per page
        if (currentPage < onboardingPages.size - 1) {
            scope.launch {
                pagerState.animateScrollToPage(currentPage + 1)
            }
        }
    }
    
    // Update current page when pager changes
    LaunchedEffect(pagerState.currentPage) {
        currentPage = pagerState.currentPage
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPage(
                page = onboardingPages[page],
                isActive = page == currentPage
            )
        }
        
        // Page Indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(onboardingPages.size) { index ->
                val isActive = index == currentPage
                Box(
                    modifier = Modifier
                        .size(if (isActive) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        .animateContentSize()
                )
            }
        }
        
        // Navigation Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skip Button
            TextButton(
                onClick = onOnboardingComplete
            ) {
                Text("Überspringen")
            }
            
            // Navigation Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(currentPage - 1)
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Zurück")
                    }
                }
                
                if (currentPage < onboardingPages.size - 1) {
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(currentPage + 1)
                            }
                        }
                    ) {
                        Text("Weiter")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Button(
                        onClick = onOnboardingComplete
                    ) {
                        Text("Los geht's!")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    page: OnboardingPageData,
    isActive: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with Animation
        AnimatedVisibility(
            visible = isActive,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Card(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = page.iconColor.copy(alpha = 0.1f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        page.icon,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = page.iconColor
                    )
                }
            }
        }
        
        Spacer(Modifier.height(40.dp))
        
        // Title with Animation
        AnimatedVisibility(
            visible = isActive,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { -it / 2 } + fadeOut()
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Description with Animation
        AnimatedVisibility(
            visible = isActive,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
        
        Spacer(Modifier.height(32.dp))
        
        // Feature Highlights
        AnimatedVisibility(
            visible = isActive,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = page.iconColor.copy(alpha = 0.05f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    page.iconColor.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    page.features.forEach { feature ->
                        FeatureHighlight(
                            feature = feature,
                            color = page.iconColor
                        )
                        if (feature != page.features.last()) {
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureHighlight(
    feature: FeatureHighlight,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            feature.icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = color
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                feature.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Data Classes
private data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val features: List<FeatureHighlight>
)

private data class FeatureHighlight(
    val title: String,
    val description: String,
    val icon: ImageVector
)

// Onboarding Pages Content
private val onboardingPages = listOf(
    OnboardingPageData(
        title = "Willkommen bei FitApp",
        description = "Deine revolutionäre Fitness-Journey beginnt hier! Entdecke eine App, die weit über herkömmliche Fitness-Tracker hinausgeht.",
        icon = Icons.Default.RocketLaunch,
        iconColor = Color(0xFF6C5CE7),
        features = listOf(
            FeatureHighlight(
                title = "KI-powered Coaching",
                description = "Personalisierte Trainingspläne und Ernährungsberatung",
                icon = Icons.Default.Psychology
            ),
            FeatureHighlight(
                title = "Comprehensive Tracking",
                description = "BMI, Intervallfasten, Makros und mehr in einer App",
                icon = Icons.Default.Analytics
            ),
            FeatureHighlight(
                title = "Smart Integration",
                description = "Health Connect, Barcode Scanner, Rezept-Generation",
                icon = Icons.Default.IntegrationInstructions
            )
        )
    ),
    
    OnboardingPageData(
        title = "KI Personal Trainer",
        description = "Unser AI Coach analysiert deine Fortschritte und erstellt maßgeschneiderte Trainingspläne, die sich an deine Ziele und Fähigkeiten anpassen.",
        icon = Icons.Default.Psychology,
        iconColor = Color(0xFF00B894),
        features = listOf(
            FeatureHighlight(
                title = "Adaptive Workouts",
                description = "Training passt sich automatisch an deine Performance an",
                icon = Icons.AutoMirrored.Filled.TrendingUp
            ),
            FeatureHighlight(
                title = "Smart Nutrition",
                description = "Personalisierte Mahlzeitenpläne basierend auf deinen Zielen",
                icon = Icons.Default.Restaurant
            ),
            FeatureHighlight(
                title = "Progress Analysis",
                description = "Tiefe Einblicke in deine Fitness-Journey mit AI",
                icon = Icons.Default.Insights
            )
        )
    ),
    
    OnboardingPageData(
        title = "Smarte Ernährung",
        description = "Scanne Barcodes, analysiere Mahlzeiten mit der Kamera und entdecke gesunde Rezepte - alles mit KI-Unterstützung.",
        icon = Icons.Default.QrCodeScanner,
        iconColor = Color(0xFFE17055),
        features = listOf(
            FeatureHighlight(
                title = "Barcode Scanner",
                description = "Instant Produktinformationen und Nährwerte",
                icon = Icons.Default.CameraAlt
            ),
            FeatureHighlight(
                title = "Food Photo AI",
                description = "Fotografiere Mahlzeiten für automatische Kalorienschätzung",
                icon = Icons.Default.PhotoCamera
            ),
            FeatureHighlight(
                title = "Rezept Generator",
                description = "AI erstellt gesunde Rezepte basierend auf deinen Vorlieben",
                icon = Icons.Default.AutoAwesome
            )
        )
    ),
    
    OnboardingPageData(
        title = "Intervallfasten Pro",
        description = "Professionelles Intervallfasten mit 6 bewährten Protokollen, intelligentem Timer und Fortschrittstracking.",
        icon = Icons.Default.Schedule,
        iconColor = Color(0xFF6C5CE7),
        features = listOf(
            FeatureHighlight(
                title = "6 Fasten-Protokolle",
                description = "16:8, 14:10, 18:6, 24:0, 5:2, 6:1 verfügbar",
                icon = Icons.Default.Timer
            ),
            FeatureHighlight(
                title = "Smart Timer",
                description = "Circular Progress mit Benachrichtigungen",
                icon = Icons.Default.NotificationsActive
            ),
            FeatureHighlight(
                title = "Streak Tracking",
                description = "Verfolge deine Konstanz und feure dich selbst an",
                icon = Icons.Default.LocalFireDepartment
            )
        )
    ),
    
    OnboardingPageData(
        title = "Health Integration",
        description = "Nahtlose Verbindung mit Health Connect, Fitness-Trackern und anderen Gesundheits-Apps für ein vollständiges Bild deiner Fitness.",
        icon = Icons.Default.HealthAndSafety,
        iconColor = Color(0xFF00B894),
        features = listOf(
            FeatureHighlight(
                title = "Health Connect",
                description = "Automatische Synchronisation mit Android Health",
                icon = Icons.Default.Sync
            ),
            FeatureHighlight(
                title = "Multi-Device",
                description = "Daten zwischen Smartphone und Wearables synchronisieren",
                icon = Icons.Default.Watch
            ),
            FeatureHighlight(
                title = "Real-time Tracking",
                description = "Live Herzfrequenz, Schritte und Aktivitäten",
                icon = Icons.Default.MonitorHeart
            )
        )
    ),
    
    OnboardingPageData(
        title = "Ready to Transform?",
        description = "Du hast jetzt alles gesehen! FitApp ist mehr als nur eine Fitness-App - es ist dein persönlicher Gesundheits-Assistent mit KI-Power.",
        icon = Icons.Default.EmojiEvents,
        iconColor = Color(0xFFFFD700),
        features = listOf(
            FeatureHighlight(
                title = "Unified Dashboard",
                description = "Alle Features an einem Ort - intelligent verknüpft",
                icon = Icons.Default.Dashboard
            ),
            FeatureHighlight(
                title = "Achievement System",
                description = "Gamification hält dich motiviert und auf Kurs",
                icon = Icons.Default.Stars
            ),
            FeatureHighlight(
                title = "Continuous Learning",
                description = "Die App wird durch dein Feedback immer besser",
                icon = Icons.AutoMirrored.Filled.TrendingUp
            )
        )
    )
)
