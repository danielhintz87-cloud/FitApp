# CI/CD Pipeline Documentation

## 🚀 Overview

The FitApp repository implements a comprehensive CI/CD pipeline using GitHub Actions to ensure code quality, automated testing, and streamlined deployment processes.

## 📋 Pipeline Components

### 1. Main CI Pipeline (`android-room-ci.yml`)

**Triggers:**
- Push to `main` or `master` branches
- Pull requests to `main` or `master` branches

**Jobs:**
- **Build & Unit Tests & Schema Guard**: Builds debug APK, runs unit tests, validates Room schema changes
- **Instrumented Tests**: Runs UI tests on Android emulator (API 34)

**Duration:** ~7-15 minutes

### 2. Comprehensive Test Suite (`android_tests.yml`)

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches

**Jobs:**
- **Unit Tests**: Executes all unit tests with coverage reporting
- **Instrumented Tests**: UI tests on Android emulator (API 29)
- **Build Check**: Validates build integrity and code style

**Features:**
- Test coverage reports (Jacoco)
- Artifact upload for test results
- Build caching for performance

### 3. Code Quality & Security (`code-quality.yml`)

**Triggers:**
- Push to `main`, `master`, or `develop` branches
- Pull requests
- Weekly scheduled security scans (Sundays 2 AM UTC)

**Jobs:**
- **Lint Analysis**: Android Lint + Detekt static analysis
- **Dependency Analysis**: Vulnerability scanning with OWASP Dependency-Check
- **Security Analysis**: Trivy filesystem scanning
- **CodeQL Analysis**: GitHub's semantic code analysis
- **Performance Analysis**: Build metrics and APK size analysis

### 4. Release Pipeline (`release.yml`)

**Triggers:**
- GitHub releases (published)
- Git tags matching `v*.*.*`
- Manual workflow dispatch

**Jobs:**
- **Build Release**: Creates signed APK and AAB files
- **Quality Checks**: Release-specific testing and validation
- **Security Scan**: Final security validation
- **Deployment**: Optional Google Play Store upload

**Features:**
- Automatic version extraction from tags
- Signed release artifacts
- GitHub release creation with changelogs
- Artifact retention (90 days)

## 🔧 Configuration

### Required Secrets

For the full pipeline to work, configure these GitHub repository secrets:

#### Android Signing
```
ANDROID_SIGNING_KEY          # Base64 encoded keystore file
ANDROID_KEY_ALIAS           # Keystore alias
ANDROID_KEYSTORE_PASSWORD   # Keystore password
ANDROID_KEY_PASSWORD        # Key password
```

#### API Keys
```
GEMINI_API_KEY              # Gemini AI API key
PERPLEXITY_API_KEY          # Perplexity AI API key
```

#### Google Play (Optional)
```
GOOGLE_PLAY_SERVICE_ACCOUNT # Service account JSON for Play Store uploads
```

### Local Setup

1. **Create local.properties:**
   ```bash
   cp local.properties.sample local.properties
   ```

2. **Add API keys** (optional for builds):
   ```properties
   GEMINI_API_KEY=your_key_here
   PERPLEXITY_API_KEY=your_key_here
   ```

## 📊 Quality Gates

### Build Requirements
- ✅ All unit tests must pass
- ✅ Code must compile without errors
- ✅ Lint analysis must not introduce critical issues
- ✅ Room schema changes must be committed

### Security Requirements
- ✅ No high-severity vulnerabilities in dependencies
- ✅ CodeQL analysis must pass
- ✅ Trivy security scan must pass

### Performance Requirements
- ✅ Build time < 15 minutes
- ✅ APK size monitoring and reporting

## 🚀 Deployment Process

### Development Workflow
1. **Feature Development**: Create feature branch from `main`
2. **Pull Request**: Open PR to trigger CI pipeline
3. **Code Review**: Review + automated checks
4. **Merge**: Merge to `main` triggers full CI suite

### Release Workflow
1. **Version Tag**: Create git tag following semver (`v1.0.0`)
2. **Automatic Build**: Release pipeline creates signed artifacts
3. **GitHub Release**: Automatic release with changelogs
4. **Distribution**: Artifacts available for download/deployment

### Hotfix Workflow
1. **Hotfix Branch**: Create from `main` for urgent fixes
2. **Fast Track**: Critical fixes can bypass some checks
3. **Emergency Release**: Manual workflow dispatch for immediate releases

## 📈 Monitoring & Reporting

### Build Status
- GitHub repository shows build badges
- Failed builds trigger notifications
- Detailed logs available in Actions tab

### Quality Reports
- **Lint Results**: HTML reports with issue breakdown
- **Test Coverage**: Jacoco coverage reports
- **Security Scans**: SARIF reports integrated with GitHub Security tab
- **Performance**: Build metrics and APK size trends

### Artifacts
- **APK Files**: Debug and release builds (30-90 day retention)
- **Test Reports**: JUnit XML and HTML reports
- **Coverage Reports**: Jacoco coverage analysis
- **Security Reports**: Vulnerability scan results

## 🛠️ Maintenance

### Regular Tasks
- **Weekly**: Review security scan results
- **Monthly**: Update dependencies and Gradle version
- **Quarterly**: Review and optimize pipeline performance

### Pipeline Updates
- **Gradle Updates**: Modify `gradle/wrapper/gradle-wrapper.properties`
- **Android SDK**: Update `compileSdk` in `build.gradle.kts`
- **Dependencies**: Use `gradle/libs.versions.toml` for version management

### Troubleshooting

#### Common Issues
1. **Build Timeouts**: Increase timeout in workflow files
2. **Test Failures**: Check `testDebugUnitTest --continue` for details
3. **Schema Failures**: Commit updated schema files from `app/schemas/`
4. **Signing Issues**: Verify all signing secrets are correctly configured

#### Debug Commands
```bash
# Local build verification
./gradlew clean assembleDebug --stacktrace

# Test specific module
./gradlew testDebugUnitTest --tests "*.YourTestClass"

# Generate dependency report
./gradlew dependencies --configuration debugCompileClasspath
```

## 🔗 External Integrations

### GitHub Features Used
- **Actions**: All CI/CD workflows
- **Security**: CodeQL, Dependabot, Secret scanning
- **Releases**: Automated release creation
- **Artifacts**: Build output storage

### Third-Party Tools
- **Trivy**: Container and filesystem vulnerability scanning
- **OWASP Dependency-Check**: Dependency vulnerability analysis
- **Jacoco**: Code coverage reporting
- **Detekt**: Kotlin static analysis

## 📚 Best Practices

### Code Quality
- **Consistent Formatting**: Use `ktlint` for Kotlin code style
- **Static Analysis**: Address Detekt and Lint warnings
- **Test Coverage**: Maintain >70% unit test coverage
- **Documentation**: Update README and docs with changes

### Security
- **Dependency Updates**: Regular security patch updates
- **Secret Management**: Use GitHub secrets, never commit keys
- **Vulnerability Scanning**: Address high-severity findings
- **Code Review**: Require approvals for sensitive changes

### Performance
- **Build Optimization**: Use Gradle build cache and configuration cache
- **Parallel Execution**: Enable parallel Gradle execution
- **Incremental Builds**: Minimize unnecessary rebuilds
- **Resource Optimization**: Monitor APK size and build times

---

This pipeline ensures reliable, secure, and high-quality releases while maintaining development velocity through automation and comprehensive testing.