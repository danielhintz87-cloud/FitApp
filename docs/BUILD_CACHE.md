# Build Cache and Configuration Cache

## Overview

This project uses Gradle's **configuration cache** and **build cache** to improve build performance:

- **Configuration cache**: Speeds up builds by caching the result of the configuration phase
- **Build cache**: Reuses outputs from previous builds and builds on other machines

## Configuration Cache Report

When configuration cache is enabled, Gradle generates a `configuration-cache-report.html` file that contains:

- Cache hit/miss information
- Details about why the cache couldn't be reused (e.g., file changes)
- Diagnostic information about build inputs (system properties, environment variables)
- Any configuration cache problems detected

## Why Cache Reports Are Ignored

The configuration cache reports are **automatically ignored by Git** because:

1. **Build artifacts**: They are generated outputs, not source code
2. **Machine-specific**: Reports contain local paths and system information
3. **Noisy diffs**: They change frequently and would create meaningless commits
4. **Large files**: Reports can be verbose with detailed diagnostic information

## Generating Reports Locally

To generate and view configuration cache reports:

```bash
# Run any Gradle task to generate the report
./gradlew help --configuration-cache

# View the report (location varies by build)
find build/reports/configuration-cache -name "*.html" -exec open {} \;
```

## Build Cache Settings

Our gradle.properties enables:

- `org.gradle.configuration-cache=true` - Enable configuration cache
- `org.gradle.configuration-cache.problems=warn` - Show cache problems as warnings
- `org.gradle.build-cache=true` - Enable build cache
- `org.gradle.parallel=true` - Enable parallel execution

## Troubleshooting

If you see configuration cache problems:

1. Check the generated report for details
2. Most system properties and environment variables are tracked automatically
3. Legitimate changes (like updating dependencies) will cause cache misses
4. Only fix actual problems, not normal cache behavior

## Performance Tips

- Keep configuration cache enabled for faster builds
- Use `--configuration-cache` flag to force cache usage
- Run `./gradlew --stop` if you encounter daemon issues
- The cache warms up over time for better performance