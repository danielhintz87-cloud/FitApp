# Water Tracking Unification Guide

This document explains the unified water tracking system implemented via `HydrationGoalUseCase` that serves as the single source of truth for hydration goals throughout the FitApp.

## Problem Solved

Previously, water tracking goals were fragmented across the codebase with hardcoded 2000ml values scattered throughout:
- `WaterReminderWorker`: Used hardcoded 2000ml target
- `FoodDiaryScreen`: Fallback to hardcoded 2000ml when `DailyGoalEntity.targetWaterMl` was null
- `NutritionAnalyticsScreen`: Same hardcoded fallback logic

This created inconsistency where different parts of the app could show different water targets for the same day.

## Solution Architecture

### Single Source of Truth: HydrationGoalUseCase

The `HydrationGoalUseCase` implements a clear priority hierarchy:

1. **DailyGoalEntity.targetWaterMl** (if > 0) - Date-specific goals from Room database
2. **UserPreferencesProto.dailyWaterGoalLiters × 1000** (if > 0) - User's default preference from DataStore
3. **DEFAULT_DAILY_WATER_GOAL_ML (2000)** - Final fallback constant

### Key Features

✅ **Reactive Updates**: UI automatically updates when goals change  
✅ **Timezone-Safe**: Uses `ZoneId.systemDefault()` for consistent day boundaries  
✅ **Thread-Safe**: All methods work from any coroutine context  
✅ **Migration Support**: Seamless migration from legacy SharedPreferences  
✅ **Backward Compatible**: Preserves existing DailyGoalEntity and DataStore functionality  

## Integration Examples

### Basic Usage

```kotlin
val hydrationGoalUseCase = HydrationGoalUseCase.create(context)

// Get goal for a specific date
val goalMl = hydrationGoalUseCase.getHydrationGoalMl(LocalDate.of(2025, 1, 15))

// Get today's goal (timezone-safe)
val todaysGoal = hydrationGoalUseCase.getTodaysHydrationGoalMl()
```

### Reactive UI Updates (Recommended)

```kotlin
@Composable
fun WaterTrackingScreen() {
    val hydrationGoalUseCase = remember { HydrationGoalUseCase.create(LocalContext.current) }
    val today = remember { LocalDate.now(ZoneId.systemDefault()) }
    
    // Automatically updates when goal changes
    val hydrationGoal by hydrationGoalUseCase.getHydrationGoalMlFlow(today)
        .collectAsState(initial = 2000)
    
    // UI will recompose when hydrationGoal changes
    Text("Daily Goal: ${hydrationGoal}ml")
    
    // Progress calculation
    val progress = currentIntake.toFloat() / hydrationGoal.toFloat()
}
```

### WorkManager Integration

```kotlin
class WaterReminderWorker : CoroutineWorker {
    override suspend fun doWork(): Result {
        val hydrationGoalUseCase = HydrationGoalUseCase.create(applicationContext)
        val targetIntake = hydrationGoalUseCase.getTodaysHydrationGoalMl()
        
        // Use targetIntake for reminder logic
        SmartNotificationManager.showWaterReminder(
            applicationContext,
            currentIntake,
            targetIntake
        )
        
        return Result.success()
    }
}
```

### Updating Default Goals

```kotlin
// Update the user's default hydration goal
hydrationGoalUseCase.updateDefaultHydrationGoalMl(2500) // 2.5L

// This will trigger reactive updates in all observing UI components
// Any screen using getHydrationGoalMlFlow() will automatically update
```

## Migration from Legacy Code

### Before (Fragmented)
```kotlin
// Different hardcoded values across the app
val targetIntake = 2000 // Hardcoded in WaterReminderWorker
val targetWater = goal?.targetWaterMl ?: 2000 // Hardcoded fallback in UI
```

### After (Unified)
```kotlin
val hydrationGoalUseCase = HydrationGoalUseCase.create(context)
val targetWater = hydrationGoalUseCase.getHydrationGoalMl(date)
```

## Data Flow

```
┌─────────────────────────┐    ┌─────────────────────────┐
│   DailyGoalEntity       │    │  UserPreferencesProto   │
│   .targetWaterMl        │    │  .dailyWaterGoalLiters  │
│   (Room Database)       │    │  (DataStore Proto)      │
└─────────┬───────────────┘    └─────────┬───────────────┘
          │                              │
          │ Priority 1                   │ Priority 2
          │ (if > 0)                     │ (if > 0, × 1000)
          │                              │
          └────────┬─────────────────────┘
                   │
                   ▼
    ┌─────────────────────────────────┐
    │     HydrationGoalUseCase        │
    │   combine() + priority logic    │
    └─────────────┬───────────────────┘
                  │
                  │ Priority 3: DEFAULT_DAILY_WATER_GOAL_ML (2000)
                  │
                  ▼
    ┌─────────────────────────────────┐
    │        UI Components            │
    │   • FoodDiaryScreen             │
    │   • NutritionAnalyticsScreen    │
    │   • WaterReminderWorker         │
    │   • Any other water tracking    │
    └─────────────────────────────────┘
```

## Timezone Considerations

The system handles timezone changes and DST transitions safely:

- **LocalDate.now(ZoneId.systemDefault())**: Explicit timezone handling
- **Day Boundary Logic**: Consistent across timezone changes
- **DST Transitions**: Verified with comprehensive tests

### Example DST Handling
```kotlin
// Spring forward: 2:00 AM -> 3:00 AM (lost hour)
val dstDate = LocalDate.of(2025, 3, 30)
val goal = hydrationGoalUseCase.getHydrationGoalMl(dstDate)
// Returns correct goal regardless of DST transition

// Fall back: 2:00 AM -> 1:00 AM (gained hour)  
val fallBackDate = LocalDate.of(2025, 10, 26)
val goal2 = hydrationGoalUseCase.getHydrationGoalMl(fallBackDate)
// Returns correct goal regardless of DST transition
```

## Testing Strategy

### Unit Tests
- **Priority Logic**: Verify correct goal selection
- **Reactive Flows**: Test UI update triggers
- **Timezone Safety**: DST and timezone change scenarios
- **Migration**: SharedPreferences to DataStore conversion

### Integration Tests
```kotlin
@Test
fun `reactive flow updates UI when goal changes`() = runTest {
    // Given: UI observing hydration goal
    val flow = hydrationGoalUseCase.getHydrationGoalMlFlow(date)
    
    // When: Goal changes (via user preference or daily goal)
    userPrefsRepo.updateNutritionPreferences(dailyWaterGoalLiters = 3.0)
    
    // Then: Flow emits new value
    assertEquals(3000, flow.first())
}
```

## Performance Considerations

- **Flow Combination**: Efficient combining of Room and DataStore flows
- **Memory Usage**: No unnecessary caching, relies on upstream optimizations
- **Threading**: All operations respect caller's dispatcher context

## Future Extensibility

The architecture supports future enhancements:

- **Adaptive Goals**: Machine learning-based goal adjustments
- **Weather Integration**: Increase goals on hot days
- **Activity Tracking**: Adjust based on workout intensity
- **Health Connect**: Sync with health platforms

```kotlin
// Future enhancement example
class AdaptiveHydrationGoalUseCase(
    private val baseUseCase: HydrationGoalUseCase,
    private val weatherService: WeatherService,
    private val activityTracker: ActivityTracker
) {
    suspend fun getAdaptiveGoalMl(date: LocalDate): Int {
        val baseGoal = baseUseCase.getHydrationGoalMl(date)
        val weatherMultiplier = weatherService.getHydrationMultiplier(date)
        val activityMultiplier = activityTracker.getHydrationMultiplier(date)
        
        return (baseGoal * weatherMultiplier * activityMultiplier).toInt()
    }
}
```

## Troubleshooting

### Common Issues

1. **UI Not Updating**: Ensure you're using `getHydrationGoalMlFlow()` instead of one-time `getHydrationGoalMl()`
2. **Wrong Date**: Check timezone handling - use `LocalDate.now(ZoneId.systemDefault())`
3. **Migration Issues**: Verify `UserPreferencesRepository.migrateFromSharedPreferences()` was called

### Debug Logging
```kotlin
// Add to HydrationGoalUseCase for debugging
private suspend fun debugLogGoalResolution(date: LocalDate): Int {
    val dailyGoal = nutritionRepository.goalFlow(date).first()
    val userPrefs = userPreferencesRepository.nutritionPreferences.first()
    
    Log.d("HydrationGoal", "Date: $date")
    Log.d("HydrationGoal", "Daily goal: ${dailyGoal?.targetWaterMl}")
    Log.d("HydrationGoal", "User pref: ${userPrefs.dailyWaterGoalLiters}L")
    
    return getHydrationGoalMl(date).also { result ->
        Log.d("HydrationGoal", "Final goal: ${result}ml")
    }
}
```

## Related Files

- **Use Case**: `app/src/main/java/com/example/fitapp/domain/usecases/HydrationGoalUseCase.kt`
- **Tests**: `app/src/test/java/com/example/fitapp/domain/usecases/HydrationGoalUseCase*Test.kt`
- **Consumer Examples**:
  - `app/src/main/java/com/example/fitapp/ui/nutrition/FoodDiaryScreen.kt`
  - `app/src/main/java/com/example/fitapp/ui/nutrition/NutritionAnalyticsScreen.kt`
  - `app/src/main/java/com/example/fitapp/services/WaterReminderWorker.kt`
- **Data Sources**:
  - `app/src/main/java/com/example/fitapp/data/repo/NutritionRepository.kt`
  - `app/src/main/java/com/example/fitapp/data/prefs/UserPreferencesRepository.kt`