#!/bin/bash

# FitApp CI/CD Setup Script
# This script helps set up the development environment and CI/CD pipeline

set -e

echo "🚀 FitApp CI/CD Setup Script"
echo "============================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_status() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if we're in the right directory
if [ ! -f "gradlew" ] || [ ! -f "settings.gradle.kts" ]; then
    print_error "This script must be run from the FitApp root directory"
    exit 1
fi

print_status "Checking prerequisites..."

# Check Java version
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 17 ]; then
        print_success "Java $JAVA_VERSION found"
    else
        print_error "Java 17 or higher required. Found Java $JAVA_VERSION"
        exit 1
    fi
else
    print_error "Java not found. Please install Java 17 or higher"
    exit 1
fi

# Check Android SDK (optional)
if [ -n "$ANDROID_HOME" ]; then
    print_success "Android SDK found at $ANDROID_HOME"
else
    print_warning "ANDROID_HOME not set. This is optional for builds but recommended for development"
fi

# Check Git
if command -v git &> /dev/null; then
    print_success "Git found"
else
    print_error "Git not found. Please install Git"
    exit 1
fi

print_status "Setting up local development environment..."

# Create local.properties if it doesn't exist
if [ ! -f "local.properties" ]; then
    if [ -f "local.properties.sample" ]; then
        cp local.properties.sample local.properties
        print_success "Created local.properties from sample"
        print_warning "Please edit local.properties to add your API keys"
    else
        print_error "local.properties.sample not found"
        exit 1
    fi
else
    print_success "local.properties already exists"
fi

# Make gradlew executable
chmod +x gradlew
print_success "Made gradlew executable"

print_status "Testing Gradle setup..."

# Test Gradle
if ./gradlew --version &> /dev/null; then
    print_success "Gradle wrapper is working"
else
    print_error "Gradle wrapper test failed"
    exit 1
fi

print_status "Checking CI/CD workflow files..."

# Check workflow files
WORKFLOWS=(
    ".github/workflows/android-room-ci.yml"
    ".github/workflows/android_tests.yml"
    ".github/workflows/code-quality.yml"
    ".github/workflows/release.yml"
    ".github/workflows/update-badges.yml"
)

for workflow in "${WORKFLOWS[@]}"; do
    if [ -f "$workflow" ]; then
        print_success "Found $(basename "$workflow")"
    else
        print_error "Missing workflow: $workflow"
        exit 1
    fi
done

# Check dependabot config
if [ -f ".github/dependabot.yml" ]; then
    print_success "Dependabot configuration found"
else
    print_warning "Dependabot configuration missing"
fi

print_status "Running initial build test..."

# Test clean build (don't run full build to save time)
if ./gradlew clean --no-daemon --quiet; then
    print_success "Clean build test passed"
else
    print_error "Clean build test failed"
    exit 1
fi

print_status "Checking documentation..."

# Check documentation files
DOCS=(
    "README.md"
    "docs/CI_CD_PIPELINE.md"
)

for doc in "${DOCS[@]}"; do
    if [ -f "$doc" ]; then
        print_success "Found $(basename "$doc")"
    else
        print_warning "Missing documentation: $doc"
    fi
done

echo ""
echo "🎉 Setup Complete!"
echo "=================="
echo ""
echo -e "${GREEN}Your FitApp development environment is ready!${NC}"
echo ""
echo "📋 Next Steps:"
echo "1. Edit local.properties to add your API keys (optional):"
echo "   - GEMINI_API_KEY=your_key_here"
echo "   - PERPLEXITY_API_KEY=your_key_here"
echo ""
echo "2. Run a full build to test everything:"
echo "   ./gradlew assembleDebug"
echo ""
echo "3. Run tests:"
echo "   ./gradlew testDebugUnitTest"
echo ""
echo "4. Check the CI/CD documentation:"
echo "   docs/CI_CD_PIPELINE.md"
echo ""
echo "🔗 Useful Commands:"
echo "  ./gradlew clean              # Clean build artifacts"
echo "  ./gradlew assembleDebug      # Build debug APK"
echo "  ./gradlew lintDebug          # Run lint analysis"
echo "  ./gradlew testDebugUnitTest  # Run unit tests"
echo "  ./gradlew check              # Run all checks"
echo ""
echo "📊 CI/CD Features Available:"
echo "  ✅ Automated builds on push/PR"
echo "  ✅ Unit and instrumented testing"
echo "  ✅ Code quality and security scanning"
echo "  ✅ Automated dependency updates"
echo "  ✅ Release automation with signed APK/AAB"
echo "  ✅ Build status badges and reporting"
echo ""
echo -e "${BLUE}Happy coding! 🚀${NC}"