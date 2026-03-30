#!/usr/bin/env bash
set -euo pipefail

APP_NAME="com.microsoft.teams.activity"
VERSION="2.0"
TARGET="target"
JPACKAGE_OUT="$TARGET/jpackage"
ZIP_NAME="${APP_NAME}-${VERSION}-macos.zip"

echo "==> Building fat jar..."
mvn package -q

echo "==> Running jpackage to create .app bundle..."
mvn verify -q

APP_BUNDLE="$JPACKAGE_OUT/${APP_NAME}.app"

if [ ! -d "$APP_BUNDLE" ]; then
  echo "ERROR: jpackage output not found at $APP_BUNDLE"
  exit 1
fi

echo "==> Zipping .app bundle..."
cd "$JPACKAGE_OUT"
zip -qr "../../$TARGET/$ZIP_NAME" "${APP_NAME}.app"
cd - > /dev/null

echo ""
echo "Build complete. Artifacts:"
echo "  Jar : $TARGET/${APP_NAME}.jar"
echo "  App : $APP_BUNDLE"
echo "  Zip : $TARGET/$ZIP_NAME"
echo ""
echo "To run the app:"
echo "  open $APP_BUNDLE"
echo "  (or double-click in Finder)"
