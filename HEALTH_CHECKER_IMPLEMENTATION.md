# API Health Checker Implementation

This document describes the implemented API Health Checker system for monitoring external service providers in the FitApp.

## Overview

The health checker system provides:
- **Periodic Health Monitoring**: Automated checks every hour via WorkManager
- **Caching**: Persistent storage of health status in Room database
- **UI Surface**: Settings screen displaying provider health with manual retry
- **Structured Logging**: Comprehensive logging with telemetry hooks
- **Exponential Backoff**: Resilient retry mechanism for failed checks

## Architecture

### Core Components

#### 1. Health Status Model (`HealthStatus.kt`)
```kotlin
data class HealthStatus(
    val isHealthy: Boolean,
    val provider: String,
    val responseTimeMs: Long?,
    val errorMessage: String?,
    val lastChecked: Long,
    val additionalData: Map<String, Any>
) {
    enum class Status { OK, DEGRADED, DOWN }
    
    val status: Status
        get() = when {
            !isHealthy -> Status.DOWN
            responseTimeMs != null && responseTimeMs > 5000 -> Status.DEGRADED
            else -> Status.OK
        }
}
```

**Features:**
- Three-tier status: OK (healthy), DEGRADED (slow), DOWN (failed)
- Response time tracking with degradation threshold (5s)
- Structured error reporting
- Backward compatibility with legacy status string

#### 2. Health Checkable Interface (`HealthCheckable.kt`)
```kotlin
interface HealthCheckable {
    val providerName: String
    suspend fun checkHealth(): HealthStatus
    fun healthStatusFlow(): Flow<HealthStatus>
}
```

**Implementations:**
- `GeminiHealthChecker`: AI provider health monitoring
- Extensible for other providers (Perplexity, Health Connect, etc.)

#### 3. Health Registry (`ApiHealthRegistry.kt`)
```kotlin
@Singleton
class ApiHealthRegistry @Inject constructor(
    private val geminiHealthChecker: GeminiHealthChecker
) {
    suspend fun checkAllHealth(): Map<String, HealthStatus>
    fun healthStatusFlow(): Flow<Map<String, HealthStatus>>
}
```

**Features:**
- Centralized management of all health checkers
- Aggregated health status monitoring
- Dependency injection ready

#### 4. Persistent Storage (`HealthStatusEntity.kt`, `HealthStatusDao.kt`)
```kotlin
@Entity(tableName = "health_status")
data class HealthStatusEntity(
    @PrimaryKey val provider: String,
    val isHealthy: Boolean,
    val responseTimeMs: Long?,
    val errorMessage: String?,
    val lastChecked: Long,
    val additionalData: String?
)
```

**Features:**
- Room database integration with migration support
- Indexed for performance (provider, lastChecked, isHealthy)
- JSON serialization support for additional data

#### 5. Repository Layer (`HealthStatusRepository.kt`)
```kotlin
@Singleton
class HealthStatusRepository @Inject constructor(
    private val database: AppDatabase,
    private val dispatchers: DispatcherProvider
) {
    suspend fun saveHealthStatus(status: HealthStatus)
    fun getAllHealthStatusesFlow(): Flow<List<HealthStatus>>
    suspend fun getHealthSummary(): HealthSummary
}
```

**Features:**
- Clean architecture separation
- Reactive data flows
- Aggregated health summaries

#### 6. Background Worker (`HealthCheckWorker.kt`)
```kotlin
@HiltWorker
class HealthCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiHealthRegistry: ApiHealthRegistry,
    private val healthStatusRepository: HealthStatusRepository
) : CoroutineWorker(context, workerParams)
```

**Features:**
- Hilt dependency injection
- Periodic execution (hourly)
- Network constraints and battery optimization
- Exponential backoff for failures
- Structured logging

### Scheduling Configuration

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .build()

val periodicRequest = PeriodicWorkRequestBuilder<HealthCheckWorker>(1, TimeUnit.HOURS)
    .setConstraints(constraints)
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
    .build()
```

## User Interface

### Health Status Screen (`HealthStatusScreen.kt`)
- **Overall Summary Card**: Shows aggregated health with OK/DEGRADED/DOWN status
- **Provider Details**: Individual provider status with response times and errors
- **Action Controls**: Manual refresh and clear data functionality
- **Error Handling**: User-friendly error display

### Key UI Features
- Real-time status updates via reactive flows
- Color-coded status indicators (Green/Orange/Red)
- Response time display
- Last updated timestamps
- Manual retry capability

## Integration Points

### Dependency Injection Setup
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object HealthCheckerModule {
    
    @Provides
    @Singleton
    fun provideApiHealthRegistry(
        geminiHealthChecker: GeminiHealthChecker
    ): ApiHealthRegistry = ApiHealthRegistry(geminiHealthChecker)
}
```

### Database Migration
```sql
CREATE TABLE IF NOT EXISTS `health_status` (
    `provider` TEXT PRIMARY KEY NOT NULL,
    `isHealthy` INTEGER NOT NULL,
    `responseTimeMs` INTEGER,
    `errorMessage` TEXT,
    `lastChecked` INTEGER NOT NULL,
    `additionalData` TEXT
);
```

### Structured Logging Integration
```kotlin
StructuredLogger.info(
    StructuredLogger.LogCategory.HEALTH_CHECK,
    "HealthCheckWorker",
    "Health check completed",
    mapOf(
        "healthy_count" to healthyCount,
        "total_count" to totalCount,
        "health_percentage" to healthPercentage
    )
)
```

## Testing Strategy

### Unit Tests
- **HealthStatusLogicTest**: Core status logic validation
- **HealthStatusRepositoryTest**: Repository layer testing with mocks
- **HealthCheckerTest**: Individual checker implementations

### Key Test Cases
- Status enum classification (OK/DEGRADED/DOWN)
- Entity conversion accuracy
- Repository data flow validation
- Mock health checker contract verification
- Summary calculation correctness

## Implementation Status

✅ **Completed:**
- Core health status model with three-tier classification
- Database entities and migration (v17→v18)
- Repository pattern with reactive flows
- Health checker interface and registry
- Structured logging integration
- Comprehensive unit tests
- UI components and ViewModel foundation

⚠️ **Pending (due to build environment issues):**
- WorkManager integration compilation
- UI screen integration with navigation
- End-to-end integration testing
- Production deployment validation

## Usage Example

```kotlin
// Manual health check trigger
class SettingsViewModel @Inject constructor(
    private val healthStatusRepository: HealthStatusRepository
) {
    fun getHealthSummary() = healthStatusRepository.getHealthSummaryFlow()
    
    suspend fun triggerHealthCheck() {
        // Trigger immediate worker execution
        HealthCheckWorker.triggerImmediateHealthCheck(context)
    }
}
```

## Future Enhancements

1. **Additional Providers**: Perplexity AI, Health Connect, external APIs
2. **Advanced Monitoring**: SLA tracking, performance trends
3. **Alerting**: Push notifications for critical failures
4. **Dashboard**: Developer/admin dashboard with historical data
5. **Circuit Breaker**: Automatic failover patterns

## Performance Considerations

- **Minimal Battery Impact**: WorkManager constraints prevent abuse
- **Network Efficiency**: Lightweight health check endpoints
- **Database Optimization**: Indexed queries and efficient migrations
- **Memory Management**: Proper Flow lifecycle management
- **Threading**: Proper dispatcher usage (IO for network, Main for UI)

This implementation provides a solid foundation for API health monitoring that can be easily extended as the application grows.