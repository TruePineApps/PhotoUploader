#!/usr/bin/env bash
set -euo pipefail

# -----------------------------------------------------------------------------
# PhotoUploader Release Helper Script
#
# Reads version metadata from gradle/libs.versions.toml, verifies git state,
# creates an annotated Git release tag (vMAJOR.MINOR.PATCH), and pushes it to
# GitHub to trigger the automated release workflow.
# -----------------------------------------------------------------------------

TOML_FILE="gradle/libs.versions.toml"

if [ ! -f "$TOML_FILE" ]; then
    echo "::error:: Cannot find $TOML_FILE. Please run this script from the project root."
    exit 1
fi

# 1. Check if git working directory is clean
if [ -n "$(git status --porcelain)" ]; then
    echo "❌ Error: Working directory has uncommitted changes. Please commit or stash them first."
    git status --short
    exit 1
fi

# 2. Check if currently on 'main' branch
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "main" ]; then
    echo "❌ Error: You are currently on branch '$CURRENT_BRANCH'. Releases must be cut from 'main'."
    exit 1
fi

# 3. Ensure local 'main' is up to date with remote
echo "📥 Fetching latest changes from origin..."
git fetch origin main
LOCAL_SHA=$(git rev-parse HEAD)
REMOTE_SHA=$(git rev-parse origin/main)

if [ "$LOCAL_SHA" != "$REMOTE_SHA" ]; then
    echo "❌ Error: Your local 'main' branch ($LOCAL_SHA) is not in sync with 'origin/main' ($REMOTE_SHA)."
    echo "   Please run 'git pull origin main' first."
    exit 1
fi

# 4. Extract version parts from libs.versions.toml
get_toml_val() {
    sed -nE "s/^$1 *= *\"([^\"]*)\".*/\1/p" "$TOML_FILE"
}

MAJOR=$(get_toml_val "cfg-appMajor")
MINOR=$(get_toml_val "cfg-appMinor")
PATCH=$(get_toml_val "cfg-appPatch")
STAGE=$(get_toml_val "cfg-appStage")

if [ -z "$MAJOR" ] || [ -z "$MINOR" ] || [ -z "$PATCH" ]; then
    echo "❌ Error: Failed to parse cfg-appMajor, cfg-appMinor, or cfg-appPatch from $TOML_FILE."
    exit 1
fi

VERSION="$MAJOR.$MINOR.$PATCH"
TAG_NAME="v$VERSION"

# 5. Check if tag already exists locally or remotely
if git rev-parse "$TAG_NAME" >/dev/null 2>&1; then
    echo "❌ Error: Local tag '$TAG_NAME' already exists."
    echo "   If you want to release a new version, bump 'cfg-appPatch' in $TOML_FILE first."
    exit 1
fi

if git ls-remote --tags origin | grep -q "refs/tags/$TAG_NAME"; then
    echo "❌ Error: Remote tag '$TAG_NAME' already exists on GitHub."
    echo "   If you want to release a new version, bump 'cfg-appPatch' in $TOML_FILE first."
    exit 1
fi

# 6. Display confirmation prompt
echo ""
echo "================================================="
echo " 🚀 Ready to release PhotoUploader $VERSION"
echo "================================================="
echo " Tag name:    $TAG_NAME"
echo " Version:     $VERSION"
echo " Stage:       ${STAGE:-Final Release}"
echo " Branch:      $CURRENT_BRANCH"
echo " Target SHA:  $LOCAL_SHA"
echo "================================================="
echo ""

read -p "Do you want to create and push tag $TAG_NAME to GitHub? (y/N): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Operation canceled. No tag was created."
    exit 0
fi

# 7. Create annotated tag and push
echo "🏷️ Creating annotated tag $TAG_NAME..."
git tag -a "$TAG_NAME" -m "PhotoUploader $VERSION"

echo "📤 Pushing tag $TAG_NAME to origin..."
git push origin "$TAG_NAME"

echo ""
echo "✅ Tag $TAG_NAME pushed successfully!"
echo "   GitHub Actions release pipeline started."
echo "   Monitor progress at: https://github.com/TruePineApps/PhotoUploader/actions"
