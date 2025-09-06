#!/bin/bash
# Versioning utility script for FitApp
# Helps with semantic versioning and release management

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
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

# Get current version from Git tag
get_current_version() {
    git describe --tags --abbrev=0 2>/dev/null || echo "v1.8.0"
}

# Parse version parts
parse_version() {
    local version=$1
    version=${version#v}  # Remove 'v' prefix
    echo $version | tr '.' ' '
}

# Increment version based on type
increment_version() {
    local current_version=$1
    local version_type=$2
    
    read major minor patch <<< $(parse_version $current_version)
    
    case $version_type in
        "major")
            major=$((major + 1))
            minor=0
            patch=0
            ;;
        "minor")
            minor=$((minor + 1))
            patch=0
            ;;
        "patch")
            patch=$((patch + 1))
            ;;
        *)
            print_error "Invalid version type: $version_type"
            exit 1
            ;;
    esac
    
    echo "v$major.$minor.$patch"
}

# Show current version information
show_version_info() {
    print_info "Current version information:"
    echo
    
    local current_tag=$(get_current_version)
    print_info "Latest Git tag: $current_tag"
    
    # Get version from Gradle
    local gradle_version=$(./gradlew -q currentVersion | grep "Project version:" | cut -d' ' -f3)
    print_info "Gradle version: $gradle_version"
    
    # Check if working directory is clean
    if git diff-index --quiet HEAD --; then
        print_success "Working directory is clean"
    else
        print_warning "Working directory has uncommitted changes"
    fi
    
    # Show commits since last tag
    local commits_since_tag=$(git rev-list $current_tag..HEAD --count 2>/dev/null || echo "0")
    print_info "Commits since last tag: $commits_since_tag"
}

# Create a new release
create_release() {
    local version_type=$1
    
    if [ -z "$version_type" ]; then
        print_error "Please specify version type: major, minor, or patch"
        exit 1
    fi
    
    # Check if working directory is clean
    if ! git diff-index --quiet HEAD --; then
        print_error "Working directory is not clean. Please commit or stash your changes."
        exit 1
    fi
    
    local current_version=$(get_current_version)
    local new_version=$(increment_version $current_version $version_type)
    
    print_info "Creating $version_type release:"
    print_info "  Current: $current_version"
    print_info "  New:     $new_version"
    echo
    
    # Confirm with user
    read -p "Do you want to create this release? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Release cancelled"
        exit 0
    fi
    
    # Create tag
    print_info "Creating Git tag $new_version..."
    git tag -a $new_version -m "Release $new_version"
    
    print_success "Release $new_version created successfully!"
    print_info "To push the tag to remote: git push origin $new_version"
    print_info "To push all tags: git push --tags"
}

# Show usage information
show_usage() {
    echo "FitApp Versioning Utility"
    echo
    echo "Usage: $0 <command> [options]"
    echo
    echo "Commands:"
    echo "  info                     Show current version information"
    echo "  release <type>          Create new release (type: major|minor|patch)"
    echo "  help                    Show this help message"
    echo
    echo "Examples:"
    echo "  $0 info                 # Show current version info"
    echo "  $0 release patch        # Create patch release (1.8.0 → 1.8.1)"
    echo "  $0 release minor        # Create minor release (1.8.0 → 1.9.0)"
    echo "  $0 release major        # Create major release (1.8.0 → 2.0.0)"
}

# Main script logic
case "${1:-help}" in
    "info")
        show_version_info
        ;;
    "release")
        create_release $2
        ;;
    "help"|"--help"|"-h")
        show_usage
        ;;
    *)
        print_error "Unknown command: $1"
        echo
        show_usage
        exit 1
        ;;
esac