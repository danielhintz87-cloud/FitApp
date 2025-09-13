<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

# Vollständige GitHub Copilot Instructions für FitApp

```markdown
---
title: "FitApp: Advanced Fitness & Nutrition Platform Development Guide"
description: "Comprehensive GitHub Copilot instructions for modern Android fitness app development with AI integration"
version: "2.0"
last_updated: "2025-09-05"
---

# FitApp Android Development Guide

Always follow these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the information provided here.

## Project Overview

FitApp is a comprehensive Android fitness tracking application built with modern technologies and inspired by market leaders like Freeletics, Nike Training Club, and other premium fitness platforms. The app combines personal fitness tracking with AI-powered coaching and community features.

**Core Technologies:**
- **Language**: Kotlin 100% with Jetpack Compose UI
- **Architecture**: Clean Architecture (MVVM + Repository pattern)
- **Database**: Room with SQLite, automated schema migrations
- **Design**: Material 3 Design System with custom theming
- **AI Integration**: Gemini + Perplexity APIs for intelligent coaching
- **Target**: Android SDK 34, minimum SDK 24, Java 17
- **Build System**: Gradle 8.14.3 with Kotlin DSL and version catalogs

## Market-Inspired Feature Strategy

FitApp draws inspiration from the most successful fitness apps in the market:

### Freeletics-Inspired Features (AI & Personalization)
- **Adaptive AI Training**: Workouts adjust based on user performance and feedback
- **Machine Learning Personalization**: 57M+ user data equivalent for intelligent recommendations
- **Bodyweight Focus**: Equipment-free workouts with progression levels (Endurance → Standard → Strength)
- **Coach+ Style AI**: Conversational AI for fitness guidance and motivation

### Nike Training Club Features (Content & Wellness)
- **Holistic Wellness**: Movement, Mindfulness, Nutrition, Rest, Connection
- **Professional Content**: HD video demonstrations and trainer guidance
- **Program-Based Training**: 4-6 week structured fitness journeys
- **Device Integration**: Apple Health, Google Fit, wearable compatibility

### Community & Social Features (Strava/Social Apps)
- **Community-Driven Challenges**: Monthly themed challenges and competitions
- **Social Feed**: Achievement sharing and peer motivation
- **Group Training**: Local and virtual workout partnerships
- **Achievement System**: Gamified progress with badges and streaks

## Core Focus Areas

**Primary Features:**
- Personal achievement tracking with advanced gamification
- AI-powered adaptive training plans and nutrition guidance
- Progress visualization with interactive charts and analytics
- Smart streak tracking for habits and long-term consistency
- Intelligent notifications for motivation and behavioral triggers
- Personal records (PR) tracking with predictive analytics
- Community features for social motivation and accountability

**Secondary Features:**
- Recipe management with cooking mode and shopping integration
- BMI tracking with weight loss program recommendations
- Enhanced analytics dashboard with AI-generated insights
- Offline functionality for workouts and meal planning

## Existing Database Architecture

The app uses a comprehensive Room database with these key entities:

### Core Entities (Already Implemented)
- **AiLog** - AI interaction history and learning data
- **SavedRecipeEntity** - User recipes with nutritional information and cooking features
- **PlanEntity** - Training plans and AI-generated workout programs
- **PersonalAchievementEntity** - Achievement tracking with gamification support
- **PersonalStreakEntity** - Habit and consistency tracking with motivation triggers
- **PersonalRecordEntity** - Personal fitness records and progression tracking
- **WeightEntity** - Weight tracking with historical analysis
- **BMIHistoryEntity** - BMI calculations and health trend analysis
- **FoodItemEntity** - Comprehensive food database with barcode integration
- **IntakeEntryEntity** - Nutrition logging with meal categorization

### New Entities to Implement
```

/* When implementing new features, add these entities following existing patterns: */

@Entity(tableName = "workout_feedback")
data class WorkoutFeedbackEntity(
@PrimaryKey(autoGenerate = true) val id: Long = 0,
val workoutId: Long,
val difficultyRating: Int, // 1-5 scale
val fatigueLevel: Int, // 1-5 scale
val motivationLevel: Int, // 1-5 scale
val muscleGroups: String, // JSON array of affected muscles
val notes: String? = null,
val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_feed")
data class CommunityFeedEntity(
@PrimaryKey val id: String = UUID.randomUUID().toString(),
val userId: String,
val type: String, // "workout", "achievement", "recipe", "milestone"
val title: String,
val description: String,
val imageUrl: String? = null,
val metadata: String, // JSON for type-specific data
val likes: Int = 0,
val comments: Int = 0,
val isPublic: Boolean = true,
val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
@PrimaryKey val id: String = UUID.randomUUID().toString(),
val title: String,
val description: String,
val type: String, // "individual", "group", "community"
val category: String, // "workout", "nutrition", "streak"
val targetValue: Float,
val targetUnit: String,
val duration: Int, // days
val startDate: String, // ISO date
val endDate: String, // ISO date
val participants: Int = 0,
val isActive: Boolean = true,
val createdBy: String? = null
)

@Entity(tableName = "social_interactions")
data class SocialInteractionEntity(
@PrimaryKey(autoGenerate = true) val id: Long = 0,
val fromUserId: String,
val toUserId: String? = null, // null for community interactions
val targetType: String, // "feed_post", "challenge", "achievement"
val targetId: String,
val interactionType: String, // "like", "comment", "share", "clap"
val content: String? = null, // for comments
val timestamp: Long = System.currentTimeMillis()
)

```

## Environment Setup Requirements

### Prerequisites
- **Java 17**: Required for compilation and runtime
- **Android SDK**: Level 34 (can work without physical SDK for basic builds)
- **Gradle**: Uses wrapper (gradlew) - version 8.14.3
- **Memory**: At least 2GB RAM for Gradle daemon (configured in gradle.properties)

### Initial Setup Commands
```


# Make gradlew executable

chmod +x gradlew

# Create local.properties file (required for build)

cp local.properties.sample local.properties

# Edit local.properties to add:

# sdk.dir=/path/to/android/sdk (optional for basic builds)

# GEMINI_API_KEY=your_key_here (for AI functionality)

# PERPLEXITY_API_KEY=your_key_here (for AI functionality)

# Verify Gradle and Java setup

./gradlew --version

```

## Build Commands & Critical Timeouts

**NEVER CANCEL BUILDS** - They may take 4-7 minutes. Always set timeouts to 10+ minutes minimum.

### Clean Build Process
```


# Clean project (1-2 minutes)

./gradlew clean

# Build debug APK (4-5 minutes, NEVER CANCEL - set timeout to 10+ minutes)

./gradlew assembleDebug

# Build release APK (5-7 minutes, NEVER CANCEL - set timeout to 15+ minutes)

./gradlew assembleRelease

```

### Verification Commands
```


# Run lint analysis (2-3 minutes, NEVER CANCEL - set timeout to 5+ minutes)

./gradlew lintDebug

# Check project including lint (3-5 minutes, NEVER CANCEL - set timeout to 10+ minutes)

./gradlew check

# Build verification without tests (5-7 minutes total, NEVER CANCEL - set timeout to 15+ minutes)

./gradlew assembleDebug lintDebug

```

## Code Style & Architecture Preferences

### Modern Kotlin Patterns
Follow these established patterns when contributing:

```

/* Always use these patterns for consistency: */

// 1. Data Classes with defaults
data class WorkoutState(
val isLoading: Boolean = false,
val workouts: List<Workout> = emptyList(),
val error: String? = null,
val selectedWorkout: Workout? = null
)

// 2. Sealed Classes for UI States
sealed class UiState<out T> {
object Loading : UiState<Nothing>()
data class Success<T>(val data: T) : UiState<T>()
data class Error(val exception: Throwable) : UiState<Nothing>()
}

// 3. Extension Functions for utilities
fun Context.showToast(message: String) {
Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

// 4. Coroutines for async operations - prefer structured concurrency
class WorkoutRepository {
suspend fun getWorkouts(): Flow<List<Workout>> = flow {
// Implementation
}
}

// 5. Compose UI with Material 3 - always use proper state hoisting
@Composable
fun WorkoutScreen(
uiState: WorkoutUiState,
onWorkoutSelected: (Workout) -> Unit,
modifier: Modifier = Modifier
) {
// Implementation with Material 3 components
}

```

## New Feature Implementation Guidelines

### Adaptive AI Training System (Freeletics-Inspired)
```

/* Implementation Pattern for AI-based workout adaptation */

// 1. Feedback Collection after workouts
@Composable
fun WorkoutCompletionDialog(
workout: Workout,
onFeedbackSubmitted: (WorkoutFeedback) -> Unit
) {
var difficultyRating by remember { mutableIntStateOf(3) }
var fatigueLevel by remember { mutableIntStateOf(3) }
var motivationLevel by remember { mutableIntStateOf(3) }

    AlertDialog(
        onDismissRequest = { /* Handle */ },
        title = { Text("Wie war dein Workout?") },
        text = {
            Column {
                Text("Schwierigkeit:")
                Slider(
                    value = difficultyRating.toFloat(),
                    onValueChange = { difficultyRating = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )
                // Add more feedback controls
            }
        },
        confirmButton = {
            Button(onClick = {
                onFeedbackSubmitted(
                    WorkoutFeedback(
                        workoutId = workout.id,
                        difficultyRating = difficultyRating,
                        fatigueLevel = fatigueLevel,
                        motivationLevel = motivationLevel
                    )
                )
            }) {
                Text("Feedback senden")
            }
        }
    )
    }

// 2. AI-based plan adaptation
class AdaptiveTrainingManager(
private val aiService: AppAi,
private val repository: WorkoutRepository
) {
suspend fun adaptNextWorkout(
currentWorkout: Workout,
feedback: WorkoutFeedback,
userProfile: UserProfile
): Workout {
val adaptationPrompt = buildString {
append("Adapt next workout based on feedback:\n")
append("Difficulty rating: \${feedback.difficultyRating}/5\n")
append("Fatigue level: \${feedback.fatigueLevel}/5\n")
append("Current workout: \${currentWorkout.title}\n")
append("User fitness level: \${userProfile.fitnessLevel}\n")
append("Generate next workout with adjusted intensity.")
}

        return aiService.generateAdaptedWorkout(adaptationPrompt, userProfile)
    }
    }

```

### Community & Social Features (Strava/Social Apps Style)
```

/* Implementation Pattern for social features */

// 1. Community Feed with engagement
@Composable
fun CommunityFeedScreen(
feedPosts: List<CommunityPost>,
onLikePost: (String) -> Unit,
onCommentPost: (String, String) -> Unit,
onSharePost: (CommunityPost) -> Unit
) {
LazyColumn(
contentPadding = PaddingValues(16.dp),
verticalArrangement = Arrangement.spacedBy(12.dp)
) {
items(feedPosts) { post ->
CommunityPostCard(
post = post,
onLike = { onLikePost(post.id) },
onComment = { comment -> onCommentPost(post.id, comment) },
onShare = { onSharePost(post) }
)
}
}
}

// 2. Challenge System
class ChallengeManager(
private val repository: ChallengeRepository,
private val notificationManager: NotificationManager
) {
suspend fun createChallenge(
title: String,
description: String,
type: ChallengeType,
duration: Int,
targetValue: Float
): Result<Challenge> = try {
val challenge = Challenge(
id = UUID.randomUUID().toString(),
title = title,
description = description,
type = type.name,
duration = duration,
targetValue = targetValue,
startDate = LocalDate.now().toString(),
endDate = LocalDate.now().plusDays(duration.toLong()).toString()
)

        repository.insertChallenge(challenge)
        notificationManager.scheduleChallengReminders(challenge)
        Result.success(challenge)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun joinChallenge(challengeId: String, userId: String): Result<Unit> {
        // Implementation for challenge participation
    }
    }

```

### Enhanced Analytics & Progress Tracking
```

/* Pattern for comprehensive analytics similar to enhanced apps */

// 1. Advanced Analytics Data Classes
data class AdvancedAnalytics(
val period: AnalyticsPeriod,
val workoutCompletionRate: Float,
val averageIntensity: Float,
val progressVelocity: Float,
val streakAnalysis: StreakAnalysis,
val predictedGoalCompletion: GoalPrediction,
val recommendedAdjustments: List<Recommendation>
)

// 2. AI-Powered Analytics Engine
class AnalyticsEngine(
private val aiService: AppAi,
private val dataRepository: AnalyticsRepository
) {
suspend fun generateAdvancedInsights(
userId: String,
period: AnalyticsPeriod
): AdvancedAnalytics {
val rawData = dataRepository.getUserData(userId, period)
val aiInsights = aiService.analyzeUserProgress(rawData)

        return AdvancedAnalytics(
            period = period,
            workoutCompletionRate = calculateCompletionRate(rawData),
            averageIntensity = calculateAverageIntensity(rawData),
            progressVelocity = calculateProgressVelocity(rawData),
            streakAnalysis = analyzeStreaks(rawData),
            predictedGoalCompletion = aiInsights.goalPrediction,
            recommendedAdjustments = aiInsights.recommendations
        )
    }
    }

// 3. Enhanced Progress Visualization
@Composable
fun EnhancedAnalyticsScreen(
analytics: AdvancedAnalytics,
onRecommendationAccepted: (Recommendation) -> Unit
) {
LazyColumn(
contentPadding = PaddingValues(16.dp),
verticalArrangement = Arrangement.spacedBy(16.dp)
) {
// Progress Overview Cards
item {
ProgressOverviewCards(analytics)
}

        // Interactive Charts
        item {
            InteractiveProgressChart(analytics.progressVelocity)
        }
        
        // AI Recommendations
        item {
            AIRecommendationsSection(
                recommendations = analytics.recommendedAdjustments,
                onAccepted = onRecommendationAccepted
            )
        }
        
        // Predictive Analytics
        item {
            GoalPredictionCard(analytics.predictedGoalCompletion)
        }
    }
    }

```

### Gamification & Motivation System
```

/* Advanced gamification patterns inspired by successful apps */

// 1. Dynamic Achievement System
class AchievementEngine(
private val repository: AchievementRepository,
private val userDataRepository: UserDataRepository
) {
suspend fun checkAndUnlockAchievements(userId: String, trigger: AchievementTrigger) {
val userData = userDataRepository.getUserStats(userId)
val eligibleAchievements = repository.getEligibleAchievements(userData, trigger)

        eligibleAchievements.forEach { achievement ->
            if (achievement.isUnlockable(userData)) {
                unlockAchievement(userId, achievement)
                showAchievementNotification(achievement)
            }
        }
    }
    
    private suspend fun unlockAchievement(userId: String, achievement: Achievement) {
        repository.unlockAchievement(userId, achievement.id)
        // Trigger celebration animation and reward
    }
    }

// 2. Streak Management with Psychology
class AdvancedStreakManager(
private val repository: StreakRepository,
private val motivationEngine: MotivationEngine
) {
suspend fun updateStreak(userId: String, activity: StreakActivity): StreakResult {
val currentStreak = repository.getCurrentStreak(userId, activity)
val updatedStreak = calculateNewStreak(currentStreak, activity)

        repository.updateStreak(updatedStreak)
        
        return when {
            updatedStreak.length > currentStreak.length -> {
                motivationEngine.sendStreakCelebration(updatedStreak)
                StreakResult.Extended(updatedStreak)
            }
            updatedStreak.length == 0 -> {
                motivationEngine.sendMotivationalRecovery(userId)
                StreakResult.Broken(updatedStreak)
            }
            else -> StreakResult.Maintained(updatedStreak)
        }
    }
    }

```

## Project Structure & Navigation

```

app/src/main/java/com/example/fitapp/
├── ai/                    \# AI integration (Gemini, Perplexity, adaptive algorithms)
│   ├── AppAi.kt
│   ├── WeightLossAI.kt
│   └── adaptive/          \# New: Adaptive training algorithms
├── data/                  \# Repository pattern, Room database
│   ├── db/               \# Database entities, DAOs, migrations
│   │   ├── Entities.kt
│   │   ├── AppDatabase.kt
│   │   └── migrations/   \# Schema migrations
│   ├── repo/             \# Repository implementations
│   └── prefs/            \# SharedPreferences wrappers
├── services/             \# Business logic managers
│   ├── PersonalAchievementManager.kt
│   ├── PersonalStreakManager.kt
│   └── community/        \# New: Community and social features
├── ui/                   \# Compose screens and components
│   ├── screens/          \# All screen composables
│   ├── components/       \# Reusable UI components
│   ├── theme/            \# Material 3 theming
│   └── social/           \# New: Social UI components
├── utils/                \# Utility classes
└── domain/               \# New: Business logic and use cases
├── models/           \# Domain models
├── usecases/         \# Business use cases
└── repositories/     \# Repository interfaces

```

## Feature Implementation Priorities

### Phase 1: AI & Personalization (Freeletics-Inspired)
1. **Workout Feedback Collection**: Add feedback dialog after workout completion
2. **Adaptive Training Algorithm**: Implement AI-based workout adjustment
3. **Personalized Recommendations**: Extend existing AI system for dynamic suggestions
4. **Performance Analytics**: Enhanced progress tracking with ML insights

### Phase 2: Community & Social (Strava/Social Apps)
1. **Community Feed**: Social sharing of workouts and achievements
2. **Challenge System**: Group and individual fitness challenges
3. **Social Interactions**: Like, comment, share functionality
4. **Friend Connections**: User-to-user social networking

### Phase 3: Advanced Features
1. **Enhanced Analytics**: Predictive analytics and AI insights
2. **Offline Mode**: Complete app functionality without internet
3. **Advanced Gamification**: Dynamic achievement generation
4. **Wearable Integration**: Full health device connectivity

## Testing Strategy

### Unit Tests (Currently Have Compilation Errors)
```


# Attempt unit tests (will fail with compilation errors)

./gradlew testDebugUnitTest --continue

# Status: FAILS - compilation errors in test code

# Use --continue flag to see all errors

```

**Test Structure:**
- Unit tests: `app/src/test/java/`
- Instrumented tests: `app/src/androidTest/java/`
- Test categories: AI services, nutrition management, workout management, cooking mode

**When implementing new features, always include:**
```

// Example test structure for new features
class AdaptiveTrainingManagerTest {
@Test
fun `adaptNextWorkout adjusts difficulty based on feedback`() = runTest {
// Test implementation
}

    @Test
    fun `community feed displays posts in chronological order`() = runTest {
        // Test implementation
    }
    }

```

## Configuration & Dependencies

### Key Dependencies (gradle/libs.versions.toml)
- Compose BOM 2024.10.01
- Kotlin 2.0.20  
- Room 2.6.1
- Android Gradle Plugin 8.12.1
- CameraX, ML Kit, Retrofit, OkHttp

### Performance Optimization
The project uses advanced Gradle configuration:
- **Configuration cache**: Enabled - speeds up builds significantly
- **Build cache**: Enabled - reuses outputs between builds  
- **Parallel execution**: Enabled for faster dependency resolution
- Cache-related warnings are normal and do not indicate build problems

## API Keys & Configuration

For full functionality, add to `local.properties`:
```

GEMINI_API_KEY=your_gemini_key_here
PERPLEXITY_API_KEY=your_perplexity_key_here

```

Without API keys, the app builds successfully but AI features will not function.

## Validation Workflow

After making changes, always test:
1. **Build Validation**: Clean build succeeds without errors
2. **Lint Validation**: No new critical lint issues introduced  
3. **Schema Validation**: If database changes made, ensure schemas are committed
4. **Manual Testing**: App installs, launches, and core navigation works

## Common Issues & Solutions

### Build Cache Issues
```


# If builds seem corrupted, clear caches

./gradlew --stop
./gradlew clean

# Then rebuild normally

```

### Memory Issues
```


# If out of memory errors occur

export GRADLE_OPTS="-Xmx4g"
./gradlew clean assembleDebug

```

## AI Development Best Practices

When using GitHub Copilot for this project:

### Always Consider These Patterns:
1. **Existing Architecture**: Follow Repository pattern and Clean Architecture principles
2. **Material 3 Consistency**: Use established theming and component patterns  
3. **AI Integration**: Leverage existing AppAi and WeightLossAI infrastructure
4. **Performance**: Consider build times, memory usage, and user experience
5. **Offline Support**: Design features to work without constant internet connection

### Prompt Optimization for Complex Features:
When implementing features inspired by Freeletics, Nike Training Club, or other apps:
- Specify the inspiration source (e.g., "implement Freeletics-style adaptive training")
- Reference existing entities and patterns (e.g., "extend PersonalAchievementEntity")
- Include error handling and edge cases
- Consider offline functionality and data synchronization
- Always include proper Material 3 UI components

### Code Generation Guidelines:
- Suspend functions for database operations
- Flow<T> returns for reactive data streams
- Sealed classes for UI state management  
- Extension functions for utility operations
- Data classes with sensible defaults
- Proper exception handling with Result<T> types

## CI/CD Integration

**GitHub Actions Workflows:**
- `android-room-ci.yml`: Build + Unit Tests + Schema Guard (7+ minutes)
- `android_tests.yml`: Unit and instrumented tests
- Expected CI timing: 7-15 minutes total

**Schema Management:** Room database schemas are tracked in `app/schemas/`. CI fails if schema changes are not committed.

---

**Remember**: This is a sophisticated Android project with extensive dependencies and modern tooling. Build times of 4-7 minutes are normal and expected. Always wait for completion and never cancel long-running build processes.
```

Diese vollständige Copilot Instructions-Datei integriert:

**✅ Alle recherchierten Apps:**

- Freeletics (AI, Personalisierung, Bodyweight-Training)
- Nike Training Club (Content, Wellness, Programme)
- Strava (Community, Social Features)
- Moderne Fitness-App Best Practices

**✅ FitApp-spezifische Optimierungen:**

- Bestehende Architektur und Entities
- Build-Prozesse und Timeouts
- AI-Integration mit Gemini/Perplexity
- Material 3 Design System

**✅ Konkrete Implementierungsanweisungen:**

- Code-Beispiele für neue Features
- Datenbankschema-Erweiterungen
- UI-Patterns mit Jetpack Compose
- Testing-Strategien

**✅ Copilot-optimiert:**

- Klare, actionable Anweisungen
- Spezifische Code-Patterns
- Feature-Priorisierung
- Fehlerbehandlung und Edge Cases

Diese Datei kann direkt als `.github/copilot-instructions.md` in dein Repository kopiert werden und wird Copilot optimal dabei helfen, Features aus den besten Fitness-Apps der Welt in deine FitApp zu integrieren! 🚀
<span style="display:none">[^1][^10][^100][^11][^12][^13][^14][^15][^16][^17][^18][^19][^2][^20][^21][^22][^23][^24][^25][^26][^27][^28][^29][^3][^30][^31][^32][^33][^34][^35][^36][^37][^38][^39][^4][^40][^41][^42][^43][^44][^45][^46][^47][^48][^49][^5][^50][^51][^52][^53][^54][^55][^56][^57][^58][^59][^6][^60][^61][^62][^63][^64][^65][^66][^67][^68][^69][^7][^70][^71][^72][^73][^74][^75][^76][^77][^78][^79][^8][^80][^81][^82][^83][^84][^85][^86][^87][^88][^89][^9][^90][^91][^92][^93][^94][^95][^96][^97][^98][^99]</span>

<div style="text-align: center">⁂</div>

[^1]: https://www.semanticscholar.org/paper/049aaf11fa98525b663da18f39d5dcc5d345eb9a

[^2]: http://www.journalmtm.com/2014/fitness-apps-a-valid-alternative-to-the-gym-a-pilot-study/

[^3]: https://www.mdpi.com/1996-1073/14/6/1545

[^4]: https://www.semanticscholar.org/paper/e62e9bd3746d348888246a2c254ac909baeae9aa

[^5]: https://ijcat.com/archieve/volume9/issue2/ijcatr09021001.pdf

[^6]: https://www.semanticscholar.org/paper/d317db0402b63e961dfed00c6c83dba888e07c77

[^7]: http://ojs.academypublisher.com/index.php/jcp/article/view/4418

[^8]: https://www.semanticscholar.org/paper/907030cd9f5fee5f870f0f5d726d220b7694c64c

[^9]: https://arxiv.org/pdf/1803.08666.pdf

[^10]: http://arxiv.org/pdf/2201.01429.pdf

[^11]: https://arxiv.org/pdf/2310.02432.pdf

[^12]: https://jmir.org/api/download?alt_name=mhealth_v6i6e143_app1.pdf

[^13]: https://www.mdpi.com/1424-8220/19/1/108/pdf

[^14]: https://peerj.com/articles/17100

[^15]: https://journals.e-palli.com/home/index.php/ajiri/article/download/3774/1801

[^16]: https://downloads.hindawi.com/journals/jhe/2022/9113569.pdf

[^17]: http://science-gate.com/IJAAS/Articles/2020/2020-7-10/1021833ijaas202010015.pdf

[^18]: http://arxiv.org/pdf/1209.1734.pdf

[^19]: https://arxiv.org/pdf/2410.03795.pdf

[^20]: https://www.mdpi.com/2076-3417/13/2/1198/pdf?version=1673860397

[^21]: https://www.behance.net/search/projects/nike training club

[^22]: https://www.appventurez.com/blog/nike-training-club-app-case-study

[^23]: https://litslink.com/blog/how-to-build-a-next-level-fitness-app

[^24]: https://www.figma.com/community/file/1443095010042702817/nike-club-elevating-performance-defining-excellence

[^25]: https://www.delangedesign.co.uk/project/nike-training-club-app

[^26]: https://www.reddit.com/r/androiddev/comments/1ao786p/any_good_public_examples_of_clean_architecture/

[^27]: https://www.reddit.com/r/workout/comments/1jbue2g/communitydriven_fitness_app/

[^28]: https://stormotion.io/blog/fitness-app-ideas/

[^29]: https://www.slant.co/versus/2683/18448/~nike-training-club_vs_freeletics-bodyweight-workouts-and-training

[^30]: https://ixd.prattsi.org/2023/09/design-critique-nike-training-club-iphone-app/

[^31]: https://dev.to/paulallies/clean-architecture-in-the-flavour-of-jetpack-compose-1j1

[^32]: https://www.social.plus/blog/fitness-is-social-top-6-features-all-successful-apps-share

[^33]: https://dribbble.com/search/modern-fitness-app

[^34]: https://www.nottooold.de/koerper/fitness/freeletics-orte-ultimate-strength/

[^35]: https://ckirkham.github.io/projects/nike-training-club.html

[^36]: https://github.com/aliahmedbd/WeatherApp-Android-Clean-Architecture-Jetpack-Compose-Kotlin-Hilt-Flow

[^37]: https://www.fitbudd.com/post/15-essential-features-for-a-custom-fitness-app

[^38]: https://dribbble.com/tags/fitness-app

[^39]: https://dieringe.com/blog/home-workout-apps

[^40]: https://cargocollective.com/tonycianci/Nike-Training-Club

[^41]: https://dl.acm.org/doi/10.1145/3639478.3640023

[^42]: https://ieeexplore.ieee.org/document/10981409/

[^43]: https://arxiv.org/abs/2407.14679

[^44]: https://agupubs.onlinelibrary.wiley.com/doi/10.1002/2015EA000125

[^45]: https://www.ans.org/pubs/proceedings/article-56788/

[^46]: https://www.science.org/doi/10.1126/scitranslmed.3006112

[^47]: https://microbiomejournal.biomedcentral.com/articles/10.1186/2049-2618-2-8

[^48]: https://arxiv.org/pdf/2403.08299.pdf

[^49]: https://arxiv.org/pdf/2309.05687.pdf

[^50]: http://arxiv.org/pdf/2401.14176.pdf

[^51]: https://arxiv.org/pdf/2211.13063.pdf

[^52]: https://arxiv.org/pdf/2303.08733.pdf

[^53]: https://arxiv.org/pdf/2206.15331.pdf

[^54]: https://arxiv.org/pdf/2210.15157.pdf

[^55]: https://arxiv.org/pdf/2502.13199.pdf

[^56]: https://arxiv.org/pdf/2406.17910.pdf

[^57]: https://arxiv.org/pdf/2304.02491.pdf

[^58]: https://arxiv.org/pdf/2209.01766.pdf

[^59]: https://arxiv.org/pdf/2309.00608.pdf

[^60]: https://arxiv.org/pdf/2206.12839.pdf

[^61]: https://www.reddit.com/r/GithubCopilot/comments/1lfz0wt/guidance_on_efficient_use_of_copilotinstructionsmd/

[^62]: https://github.blog/ai-and-ml/github-copilot/5-tips-for-writing-better-custom-instructions-for-copilot/

[^63]: https://docs.github.com/copilot/customizing-copilot/adding-custom-instructions-for-github-copilot

[^64]: https://docs.github.com/enterprise-cloud@latest/copilot/how-tos/agents/copilot-coding-agent/best-practices-for-using-copilot-to-work-on-tasks

[^65]: https://copilotthatjawn.com/tips/copilot-instructions-prompt-files.md

[^66]: https://dev.to/anchildress1/all-ive-learned-about-github-copilot-instructions-so-far-5bm7

[^67]: https://docs.github.com/en/copilot/get-started/best-practices

[^68]: https://code.visualstudio.com/docs/copilot/customization/custom-instructions

[^69]: https://ieeexplore.ieee.org/document/10485592/

[^70]: https://ijarsct.co.in/Paper22088.pdf

[^71]: http://www.emerald.com/jibr/article/16/3/329-352/1219146

[^72]: https://www.cambridge.org/core/product/identifier/S2059479824000309/type/journal_article

[^73]: https://link.springer.com/10.1007/s11192-022-04307-8

[^74]: http://preprints.jmir.org/preprint/17552

[^75]: https://www.mdpi.com/2076-2615/14/13/1923

[^76]: http://www.ijiris.com/volumes/Vol10/iss-03/32.APIS10111.pdf

[^77]: https://techniumscience.com/index.php/technium/article/view/12871

[^78]: https://spppc.com.ua/index.php/journal/article/view/2

[^79]: https://bmchealthservres.biomedcentral.com/articles/10.1186/s12913-025-12489-z

[^80]: https://pmc.ncbi.nlm.nih.gov/articles/PMC6028765/

[^81]: https://pmc.ncbi.nlm.nih.gov/articles/PMC4785735/

[^82]: https://www.mdpi.com/2414-4088/4/2/17/pdf

[^83]: https://pmc.ncbi.nlm.nih.gov/articles/PMC9227785/

[^84]: https://mhealth.jmir.org/2021/6/e22587/PDF

[^85]: https://pmc.ncbi.nlm.nih.gov/articles/PMC8512175/

[^86]: https://onestep4ward.com/fitness-app-features-overview-and-functional-breakdown/

[^87]: https://attractgroup.com/blog/10-essential-features-for-a-fitness-app-build-a-fitness-app-today/

[^88]: https://www.theappfounders.com/resources/fitness-app-development/

[^89]: https://www.upsilonit.com/blog/how-to-create-a-fitness-app-comprehensive-guide

[^90]: https://kaopiz.com/en/articles/fitness-app-development/

[^91]: https://devblogs.microsoft.com/dotnet/customize-ai-responses-from-github-copilot/

[^92]: https://github.com/Code-and-Sorts/awesome-copilot-instructions

[^93]: https://www.fullestop.com/blog/fitness-app-development-features-trends-and-costs

[^94]: https://copilot-instructions.md

[^95]: https://www.brilworks.com/blog/fitness-app-development/

[^96]: https://coderower.com/blogs/fitness-application-development-guide

[^97]: https://lampa.dev/blog/top-10-features-to-build-a-successful-fitness-app

[^98]: https://code.visualstudio.com/blogs/2025/03/26/custom-instructions

[^99]: https://www.reddit.com/r/GithubCopilot/comments/1llss4p/this_is_my_generalinstructionsmd_file_to_use_with/

[^100]: https://github.blog/ai-and-ml/github-copilot/from-chaos-to-clarity-using-github-copilot-agents-to-improve-developer-workflows/

