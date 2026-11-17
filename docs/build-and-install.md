# Build, validate and install

Target: TCL 32V4C (2025), Google TV / Android 14 (API 34). Minimum API 28.

## Build

Install JDK 17 or 21 and Android SDK Platform 35. Set `ANDROID_HOME` or put `sdk.dir=/path/to/sdk` in an untracked `local.properties` file.

```sh
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. The wrapper pins Gradle 8.9. CI uploads the debug APK. Debug signing is for testing; a release distributed to others needs your own stable signing key.

## Emulator validation

Create an API 34 Android TV virtual device in Android Studio. Start it without changing its default Home app, then run:

```sh
./gradlew :app:connectedDebugAndroidTest
```

The instrumentation suite checks library and favorite-picker navigation, Back behavior, D-pad focus and persistence across activity recreation, including unavailable favorite packages. Also manually check:

- Every control is reachable using only D-pad and Select, including after scrolling the app library.
- Each favorite can be changed and survives closing/reopening the app.
- Install/remove an ordinary app and return: library refreshes; unavailable favorites offer selection.
- Settings and Inputs open or show the firmware fallback.
- Choose local photos, restart, clear photos and cancel the picker; verify behavior with a removed document.
- Select **TCL Launcher photos** in system screensaver settings, preview, exit with remote input.
- Check layout at 720p and 1080p and with long app names.

## Install as an ordinary app on the TV

Enable developer options and ADB debugging, authorize your computer, and select the intended device if more than one is connected:

```sh
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n in.athulanilthomas.tcllauncher/.MainActivity
```

Repeat the emulator checks on the physical TV before considering a default Home change. Photo selection needs a file/document provider; the app explains when none is available. The Inputs shortcut opens Android TV input settings; firmware may expose HDMI switching only through its remote or Settings. Screensaver availability and timing are controlled by Android TV.

## Recovery and optional Home selection

Keep the stock launcher installed and enabled. If the TV exposes a default Home picker, only consider it after ordinary-app testing and a verified route back through Settings. This app does not select itself as default or disable anything.

To stop using the test app, select the stock Home app in TV Settings if needed, then uninstall **TCL Launcher** through Settings. Uninstall removes preferences and photo grants; source photos are never deleted. Do not disable Google TV Home or other system packages. See [safety notes](safety.md). Revoke ADB authorizations and turn off debugging when finished.
