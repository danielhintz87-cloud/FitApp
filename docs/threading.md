# Threading and Dispatcher Policy

This guide covers threading patterns, dispatcher injection, and anti-patterns to avoid in the FitApp project.

## Overview

FitApp follows **strict threading discipline** using dependency injection for dispatchers. This ensures testable, maintainable code while preventing common threading issues like NetworkOnMainThreadException.

## Core Principles

### 🚫 NEVER Use Dispatchers Directly

```kotlin
// ❌ DON'T - Direct dispatcher usage
class BadRepository {
    suspend fun loadData() = withContext(Dispatchers.IO) {
        // Network call
    }
}

// ✅ DO - Inject dispatchers
class GoodRepository @Inject constructor(
    private val dispatchers: DispatcherProvider
) {
    suspend fun loadData() = withContext(dispatchers.io) {
        // Network call
    }
}
```

### ✅ Always Inject DispatcherProvider

```kotlin
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

@Singleton
class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}
```

## Dispatcher Usage Guidelines

### When to Use Each Dispatcher

| Dispatcher | Use Case | Examples |
|------------|----------|----------|
| `dispatchers.main` | UI updates, StateFlow emissions | Updating UI state, navigation |
| `dispatchers.io` | I/O operations, network calls, database | API calls, Room queries, file operations |
| `dispatchers.default` | CPU-intensive work | JSON parsing, image processing, calculations |
| `dispatchers.unconfined` | Testing only | Unit tests, immediate execution |

### Repository Pattern

```kotlin
@Singleton
class RecipeRepository @Inject constructor(
    private val apiService: ApiService,
    private val recipeDao: RecipeDao,
    private val dispatchers: DispatcherProvider
) {
    // ✅ Network operations on IO dispatcher
    suspend fun fetchRecipes(): Result<List<Recipe>> = withContext(dispatchers.io) {
        try {
            val recipes = apiService.getRecipes()
            recipeDao.insertAll(recipes)
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ✅ Database operations on IO dispatcher  
    fun getRecipesFlow(): Flow<List<Recipe>> = recipeDao.getAllRecipes()
        .flowOn(dispatchers.io)
    
    // ✅ CPU-intensive work on Default dispatcher
    suspend fun processRecipeData(recipes: List<Recipe>): List<ProcessedRecipe> = 
        withContext(dispatchers.default) {
            recipes.map { recipe ->
                // Complex processing logic
                processRecipe(recipe)
            }
        }
}
```

### ViewModel Pattern

```kotlin
@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val repository: RecipeRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()
    
    // ✅ Launch coroutines with explicit scope and dispatcher
    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Repository handles IO dispatcher internally
            val result = repository.fetchRecipes()
            
            // ✅ Update UI state on Main dispatcher (implicit with viewModelScope)
            _uiState.value = when (result) {
                is Result.Success -> _uiState.value.copy(
                    isLoading = false,
                    recipes = result.data
                )
                is Result.Failure -> _uiState.value.copy(
                    isLoading = false,
                    error = result.exception.message
                )
            }
        }
    }
    
    // ✅ Background processing with explicit dispatcher
    fun processRecipeInBackground(recipe: Recipe) {
        viewModelScope.launch(dispatchers.default) {
            val processed = processComplexRecipe(recipe)
            
            // ✅ Switch to Main for UI update
            withContext(dispatchers.main) {
                _uiState.value = _uiState.value.copy(
                    processedRecipe = processed
                )
            }
        }
    }
}
```

### Service/Manager Pattern

```kotlin
@Singleton
class WorkoutManager @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val healthService: HealthService,
    private val dispatchers: DispatcherProvider
) {
    // ✅ Combine multiple I/O operations
    suspend fun syncWorkoutData(workoutId: String): Result<Workout> = withContext(dispatchers.io) {
        try {
            // Both operations happen on IO dispatcher
            val localWorkout = workoutDao.getWorkout(workoutId)
            val healthData = healthService.getHealthData(workoutId)
            
            val syncedWorkout = mergeWorkoutData(localWorkout, healthData)
            workoutDao.update(syncedWorkout)
            
            Result.success(syncedWorkout)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ✅ Flow operations with proper dispatcher
    fun getWorkoutUpdates(workoutId: String): Flow<Workout> = 
        workoutDao.getWorkoutFlow(workoutId)
            .flowOn(dispatchers.io)
            .catch { e -> 
                Log.e("WorkoutManager", "Error loading workout", e)
                emit(Workout.empty())
            }
}
```

## Testing with Dispatchers

### Test Dispatcher Setup

```kotlin
class TestDispatcherProvider(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : DispatcherProvider {
    override val main = testDispatcher
    override val io = testDispatcher  
    override val default = testDispatcher
    override val unconfined = testDispatcher
}

@Test
fun testRepositoryOperation() = runTest {
    // ✅ Use test dispatcher for deterministic testing
    val testDispatchers = TestDispatcherProvider(testScheduler.testDispatcher)
    val repository = RecipeRepository(apiService, recipeDao, testDispatchers)
    
    val result = repository.fetchRecipes()
    
    assertTrue(result.isSuccess)
}
```

### ViewModel Testing

```kotlin
@Test
fun testViewModelLoading() = runTest {
    val testDispatchers = TestDispatcherProvider(testScheduler.testDispatcher)
    val viewModel = RecipeViewModel(repository, testDispatchers)
    
    viewModel.loadRecipes()
    
    // ✅ Advance time for coroutines to complete
    testScheduler.advanceUntilIdle()
    
    assertFalse(viewModel.uiState.value.isLoading)
}
```

## Common Anti-Patterns

### ❌ DON'T: Direct Dispatcher Usage

```kotlin
// ❌ Hard to test, not injectable
class BadRepository {
    suspend fun loadData() = withContext(Dispatchers.IO) {
        apiService.getData()
    }
}
```

### ❌ DON'T: runBlocking in UI Code

```kotlin
// ❌ Blocks main thread, causes ANR
@Composable
fun BadScreen() {
    val data = runBlocking {
        repository.loadData()
    }
    // UI code
}

// ✅ DO: Use LaunchedEffect or remember with coroutine scope
@Composable
fun GoodScreen() {
    var data by remember { mutableStateOf<Data?>(null) }
    
    LaunchedEffect(Unit) {
        data = repository.loadData()
    }
    // UI code
}
```

### ❌ DON'T: Nested withContext

```kotlin
// ❌ Unnecessary dispatcher switching
suspend fun badFunction() = withContext(Dispatchers.IO) {
    val data1 = withContext(Dispatchers.IO) { // Redundant
        apiService.getData1()
    }
    val data2 = withContext(Dispatchers.IO) { // Redundant
        apiService.getData2()
    }
    processData(data1, data2)
}

// ✅ DO: Single context switch
suspend fun goodFunction() = withContext(Dispatchers.IO) {
    val data1 = apiService.getData1()
    val data2 = apiService.getData2()
    processData(data1, data2)
}
```

### ❌ DON'T: Blocking Operations on Main Thread

```kotlin
// ❌ Network on main thread
@Composable
fun BadComposable() {
    val data = remember {
        // This will crash with NetworkOnMainThreadException
        apiService.getDataBlocking()
    }
}

// ✅ DO: Async operations with proper scope
@Composable
fun GoodComposable() {
    var data by remember { mutableStateOf<Data?>(null) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        data = withContext(Dispatchers.IO) {
            apiService.getData()
        }
    }
}
```

### ❌ DON'T: Ignoring Cancellation

```kotlin
// ❌ Doesn't respect cancellation
class BadManager {
    fun startLongOperation() {
        GlobalScope.launch {
            // Long operation that can't be cancelled
            while (true) {
                // Work that ignores cancellation
            }
        }
    }
}

// ✅ DO: Respect cancellation
class GoodManager @Inject constructor(
    private val dispatchers: DispatcherProvider
) {
    fun startLongOperation(scope: CoroutineScope) {
        scope.launch(dispatchers.default) {
            while (isActive) { // Checks for cancellation
                // Cancellable work
                yield() // Cooperation point
            }
        }
    }
}
```

## Dispatcher Injection Setup

### Hilt Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    
    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
```

### Usage in Classes

```kotlin
// ✅ Repository with injected dispatchers
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val userApiService: UserApiService,
    private val dispatchers: DispatcherProvider
) {
    suspend fun refreshUser(userId: String) = withContext(dispatchers.io) {
        val user = userApiService.getUser(userId)
        userDao.insertUser(user)
    }
}

// ✅ ViewModel with injected dispatchers
@HiltViewModel  
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {
    // Implementation
}

// ✅ Service with injected dispatchers
@Singleton
class SyncService @Inject constructor(
    private val syncRepository: SyncRepository,
    private val dispatchers: DispatcherProvider
) {
    suspend fun performSync() = withContext(dispatchers.io) {
        syncRepository.syncAllData()
    }
}
```

## Best Practices Checklist

### Repository Layer ✅
- [ ] Inject `DispatcherProvider` instead of using `Dispatchers` directly
- [ ] Use `withContext(dispatchers.io)` for network/database operations
- [ ] Use `flowOn(dispatchers.io)` for Flow-based operations
- [ ] Handle exceptions within the context switch
- [ ] Return `Result<T>` for operations that can fail

### ViewModel Layer ✅
- [ ] Use `viewModelScope.launch` for UI-related coroutines
- [ ] Switch to appropriate dispatcher for background work
- [ ] Update UI state on Main dispatcher (implicit with `viewModelScope`)
- [ ] Handle loading/error states properly
- [ ] Cancel ongoing operations in `onCleared()`

### Compose UI Layer ✅
- [ ] Use `LaunchedEffect` for side effects
- [ ] Use `rememberCoroutineScope()` for event-driven operations
- [ ] Never use `runBlocking` in composables
- [ ] Collect flows with `collectAsState()`
- [ ] Handle loading states in UI

### Testing ✅
- [ ] Use `TestDispatcherProvider` for unit tests
- [ ] Use `runTest` for coroutine testing
- [ ] Use `testScheduler.advanceUntilIdle()` to complete coroutines
- [ ] Test both success and failure scenarios
- [ ] Verify dispatcher usage with proper mocking

## Debugging Threading Issues

### Common Errors and Solutions

```kotlin
// Error: NetworkOnMainThreadException
// Solution: Use IO dispatcher for network calls

// Error: IllegalStateException: Cannot access database on the main thread
// Solution: Use IO dispatcher for database operations

// Error: Test fails with timeout
// Solution: Use TestDispatcher and advance scheduler

// Error: State not updating in UI
// Solution: Ensure state updates happen on Main dispatcher
```

### Logging Dispatcher Usage

```kotlin
class LoggingDispatcherProvider @Inject constructor(
    private val delegate: DispatcherProvider
) : DispatcherProvider {
    override val main get() = delegate.main.apply { 
        Log.d("Dispatcher", "Using Main dispatcher") 
    }
    override val io get() = delegate.io.apply { 
        Log.d("Dispatcher", "Using IO dispatcher") 
    }
    override val default get() = delegate.default.apply { 
        Log.d("Dispatcher", "Using Default dispatcher") 
    }
    override val unconfined get() = delegate.unconfined
}
```

---

**Remember**: Thread safety is critical for app stability. Always follow these patterns to ensure your code is testable, maintainable, and performs well across all Android devices.