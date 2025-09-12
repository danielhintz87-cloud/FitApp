# CI Workflow Restructuring Summary

This PR restructures the GitHub Actions workflows as requested, splitting CI into three separate, focused workflows with improved caching and parallelization.

## New Workflows

### 1. `ci-unit.yml` - Unit Tests
- **Purpose**: Runs Kotlin/Java unit tests and Robolectric tests
- **Environment**: ubuntu-latest (JVM-based, no Android emulator needed)
- **Features**:
  - Gradle build caching with `gradle/gradle-build-action@v3`
  - Runs `testDebugUnitTest` for pure JVM testing
  - Uploads test reports as artifacts
  - 30-minute timeout for fast feedback

### 2. `ci-instrumentation.yml` - Android Emulator Tests  
- **Purpose**: Runs Android instrumentation tests requiring an emulator
- **Environment**: ubuntu-latest with Android emulator (API 30)
- **Features**:
  - AVD caching to speed up subsequent runs
  - Uses `reactivecircus/android-emulator-runner@v2`
  - Runs `connectedDebugAndroidTest` for UI/integration tests
  - Collects benchmark artifacts if available
  - 45-minute timeout for complex test suites

### 3. `ci-lint.yml` - Static Analysis & Quality Checks
- **Purpose**: Runs all static analysis and quality gates
- **Checks Include**:
  - `ktlint` code style verification
  - `detekt` static analysis  
  - Android `lintDebug` platform checks
  - Custom `checkNavGraphReachability` task
  - ML model verification
  - Unit test coverage with `jacocoTestReport`
  - Quality gate with coverage threshold (35%)
- **Features**:
  - All checks now **block merges** (removed `|| true`)
  - Uploads comprehensive lint/analysis reports
  - 25-minute timeout for quick feedback

## Key Improvements

### Caching Strategies
- **Gradle**: Leverages `gradle/gradle-build-action@v3` for dependency and build caching
- **AVD**: Caches Android Virtual Device images between runs to reduce emulator setup time
- **Proper cache keys**: Uses appropriate cache keys for AVD to ensure cache hits

### Parallelization Benefits  
- **Before**: Monolithic workflows ran all tests sequentially
- **After**: Three parallel jobs can run simultaneously:
  - Unit tests (fastest, ~5-10 min)
  - Lint checks (medium, ~10-15 min)  
  - Instrumentation tests (slowest, ~20-30 min)
- **Result**: Significant reduction in overall CI time through parallelization

### Quality Gates
- **Blocking**: All lint/quality checks now fail the build when issues are found
- **Comprehensive**: Covers code style, static analysis, Android-specific checks, and navigation graph integrity
- **Reporting**: Detailed reports uploaded as artifacts for debugging

## Static Analysis Tools Added

### detekt Configuration
- Added `detekt` plugin to `app/build.gradle.kts` 
- Updated `detekt.yml` with modern rule set
- Configured to analyze main source code with appropriate thresholds

### ktlint Configuration  
- Added `ktlint` plugin to `app/build.gradle.kts`
- Version 12.1.1 with Android-specific rules
- Excludes generated code, focuses on Kotlin sources

### NavGraph Reachability Check
- Custom Gradle task `checkNavGraphReachability`
- Validates that all navigation destinations are reachable
- Prevents orphaned fragments/activities in navigation graphs

## Migration Strategy

The old workflows (`android_tests.yml`, `android_instrumented.yml`, `code-quality.yml`) are temporarily disabled and marked as deprecated. They will be removed in a future PR once the new workflows are validated.

## Testing

All new workflows include:
- Proper error handling and timeouts
- Artifact uploads for debugging
- Clear success/failure indicators
- Comprehensive logging for troubleshooting

## Acceptance Criteria ✅

- [x] **Separate workflows**: Unit, instrumentation, and lint workflows created
- [x] **Caching**: Gradle and AVD caching implemented for speed
- [x] **Parallelization**: Jobs can run in parallel for faster overall CI time  
- [x] **Quality gates**: Lint and static analysis checks block merges
- [x] **NavGraph checks**: Custom task validates navigation graph integrity
- [x] **Proper naming**: Files use requested naming convention (ci-*.yml)
- [x] **Ubuntu-latest**: Unit tests use ubuntu-latest as requested
- [x] **ReactiveCircus**: Instrumentation tests use the specified emulator runner