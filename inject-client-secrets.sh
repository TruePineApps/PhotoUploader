#!/usr/bin/env bash
#
# Inject client_secrets.json from the local project repository into an installed CI test app.
#
# Usage:
#   macOS:                   sudo ./inject-client-secrets.sh /Applications/photo-uploader-beta.app
#   Linux:                   sudo ./inject-client-secrets.sh /opt/photo-uploader-beta
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
    echo "  macOS:                   sudo $0 /Applications/photo-uploader-beta.app"
    echo "  Linux:                   sudo $0 /opt/photo-uploader-beta"
    echo "  Windows/bash:            $0 \"/c/Program Files/Photo-Uploader\""
    echo "  Windows/CMD & PowerShell: bash $0 \"C:/Program Files/Photo-Uploader\""
    exit 1
fi

TARGET="$1"

if [ ! -e "$TARGET" ]; then
    echo "Error: Target path does not exist: $TARGET"
    exit 1
fi

# Detect resources directory inside macOS .app bundle, Linux /opt install, or Windows Program Files
if [ -d "$TARGET/Contents/app/resources" ]; then
    DEST_DIR="$TARGET/Contents/app/resources"
elif [ -d "$TARGET/lib/app/resources" ]; then
    DEST_DIR="$TARGET/lib/app/resources"
elif [ -d "$TARGET/app/resources" ]; then
    DEST_DIR="$TARGET/app/resources"
elif [ -d "$TARGET" ]; then
    DEST_DIR="$TARGET"
else
    echo "Error: Could not determine resources directory inside: $TARGET"
    exit 1
fi

mkdir -p "$DEST_DIR"
cp -v "$SOURCE_SECRETS" "$DEST_DIR/client_secrets.json"

echo "Successfully injected client_secrets.json into:"
echo "  $DEST_DIR/client_secrets.json"
