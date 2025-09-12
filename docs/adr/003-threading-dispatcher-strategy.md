# ADR-003: Threading and Dispatcher Strategy

**Date**: 2024-01-11  
**Status**: Accepted  
**Decision Makers**: Development Team

## Context

FitApp requires predictable threading behavior to avoid:
- NetworkOnMainThreadException crashes
- Database access violations
- Poor test reliability due to thread variations
- Performance issues from improper dispatcher usage

## Decision

We adopt **dependency-injected dispatchers** with strict usage patterns:

1. **No direct Dispatchers usage** - Always inject `DispatcherProvider`
2. **Context-specific dispatchers** - IO for network/DB, Default for CPU work, Main for UI
3. **Repository responsibility** - Repositories handle dispatcher switching, not ViewModels
4. **Testing support** - `TestDispatcherProvider` for deterministic testing

## Dispatcher Usage Rules

```kotlin
// ✅ Correct pattern
@Singleton
class RecipeRepository @Inject constructor(
    private val dispatchers: DispatcherProvider
) {
    suspend fun fetchRecipes() = withContext(dispatchers.io) {
        apiService.getRecipes()
    }
}

// ❌ Incorrect pattern  
class BadRepository {
    suspend fun fetchRecipes() = withContext(Dispatchers.IO) {
        apiService.getRecipes()
    }
}
```

## Anti-Patterns Forbidden

- `runBlocking` in UI code
- Direct `Dispatchers.*` usage
- Nested `withContext` on same dispatcher
- Network calls without IO dispatcher
- `GlobalScope.launch` for non-application-lifetime work

## Consequences

### Positive
- ✅ Testable with `TestDispatcher`
- ✅ No threading-related crashes
- ✅ Consistent performance characteristics
- ✅ Clear responsibility boundaries

### Negative
- ⚠️ Additional boilerplate for injection
- ⚠️ Learning curve for new developers
- ⚠️ More verbose than direct dispatcher usage

## Implementation

```kotlin
// Injection setup
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}

// Usage in Repository
@Singleton
class UserRepository @Inject constructor(
    private val dispatchers: DispatcherProvider
) {
    suspend fun refreshUser() = withContext(dispatchers.io) {
        // Network and database operations
    }
}

// Testing
class TestDispatcherProvider(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : DispatcherProvider {
    override val main = testDispatcher
    override val io = testDispatcher
    override val default = testDispatcher
    override val unconfined = testDispatcher
}
```

## Alternatives Considered

- **Direct Dispatchers usage**: Rejected due to testing difficulties
- **Annotation-based dispatcher injection**: Rejected as overly complex
- **Context receivers**: Rejected as experimental and unstable

## References

- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Testing Coroutines](https://developer.android.com/kotlin/coroutines/test)