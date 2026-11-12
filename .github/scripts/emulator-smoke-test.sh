#!/usr/bin/env bash
set -Eeuo pipefail

RESULTS_DIR="emulator-results"
APK="app/build/outputs/apk/debug/app-debug.apk"
PACKAGE="com.jennyai.app"
mkdir -p "$RESULTS_DIR"

collect_diagnostics() {
  local code=$?
  set +e
  echo "Collecting emulator diagnostics (exit=$code)"
  adb devices -l > "$RESULTS_DIR/adb-devices.txt" 2>&1
  adb logcat -d -t 16000 > "$RESULTS_DIR/logcat.txt" 2>&1
  adb shell dumpsys activity activities > "$RESULTS_DIR/activity.txt" 2>&1
  adb shell dumpsys window windows > "$RESULTS_DIR/windows.txt" 2>&1
  adb shell getprop > "$RESULTS_DIR/getprop.txt" 2>&1
  exit "$code"
}
trap collect_diagnostics EXIT

echo "Waiting for emulator boot completion..."
adb wait-for-device
for attempt in $(seq 1 90); do
  state="$(adb get-state 2>/dev/null || true)"
  boot="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
  if [[ "$state" == "device" && "$boot" == "1" ]]; then
    echo "Emulator is fully booted."
    break
  fi
  sleep 5
done

state="$(adb get-state 2>/dev/null || true)"
boot="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
if [[ "$state" != "device" || "$boot" != "1" ]]; then
  echo "Emulator not ready: state=$state boot=$boot"
  exit 1
fi

if [[ ! -s "$APK" ]]; then
  echo "APK missing: $APK"
  exit 1
fi

adb shell settings put system font_scale 1.0
adb install -r "$APK"
adb shell pm grant "$PACKAGE" android.permission.RECORD_AUDIO || true
adb shell monkey -p "$PACKAGE" -c android.intent.category.LAUNCHER 1
sleep 25

pid="$(adb shell pidof "$PACKAGE" 2>/dev/null | tr -d '\r' || true)"
printf '%s\n' "$pid" > "$RESULTS_DIR/app-pid.txt"
if [[ -z "$pid" ]]; then
  echo "Jenny process is not running after launch."
  exit 1
fi

# Stream the screenshot to the runner instead of writing to protected /sdcard.
adb exec-out screencap -p > "$RESULTS_DIR/jenny-home.png"
test -s "$RESULTS_DIR/jenny-home.png"
echo "PASS: Jenny launched; process=$pid; screenshot captured."
