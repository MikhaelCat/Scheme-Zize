#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

export ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$ANDROID_HOME}"

mkdir -p dist
rm -f dist/Scheme-Zize*.jar

./gradlew clean deploy --no-daemon -PmindustryVersion=v156
cp build/libs/Scheme-Zize.jar dist/Scheme-Zize.jar

echo
ls -la dist/
unzip -p dist/Scheme-Zize.jar mod.hjson | grep -E 'version:|minGameVersion:'
