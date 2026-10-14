#!/usr/bin/env bash
# Builds the signed APK without Gradle, using the plain Android build tools
# (aapt2, javac, d8/dx, zipalign, apksigner).
#
#   ./build.sh            -> dist/SchnickSchnackSchnuck.apk
#
# Tool lookup: $ANDROID_HOME/build-tools/<newest>, then $PATH
# (Debian/Ubuntu: apt install aapt zipalign apksigner dalvik-exchange android-sdk-platform-23).
set -euo pipefail
cd "$(dirname "$0")"

VERSION_CODE=2
VERSION_NAME=2.0
PACKAGE=de.chr1ss0.ssp
MIN_SDK=23
TARGET_SDK=34
APK_NAME=SchnickSchnackSchnuck.apk
KEYSTORE=keystore/ssp-release.jks
KEY_ALIAS=ssp
KEY_PASS=${SSP_KEY_PASS:-schnickschnack}

export JAVA_TOOL_OPTIONS=""
SDK=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/usr/lib/android-sdk}}

find_tool() {
  local name=$1
  local bt
  bt=$(ls -d "$SDK"/build-tools/*/ 2>/dev/null | sort -V | tail -1 || true)
  if [ -n "$bt" ] && [ -x "$bt$name" ]; then echo "$bt$name"; return; fi
  command -v "$name" 2>/dev/null || true
}

ANDROID_JAR=""
for p in $(ls -d "$SDK"/platforms/android-*/ 2>/dev/null | sort -V -r); do
  if [ -f "$p/android.jar" ]; then ANDROID_JAR="$p/android.jar"; break; fi
done
[ -n "$ANDROID_JAR" ] || { echo "android.jar not found (set ANDROID_HOME)"; exit 1; }

AAPT2=$(find_tool aapt2)
ZIPALIGN=$(find_tool zipalign)
APKSIGNER=$(find_tool apksigner)
D8=$(find_tool d8)
DX=$(find_tool dx)
[ -n "$DX" ] || DX=$(command -v dalvik-exchange || true)
for t in AAPT2 ZIPALIGN APKSIGNER; do [ -n "${!t}" ] || { echo "missing tool: $t"; exit 1; }; done
[ -n "$D8$DX" ] || { echo "missing tool: d8 or dx"; exit 1; }

OUT=build/apk
rm -rf "$OUT" && mkdir -p "$OUT/classes" "$OUT/gen" dist

echo "== resources"
"$AAPT2" compile --dir app/src/main/res -o "$OUT/res.zip"
# the source manifest has no package attribute (Gradle style) -> inject it
sed "s|<manifest |<manifest package=\"$PACKAGE\" |" app/src/main/AndroidManifest.xml > "$OUT/AndroidManifest.xml"
"$AAPT2" link -I "$ANDROID_JAR" --manifest "$OUT/AndroidManifest.xml" \
  --min-sdk-version $MIN_SDK --target-sdk-version $TARGET_SDK \
  --version-code $VERSION_CODE --version-name $VERSION_NAME \
  --java "$OUT/gen" -o "$OUT/base.apk" "$OUT/res.zip"

echo "== java"
javac -nowarn -encoding UTF-8 -source 8 -target 8 -bootclasspath "$ANDROID_JAR" -d "$OUT/classes" \
  $(find app/src/main/java "$OUT/gen" -name '*.java') 2>&1 | grep -v -e '^warning: \[options\]' -e '^[0-9]* warnings\?$' || true
test -f "$OUT/classes/de/chr1ss0/ssp/app/MainActivity.class"

echo "== dex"
if [ -n "$D8" ]; then
  "$D8" --release --min-api $MIN_SDK --lib "$ANDROID_JAR" --output "$OUT" $(find "$OUT/classes" -name '*.class')
else
  "$DX" --dex --min-sdk-version=$MIN_SDK --output="$OUT/classes.dex" "$OUT/classes"
fi

echo "== package"
cp "$OUT/base.apk" "$OUT/unaligned.apk"
(cd "$OUT" && zip -q -X unaligned.apk classes.dex)
"$ZIPALIGN" -p -f 4 "$OUT/unaligned.apk" "$OUT/aligned.apk"

if [ ! -f "$KEYSTORE" ]; then
  echo "== creating keystore $KEYSTORE"
  mkdir -p "$(dirname "$KEYSTORE")"
  keytool -genkeypair -keystore "$KEYSTORE" -storepass "$KEY_PASS" -keypass "$KEY_PASS" -alias "$KEY_ALIAS" \
    -keyalg RSA -keysize 2048 -validity 20000 -dname "CN=Schnick Schnack Schnuck, O=chr1ss0"
fi

echo "== sign"
"$APKSIGNER" sign --v4-signing-enabled false --ks "$KEYSTORE" --ks-pass "pass:$KEY_PASS" --ks-key-alias "$KEY_ALIAS" --key-pass "pass:$KEY_PASS" \
  --out "dist/$APK_NAME" "$OUT/aligned.apk"
"$APKSIGNER" verify "dist/$APK_NAME"
ls -l "dist/$APK_NAME"
echo "OK -> dist/$APK_NAME"
