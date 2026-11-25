#!/usr/bin/env bash
set -euo pipefail

# Inputs come from the Gradle task: jpackage image, icon, launcher, and final artifact path.
if [[ $# -ne 4 ]]; then
  echo "Usage: $0 <jpackage-app-image-dir> <icon.png> <launcher-name> <output.AppImage>" >&2
  exit 2
fi

source_dir="$(realpath "$1")"
icon_file="$(realpath "$2")"
launcher_name="$3"
output_file="$(realpath -m "$4")"

# Fail early if the jpackage image is incomplete or this host lacks the AppImage filesystem builder.
if [[ ! -x "$source_dir/bin/$launcher_name" ]]; then
  echo "jpackage launcher not found: $source_dir/bin/$launcher_name" >&2
  exit 1
fi

if ! command -v mksquashfs >/dev/null 2>&1; then
  echo "mksquashfs is required; install squashfs-tools." >&2
  exit 1
fi

# Pin both downloaded build tools by digest; appimagetool's runtime is supplied explicitly below,
# avoiding its default behavior of retrieving a runtime during packaging.
tool_url="https://github.com/AppImage/appimagetool/releases/download/1.9.1/appimagetool-x86_64.AppImage"
tool_sha256="ed4ce84f0d9caff66f50bcca6ff6f35aae54ce8135408b3fa33abfc3cb384eb0"
runtime_url="https://github.com/AppImage/type2-runtime/releases/download/continuous/runtime-x86_64"
runtime_sha256="617aa77314d57c8b8e4fae9c3f0b35af7b30d5529cb69e98f64f92510ed98a46"
# Stage beside the output to keep packaging on the same filesystem; clean up on success or failure.
mkdir -p "$(dirname "$output_file")"
work_dir="$(mktemp -d "$(dirname "$output_file")/.appimage-build.XXXXXX")"
trap 'rm -rf "$work_dir"' EXIT

tool="$work_dir/appimagetool.AppImage"
runtime="$work_dir/runtime-x86_64"
curl --fail --location --silent --show-error --retry 3 "$tool_url" --output "$tool"
printf '%s  %s\n' "$tool_sha256" "$tool" | sha256sum --check --status
curl --fail --location --silent --show-error --retry 3 "$runtime_url" --output "$runtime"
printf '%s  %s\n' "$runtime_sha256" "$runtime" | sha256sum --check --status
chmod 755 "$tool"

(
  cd "$work_dir"
  "$tool" --appimage-extract >/dev/null
)

# Assemble the AppDir expected by appimagetool from jpackage's self-contained application image.
app_dir="$work_dir/AppDir"
mkdir -p "$app_dir"
cp -a "$source_dir/." "$app_dir/"
install -m 644 "$icon_file" "$app_dir/photo-uploader.png"
ln -s "bin/$launcher_name" "$app_dir/AppRun"

cat > "$app_dir/photo-uploader.desktop" <<'DESKTOP'
[Desktop Entry]
Name=Photo Uploader
Comment=Upload a photo collection organized in folders to Google Photo
Exec=AppRun
Icon=photo-uploader
Terminal=false
Type=Application
Categories=Utility;
DESKTOP

# Force the published architecture and use only the checksum-verified runtime above.
ARCH=x86_64 "$work_dir/squashfs-root/AppRun" --no-appstream --runtime-file "$runtime" "$app_dir" "$output_file"
test -s "$output_file"
