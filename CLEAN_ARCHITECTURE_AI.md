# Clean Architecture Refactoring for AI Module

## Overview

The AI module has been refactored from a monolithic `AiCore.kt` (~543 lines) to a Clean Architecture design with proper separation of concerns, dependency injection, and provider-specific implementations.

## Architecture Layers

### 1. Domain Layer (`domain/`)
Pure business logic with no framework dependencies.

#### Entities (`domain/entities/`)
- `AiEntities.kt`: Core business objects
  - `AiProvider`: Enum for provider types (Gemini, Perplexity)
  - `TaskType`: Enum for different AI task types
  - `PlanRequest`, `RecipeRequest`, `CaloriesEstimate`: Data models
  - `AiRequest`, `AiResponse`: Request/response wrappers

#### Use Cases (`domain/usecases/`)
- `AiUseCases.kt`: Interfaces defining business operations
  - `GenerateTrainingPlanUseCase`
  - `GenerateRecipesUseCase`
  - `EstimateCaloriesUseCase`
  - `ParseShoppingListUseCase`
  - `EstimateCaloriesForManualEntryUseCase`
  - `GenerateDailyWorkoutStepsUseCase`

#### Repository Interfaces (`domain/repositories/`)
- `AiProviderRepository.kt`: Contract for AI operations

### 2. Infrastructure Layer (`infrastructure/`)
Framework-specific implementations.

#### Providers (`infrastructure/providers/`)
- `AiProvider.kt`: Interface for AI provider implementations
- `GeminiAiProvider.kt`: Google Gemini API implementation
- `PerplexityAiProvider.kt`: Perplexity API implementation

#### Repository Implementation (`infrastructure/repositories/`)
- `AiProviderRepositoryImpl.kt`: Coordinates multiple AI providers
  - Intelligent routing based on task type
  - Retry logic with exponential backoff
  - Fallback provider support
  - Logging and usage tracking

#### Logging (`infrastructure/logging/`)
- `AiLogger.kt`: Handles logging and usage tracking for AI operations

### 3. Application Layer (`application/`)
Coordinates domain and infrastructure layers.

#### Use Case Implementations (`application/usecases/`)
- `AiUseCaseImpls.kt`: Core use case implementations
- `AdditionalUseCaseImpls.kt`: Additional use case implementations

#### Dependency Injection (`application/di/`)
- `AiDiContainer.kt`: Manual dependency injection container
  - Singleton pattern
  - Lazy initialization
  - Provider registration

### 4. Interface/Presentation Layer (`ai/`)
External API and backward compatibility.

- `AppAi.kt`: Main facade (updated to delegate to Clean Architecture)
- `AppAiClean.kt`: New Clean Architecture facade
- `AiCore.kt`: Legacy implementation (preserved for compatibility)

## Key Benefits

### 1. Single Responsibility Principle
- Each provider handles only its own API
- Use cases focus on specific business operations
- Repository handles coordination logic

### 2. Dependency Inversion
- Use cases depend on abstractions (interfaces)
- Concrete implementations are injected
- Easy to mock for testing

### 3. Open/Closed Principle
- Easy to add new providers without changing existing code
- New use cases can be added without modifying existing ones

### 4. Provider-Specific Optimization
- **Gemini**: Optimized for structured content, multimodal tasks
- **Perplexity**: Optimized for quick queries, web search capabilities

### 5. Intelligent Task Routing
```kotlin
when {
    hasImage -> AiProvider.Gemini
    taskType == TRAINING_PLAN -> AiProvider.Gemini
    taskType == SHOPPING_LIST_PARSING -> AiProvider.Perplexity
    taskType == RECIPE_GENERATION -> AiProvider.Perplexity
    else -> AiProvider.Gemini
}
```

### 6. Robust Error Handling
- Exponential backoff retry logic
- Automatic fallback provider switching
- Comprehensive logging and monitoring

## Usage Examples

### Basic Usage (Unchanged Public API)
```kotlin
// Training plan generation
val result = AppAi.planWithOptimalProvider(context, PlanRequest(
    goal = "Build muscle",
    weeks = 12,
    sessionsPerWeek = 3,
    minutesPerSession = 60,
    equipment = listOf("Dumbbells", "Barbell")
))

// Recipe generation
val recipes = AppAi.recipesWithOptimalProvider(context, RecipeRequest(
    preferences = "High protein",
    diet = "Vegetarian",
    count = 5
))

// Calorie estimation
val calories = AppAi.caloriesWithOptimalProvider(context, bitmap, "Lunch meal")
```

### Advanced Usage (Direct Clean Architecture)
```kotlin
val container = AiDiContainer.getInstance(context)

// Direct use case access
val planResult = container.generateTrainingPlanUseCase.execute(planRequest)
val recipeResult = container.generateRecipesUseCase.execute(recipeRequest)

// Repository access for advanced scenarios
val repository = container.aiProviderRepository
val provider = repository.selectOptimalProvider(TaskType.TRAINING_PLAN)
```

## Migration Path

### Phase 1: ✅ Completed
- Implement Clean Architecture structure
- Provider-specific implementations
- Dependency injection container
- Backward compatibility maintained

### Phase 2: Future Enhancements
- Upgrade to Hilt for dependency injection
- Add comprehensive unit tests
- Implement caching layer
- Add metrics and monitoring

## Testing Strategy

### Unit Testing
- Mock providers for use case testing
- Test routing logic in repository
- Validate error handling scenarios

### Integration Testing
- Test provider implementations with real APIs
- Validate end-to-end scenarios
- Performance testing

## Monitoring and Logging

### Usage Tracking
- Token consumption per provider
- Request counts and success rates
- Cost estimation and budget tracking

### Error Logging
- Failed requests with context
- Provider-specific error patterns
- Performance metrics

## Configuration

### Provider Setup
```kotlin
// In ApiKeysScreen or programmatically
ApiKeys.saveGeminiKey(context, "your-gemini-api-key")
ApiKeys.savePerplexityKey(context, "your-perplexity-api-key")
```

### Monitoring Status
```kotlin
val status = AppAi.getProviderStatus(context)
println(status) // Detailed provider and usage information
```

## File Structure
```
app/src/main/java/com/example/fitapp/
├── domain/
│   ├── entities/AiEntities.kt
│   ├── usecases/AiUseCases.kt
│   └── repositories/AiProviderRepository.kt
├── infrastructure/
│   ├── providers/
│   │   ├── AiProvider.kt
│   │   ├── GeminiAiProvider.kt
│   │   └── PerplexityAiProvider.kt
│   ├── repositories/AiProviderRepositoryImpl.kt
│   └── logging/AiLogger.kt
├── application/
│   ├── usecases/
│   │   ├── AiUseCaseImpls.kt
│   │   └── AdditionalUseCaseImpls.kt
│   └── di/AiDiContainer.kt
└── ai/
    ├── AppAi.kt (updated facade)
    ├── AppAiClean.kt (Clean Architecture facade)
    ├── AiCore.kt (legacy, preserved)
    └── UsageTracker.kt (unchanged)
```

This refactoring provides a solid foundation for future enhancements while maintaining complete backward compatibility with existing code.