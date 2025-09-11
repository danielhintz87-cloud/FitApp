# Contributing to FitApp

## Development Setup

1. **Prerequisites**
   - Android Studio Arctic Fox or later
   - JDK 17+
   - Android SDK 28+ (Android 9.0+)

2. **Initial Setup**
   ```bash
   git clone https://github.com/danielhintz87-cloud/FitApp.git
   cd FitApp
   ./setup-dev.sh  # Run development setup script
   ```

3. **API Keys Configuration**
   - Copy `local.properties.sample` to `local.properties`
   - Add your API keys for Gemini and Perplexity (see API Keys screen in app)

## Branch Convention

- **Feature branches**: `feat/description` (e.g., `feat/health-checker`)
- **Bug fixes**: `fix/description` (e.g., `fix/migration-crash`)
- **Chores**: `chore/description` (e.g., `chore/update-dependencies`)

## Testing

### Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Instrumentation Tests
```bash
./gradlew connectedDebugAndroidTest
```

### Migration Tests
```bash
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.fitapp.core.migration.MigrationChainTest
```

## Code Style

- Follow Android Kotlin style guide
- Use dependency injection (Hilt) for all dependencies
- Wrap network calls in `withContext(dispatchers.io)`
- Always provide meaningful test coverage for new features

## Database Migrations

**IMPORTANT**: When adding database changes:

1. **Never modify existing migrations** - they are immutable once merged
2. **Always create additive migrations** - use `ALTER TABLE ADD COLUMN` instead of destructive changes
3. **Test migrations thoroughly** - add tests in `core/migration/` package
4. **Update schema version** in `AppDatabase.kt`
5. **Export schema** - ensure `exportSchema = true` and commit schema files

## AI Provider Guidelines

- **Use DispatcherProvider** - never use `Dispatchers.IO` directly
- **Handle errors gracefully** - provide user-friendly error messages
- **Implement health checks** - extend `HealthCheckable` for new providers
- **Cost optimization** - choose appropriate models for each task type

## Architecture

- **Clean Architecture** - Domain/Data/Presentation layers
- **MVVM Pattern** - ViewModels for UI state management
- **Repository Pattern** - Single source of truth for data
- **Dependency Injection** - Hilt for all dependencies

## Pull Request Process

1. Create feature branch from `main`
2. Implement changes with appropriate tests
3. Ensure all tests pass locally
4. Update documentation if needed
5. Create pull request with clear description
6. Address review feedback
7. Squash commits before merge

## Performance Guidelines

- **Database queries** - use appropriate indices and pagination
- **Image loading** - use Coil with proper caching
- **Memory usage** - avoid memory leaks in ViewModels and Fragments
- **Network calls** - implement proper timeout and retry logic

## Error Handling

- **User-facing errors** - provide actionable error messages
- **Logging** - use appropriate log levels (DEBUG/INFO/WARN/ERROR)
- **Crash reporting** - ensure critical paths are protected
- **Recovery** - implement graceful degradation where possible