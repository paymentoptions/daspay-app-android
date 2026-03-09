#!/bin/bash

# Flavor Build Verification Script
# This script tests all flavor builds to ensure the configuration is correct

echo "======================================"
echo "Daspay Flavor Build Verification"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to build and check
build_flavor() {
    local flavor=$1
    local variant=$2

    echo -e "${YELLOW}Building ${flavor}${variant}...${NC}"

    if ./gradlew assemble${flavor}${variant} --no-daemon > /dev/null 2>&1; then
        echo -e "${GREEN}✓ ${flavor}${variant} build successful${NC}"
        return 0
    else
        echo -e "${RED}✗ ${flavor}${variant} build failed${NC}"
        return 1
    fi
}

# Change to project directory
cd "$(dirname "$0")"

# Clean build
echo "Cleaning previous builds..."
./gradlew clean --no-daemon > /dev/null 2>&1

echo ""
echo "Testing flavor builds..."
echo ""

# Build each flavor
success_count=0
total_count=0

# Dev Debug
total_count=$((total_count + 1))
if build_flavor "Dev" "Debug"; then
    success_count=$((success_count + 1))
fi

echo ""

# Dev Release
total_count=$((total_count + 1))
if build_flavor "Dev" "Release"; then
    success_count=$((success_count + 1))
fi

echo ""

# Staging Debug
total_count=$((total_count + 1))
if build_flavor "Staging" "Debug"; then
    success_count=$((success_count + 1))
fi

echo ""

# Staging Release
total_count=$((total_count + 1))
if build_flavor "Staging" "Release"; then
    success_count=$((success_count + 1))
fi

echo ""

# Production Debug
total_count=$((total_count + 1))
if build_flavor "Production" "Debug"; then
    success_count=$((success_count + 1))
fi

echo ""

# Production Release
total_count=$((total_count + 1))
if build_flavor "Production" "Release"; then
    success_count=$((success_count + 1))
fi

echo ""
echo "======================================"
echo "Build Summary"
echo "======================================"
echo -e "Total builds: ${total_count}"
echo -e "Successful: ${GREEN}${success_count}${NC}"
echo -e "Failed: ${RED}$((total_count - success_count))${NC}"
echo ""

if [ $success_count -eq $total_count ]; then
    echo -e "${GREEN}All builds completed successfully!${NC}"
    exit 0
else
    echo -e "${RED}Some builds failed. Please check the errors above.${NC}"
    exit 1
fi

