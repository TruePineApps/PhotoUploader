#!/usr/bin/env bash
#
# Inject client_secrets.json from the local project repository into an installed CI test app.
#
# Usage:
#   macOS:                   sudo ./inject-client-secrets.sh /Applications/photo-uploader.app
#   Linux:                   sudo ./inject-client-secrets.sh /opt/photo-uploader
#   Windows/bash:            ./inject-client-secrets.sh "/c/Program Files/Photo-Uploader"
#   Windows/CMD & PowerShell: bash inject-client-secrets.sh "C:/Program Files/Photo-Uploader"
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SOURCE_SECRETS="$SCRIPT_DIR/composeApp/src/desktopMain/resources/client_secrets.json"

if [ ! -f "$SOURCE_SECRETS" ]; then
    echo "Error: Local secrets file not found at: $SOURCE_SECRETS"
    echo "Please ensure client_secrets.json exists in your local working directory."
    exit 1
fi

if [ "$#" -ne 1 ]; then
    echo "Usage: [sudo] $0 <path-to-installed-app-or-bundle>"
    echo ""
    echo "Examples:"
    echo "  macOS:                   sudo $0 /Applications/photo-uploader.app"
    echo "  Linux:                   sudo $0 /opt/photo-uploader"
    echo "  Windows/bash:            $0 \"/c/Program Files/Photo-Uploader\""
    echo "  Windows/CMD & PowerShell: bash $0 \"C:/Program Files/Photo-Uploader\""
    exit 1
fi

TARGET="$1"

if [ ! -e "$TARGET" ]; then
    echo "Error: Target path does not exist: $TARGET"
    exit 1
fi

# Detect resources directories inside macOS .app bundle, Linux /opt install, or Windows Program Files
DEST_DIRS=()

if [ -d "$TARGET/Contents" ]; then
    # macOS .app bundle
    DEST_DIRS+=("$TARGET/Contents/app" "$TARGET/Contents/app/resources" "$TARGET/Contents/Resources")
elif [ -d "$TARGET/lib/app" ]; then
    # Linux /opt install
    DEST_DIRS+=("$TARGET/lib/app" "$TARGET/lib/app/resources")
elif [ -d "$TARGET/app" ]; then
    # Windows Program Files
    DEST_DIRS+=("$TARGET/app" "$TARGET/app/resources")
else
    DEST_DIRS+=("$TARGET")
fi

for dir in "${DEST_DIRS[@]}"; do
    mkdir -p "$dir"
    cp -v "$SOURCE_SECRETS" "$dir/client_secrets.json"
done

# Inject client_secrets.json directly into composeApp JAR archives so java.getResourceAsStream("/client_secrets.json") finds it
mapfile -t JAR_FILES < <(find "$TARGET" -name "composeApp*.jar" 2>/dev/null || true)
if [ ${#JAR_FILES[@]} -gt 0 ] && [ -f "${JAR_FILES[0]}" ]; then
    SECRETS_DIR="$(dirname "$SOURCE_SECRETS")"
    SECRETS_FILE="$(basename "$SOURCE_SECRETS")"
    for jar in "${JAR_FILES[@]}"; do
        if [ -f "$jar" ]; then
            echo "Updating JAR archive: $jar"
            if command -v jar >/dev/null 2>&1; then
                (cd "$SECRETS_DIR" && jar uf "$jar" "$SECRETS_FILE")
            elif command -v zip >/dev/null 2>&1; then
                (cd "$SECRETS_DIR" && zip -u "$jar" "$SECRETS_FILE")
            fi
        fi
    done
fi

echo "Successfully injected client_secrets.json into app bundle and JAR."
