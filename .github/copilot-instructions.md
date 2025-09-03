# FitApp Android Development Guide

Always follow these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the information provided here.

## Project Overview

FitApp is an Android fitness tracking application built with:
- **Language**: Kotlin with Jetpack Compose UI
- **Architecture**: MVVM with Repository pattern, Clean Architecture principles
- **Database**: Room with SQLite, schema migrations
- **Design**: Material 3 Design System
- **AI Integration**: Gemini + Perplexity APIs for personal training features
- **Target**: Android SDK 34, minimum SDK 24, Java 17
- **Build System**: Gradle 8.14.3 with Kotlin DSL and version catalogs

## Environment Setup Requirements

### Prerequisites
- **Java 17**: Required for compilation and runtime
- **Android SDK**: Level 34 (can work without physical SDK for builds)
- **Gradle**: Uses wrapper (gradlew) - version 8.14.3
- **Memory**: At least 2GB RAM for Gradle daemon (configured in gradle.properties)

### Initial Setup Commands
Run these commands in order for a fresh clone:

```bash
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

## Build Commands

### Clean Build Process
**NEVER CANCEL BUILDS** - They may take 4-7 minutes. Always set timeouts to 10+ minutes minimum.

```bash
# Clean project
./gradlew clean
# Takes: ~1-2 minutes

# Build debug APK
./gradlew assembleDebug
# Takes: ~4-5 minutes, NEVER CANCEL - set timeout to 10+ minutes
# WARNING: First build after clean downloads dependencies and can take longer

# Build release APK  
./gradlew assembleRelease
# Takes: ~5-7 minutes, NEVER CANCEL - set timeout to 15+ minutes
```

### Verification Commands

```bash
# Run lint analysis
./gradlew lintDebug
# Takes: ~2-3 minutes, NEVER CANCEL - set timeout to 5+ minutes

# Check project (includes lint)
./gradlew check
# Takes: ~3-5 minutes, NEVER CANCEL - set timeout to 10+ minutes
# WARNING: Unit tests currently have compilation errors - use with --continue flag

# Build verification without tests
./gradlew assembleDebug lintDebug
# Takes: ~5-7 minutes total, NEVER CANCEL - set timeout to 15+ minutes
```

## Testing

### Unit Tests Status
**IMPORTANT**: Unit tests currently have compilation errors and will not run successfully. The main application code builds and runs correctly.

```bash
# Attempt unit tests (will fail with compilation errors)
./gradlew testDebugUnitTest --continue
# Status: FAILS - compilation errors in test code
# Use --continue flag to see all errors

# Check available test tasks
./gradlew tasks --group=verification
```

### Test Structure
- Unit tests: `app/src/test/java/`
- Instrumented tests: `app/src/androidTest/java/`
- Test categories: AI services, nutrition management, workout management, cooking mode

## Key Project Structure

### Source Code
```
app/src/main/java/com/example/fitapp/
├── ai/                 # AI integration (Gemini, Perplexity)
├── data/              # Repository pattern, Room database
│   ├── db/           # Database entities, DAOs, migrations  
│   └── prefs/        # SharedPreferences wrappers
├── services/          # Business logic managers
├── ui/               # Compose screens and components
└── utils/            # Utility classes
```

### Configuration Files
- `gradle.properties`: Build cache enabled, parallel execution, 2GB heap
- `local.properties`: API keys, SDK path (gitignored)
- `app/schemas/`: Room database schema files for migrations
- `.github/workflows/`: CI/CD with Android builds and tests

### Dependencies
Key dependencies (defined in `gradle/libs.versions.toml`):
- Compose BOM 2024.10.01
- Kotlin 2.0.20
- Room 2.6.1  
- Android Gradle Plugin 8.12.1
- CameraX, ML Kit, Retrofit, OkHttp

## Development Workflow

### Making Changes
Always validate changes with this sequence:

```bash
# 1. Clean build to ensure fresh state
./gradlew clean

# 2. Build main application
./gradlew assembleDebug
# NEVER CANCEL - takes 4-5 minutes

# 3. Run lint analysis
./gradlew lintDebug  
# NEVER CANCEL - takes 2-3 minutes

# 4. Check lint report
# Report located at: app/build/reports/lint-results-debug.html
```

### Performance Optimization
The project uses Gradle configuration cache and build cache:
- **Configuration cache**: Enabled - speeds up builds significantly
- **Build cache**: Enabled - reuses outputs between builds
- **Parallel execution**: Enabled for faster dependency resolution

**Cache-related warnings are normal** and do not indicate build problems.

## CI/CD Integration

### GitHub Actions
The project has automated workflows:
- `android-room-ci.yml`: Build + Unit Tests + Schema Guard (7+ minutes)
- `android_tests.yml`: Unit and instrumented tests
- **Expected CI timing**: 7-15 minutes total

### Schema Management
Room database schemas are tracked in `app/schemas/`. CI fails if schema changes are not committed.

## Troubleshooting

### Common Issues
1. **Android SDK warnings**: Build works without physical SDK installation
2. **Unit test failures**: Known issue - test code has compilation errors
3. **Long build times**: Normal - 4-7 minutes is expected
4. **Configuration cache warnings**: Normal and safe to ignore

### Build Cache Issues
```bash
# If builds seem corrupted, clear caches
./gradlew --stop
./gradlew clean
# Then rebuild normally
```

### Memory Issues
```bash
# If out of memory errors occur
export GRADLE_OPTS="-Xmx4g"
./gradlew clean assembleDebug
```

## API Keys Setup

For full functionality, add to `local.properties`:
```properties
GEMINI_API_KEY=your_gemini_key_here
PERPLEXITY_API_KEY=your_perplexity_key_here
```

Without API keys, the app builds successfully but AI features will not function.

## Validation Scenarios

After making changes, always test:

1. **Build Validation**: Clean build succeeds without errors
2. **Lint Validation**: No new critical lint issues introduced  
3. **Schema Validation**: If database changes made, ensure schemas are committed

**Manual Testing** (requires physical device or emulator):
- App installs and launches successfully
- Navigation between main screens works
- Core features accessible (workout tracking, nutrition, etc.)

## Critical Timeouts

**NEVER CANCEL these commands** - always wait for completion:
- `./gradlew clean`: 1-2 minutes
- `./gradlew assembleDebug`: 4-5 minutes (**set 10+ minute timeout**)
- `./gradlew assembleRelease`: 5-7 minutes (**set 15+ minute timeout**)
- `./gradlew lintDebug`: 2-3 minutes (**set 5+ minute timeout**)
- `./gradlew check`: 3-5 minutes (**set 10+ minute timeout**)

**Build times are normal** - this is a complex Android project with extensive dependencies and modern tooling that require significant processing time.