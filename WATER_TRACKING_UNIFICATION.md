# Water Tracking Goals Unification - Implementation Summary

## Problem Solved
Issues #269 and #273 identified fragmented water tracking goals with hardcoded 2000ml values scattered across the app, leading to inconsistent hydration goal logic.

## Solution Implemented
Created a unified single source of truth for hydration goals using the existing Proto DataStore infrastructure and a new domain use case.

### Key Components

#### 1. HydrationGoalUseCase
**Location**: `app/src/main/java/com/example/fitapp/domain/usecases/HydrationGoalUseCase.kt`

**Priority Logic**:
1. `DailyGoalEntity.targetWaterMl` (if > 0) - Daily specific goals
2. `UserPreferencesProto.dailyWaterGoalLiters * 1000` (if > 0) - User default preference  
3. `DEFAULT_DAILY_WATER_GOAL_ML` (2000) - Fallback constant

**Key Methods**:
- `getHydrationGoalMl(date: LocalDate): Int` - Get goal for specific date
- `getTodaysHydrationGoalMl(): Int` - Get today's goal
- `updateDefaultHydrationGoalMl(goalMl: Int)` - Update user's default goal
- `create(context: Context)` - Factory method

#### 2. Updated Files
**WaterReminderWorker.kt**:
- Removed: `val targetIntake = 2000`
- Added: `HydrationGoalUseCase.create(applicationContext).getTodaysHydrationGoalMl()`

**FoodDiaryScreen.kt**:
- Removed: `targetWater = goal?.targetWaterMl ?: 2000`
- Added: `hydrationGoalUseCase.getHydrationGoalMl(today)` with reactive updates

**NutritionAnalyticsScreen.kt**:
- Removed: `targetWater = goal?.targetWaterMl ?: 2000`
- Added: `hydrationGoalUseCase.getHydrationGoalMl(currentDate)` for each date

#### 3. Test Coverage
**Location**: `app/src/test/java/com/example/fitapp/domain/usecases/HydrationGoalUseCaseTest.kt`

**Test Cases**:
- Daily goal takes priority over user preferences
- User preferences used when daily goal is zero/null
- Default fallback when no goals available
- Proper unit conversion (liters to ml)
- Priority logic consistency

## Benefits Achieved

### ✅ Single Source of Truth
- All hydration goals now flow through `HydrationGoalUseCase`
- No more scattered hardcoded values
- Consistent behavior across the entire app

### ✅ Backward Compatibility
- Existing `DailyGoalEntity` system preserved
- User preferences in DataStore leveraged
- Graceful fallbacks to default values

### ✅ Minimal Changes
- No data migration required
- No breaking changes to existing functionality
- Leveraged existing Proto DataStore infrastructure

### ✅ Extensible Design
- Easy to add new goal sources in the future
- Reactive flows for UI updates
- Clear separation of concerns (domain layer)

## Technical Details

### Data Flow
```
UI/Service → HydrationGoalUseCase → Priority Check:
1. DailyGoalEntity (Room DB)
2. UserPreferencesProto (DataStore)  
3. Default constant (2000ml)
```

### Dependencies
- Uses existing `NutritionRepository` for daily goals
- Uses existing `UserPreferencesRepository` for default preferences
- Factory pattern for easy instantiation with Android context

### Migration Strategy
The solution leverages the existing Proto DataStore which already contains:
- `daily_water_goal_liters` field (line 21 in user_preferences.proto)
- Migration logic from SharedPreferences
- Default value handling

## Verification

### Build Status
✅ Code compiles successfully
✅ No breaking changes to existing functionality

### Hardcoded Value Removal
✅ WaterReminderWorker: Removed hardcoded 2000ml
✅ FoodDiaryScreen: Removed hardcoded 2000ml fallback
✅ NutritionAnalyticsScreen: Removed hardcoded 2000ml fallback

### Integration Points
✅ All water tracking UI now uses HydrationGoalUseCase
✅ Background reminders use unified goal logic
✅ Analytics screens show consistent targets

## Future Considerations

1. **Enhanced User Preferences**: Could add UI to modify default hydration goals
2. **Adaptive Goals**: Could integrate with user profile (weight, activity level) for dynamic goals
3. **Goal Templates**: Could add preset goal templates for different activity levels
4. **History Tracking**: Could track goal changes over time for analytics

## Files Changed
- `app/src/main/java/com/example/fitapp/domain/usecases/HydrationGoalUseCase.kt` (NEW)
- `app/src/main/java/com/example/fitapp/services/WaterReminderWorker.kt` (MODIFIED)
- `app/src/main/java/com/example/fitapp/ui/nutrition/FoodDiaryScreen.kt` (MODIFIED)
- `app/src/main/java/com/example/fitapp/ui/nutrition/NutritionAnalyticsScreen.kt` (MODIFIED)
- `app/src/test/java/com/example/fitapp/domain/usecases/HydrationGoalUseCaseTest.kt` (NEW)

Total: 5 files (2 new, 3 modified)