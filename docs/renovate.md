# Renovate - Automated Dependency Updates

This document explains how Renovate is configured for the FitApp project and how to work with it.

## Overview

Renovate is our automated dependency update tool that replaces the previous Dependabot setup. It handles updates for:

- **Gradle dependencies** (via `libs.versions.toml` version catalog)
- **GitHub Actions** workflows
- **Android Gradle Plugin & Kotlin**
- **AndroidX libraries** (Compose, Room, Navigation, etc.)
- **Third-party libraries** (Hilt, Retrofit, OkHttp, etc.)

## Configuration

The main configuration is in [`renovate.json`](../renovate.json) at the repository root.

### Key Features

- **Smart Grouping**: Dependencies are grouped by functionality (Android Core, Compose, Testing, etc.)
- **Automerge**: Safe updates (patches, GitHub Actions, test dependencies) are automatically merged
- **Semantic Commits**: All updates follow conventional commit format
- **Rate Limiting**: Maximum 2 PRs per hour, 10 concurrent PRs to avoid spam
- **Security Alerts**: Vulnerability updates are prioritized

### Update Groups

| Group | Description | Automerge |
|-------|-------------|-----------|
| **Android Gradle Plugin & Kotlin** | Core build tools and Kotlin version | Patch only |
| **Jetpack Compose** | Compose BOM and related libraries | Patch only |
| **AndroidX Core** | Core Android libraries (lifecycle, activity, etc.) | Patch only |
| **Room Database** | Room ORM libraries | Patch only |
| **DataStore & Protobuf** | Data persistence libraries | Patch only |
| **Hilt** | Dependency injection framework | Patch only |
| **Networking** | Retrofit, OkHttp, Moshi libraries | Patch only |
| **Camera & ML** | CameraX, ML Kit, TensorFlow libraries | Patch only |
| **Wear OS** | Wear OS specific libraries | Patch only |
| **Testing** | JUnit, Mockito, Espresso, etc. | ✅ All updates |
| **GitHub Actions** | CI/CD workflow actions | ✅ All updates |
| **Media** | Image loading and media libraries | Patch only |

## Working with Renovate

### Dependency Dashboard

Renovate creates a **Dependency Dashboard** issue that provides:

- Overview of all pending updates
- Control to pause/resume specific dependencies
- Manual trigger for updates
- Status of each update group

**Access**: Look for an issue titled "Dependency Dashboard" in the Issues tab.

### Managing Updates

#### Approving Updates
Most updates require manual approval and review. Check:
1. **Changelog/Release notes** of the updated dependency
2. **CI status** - ensure all tests pass
3. **Breaking changes** - especially for major version updates

#### Pausing Dependencies
To temporarily pause updates for a specific dependency:
1. Go to the Dependency Dashboard issue
2. Check the checkbox next to the dependency you want to pause
3. Renovate will skip that dependency until unchecked

#### Emergency Stops
To completely pause Renovate:
1. Add `"enabled": false` to `renovate.json`
2. Commit and push the change

### Automerge Rules

**Automatically merged** (no review needed):
- ✅ **Patch updates** for production dependencies
- ✅ **All updates** for testing dependencies
- ✅ **All updates** for GitHub Actions

**Requires review**:
- ⚠️ **Minor/Major updates** for production dependencies
- ⚠️ **Breaking changes** in any dependency
- ⚠️ **Security vulnerabilities** (prioritized but not auto-merged)

### Troubleshooting

#### Build Failures
If a Renovate PR breaks the build:
1. Check the **build logs** in the PR
2. Look for **compatibility issues** between dependencies
3. **Close the PR** if the update is problematic
4. Add an **ignore rule** to `renovate.json` if needed

#### Version Conflicts
For Kotlin/Android Gradle Plugin compatibility issues:
1. Check Kotlin [compatibility guide](https://kotlinlang.org/docs/gradle-configure-project.html#apply-the-plugin)
2. Updates are grouped to ensure compatibility
3. Major updates require manual review

#### Too Many PRs
If Renovate creates too many PRs:
1. Adjust `prConcurrentLimit` and `prHourlyLimit` in `renovate.json`
2. Use the Dependency Dashboard to pause specific groups
3. Consider adjusting the schedule

### Schedule

- **Weekdays**: 7 AM - 6 PM (Europe/Berlin timezone)
- **Weekends**: Any time
- **Lock file maintenance**: Before 6 AM on Mondays
- **Rate limit**: Maximum 2 PRs per hour

### Security

- **Vulnerability alerts** are enabled and labeled with `security`
- **Security updates** are not auto-merged for safety
- All security-related PRs require manual review

## Migration from Dependabot

The previous Dependabot configuration has been **disabled** but kept for reference in `.github/dependabot.yml`. 

**Key differences**:
- Renovate has better **Gradle version catalog support**
- More **granular grouping** and **automerge rules**
- **Better CI integration** and **semantic commit support**
- **Dependency dashboard** for better visibility and control

## Configuration Reference

For detailed configuration options, see:
- [Renovate Documentation](https://docs.renovatebot.com/)
- [Configuration Options](https://docs.renovatebot.com/configuration-options/)
- [Package Rules](https://docs.renovatebot.com/configuration-options/#packagerules)

## Support

For issues with Renovate configuration:
1. Check the [Renovate logs](https://github.com/danielhintz87-cloud/FitApp/issues) for error messages
2. Review the [troubleshooting guide](https://docs.renovatebot.com/troubleshooting/)
3. Create an issue if the problem persists