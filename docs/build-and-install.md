# Build and install

Target device: TCL 32V4C (2025), Google TV / Android 14 (API 34).

## Build

Install JDK 17, Android SDK Platform 35, and Gradle 8.9, or use Android Studio with a compatible Gradle installation.

```sh
gradle :app:assembleDebug
```

The output is `app/build/outputs/apk/debug/app-debug.apk`.

The GitHub Actions workflow builds the same APK on each push to main and uploads it as an Actions artifact. This repository does not yet include a Gradle wrapper; CI provisions Gradle explicitly.

## Test without changing the default Home app

Enable developer options and ADB debugging on the TV, pair or connect from your computer, then:

```sh
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n in.athulanilthomas.tcllauncher/.MainActivity
```

Test directional remote navigation, OK/Select, Back, app launching, All apps, and Settings.

**Do not disable Google TV Home or run package-disabling commands.** A HOME intent filter makes the app eligible as a Home activity but does not guarantee TCL firmware allows selecting it as the default.

## Limitations of the first version

- Favorites are currently the first four alphabetically discovered apps, not user-configurable yet.
- Photos, screensaver and dedicated input shortcut are not implemented yet.
- Some system apps may be invisible or blocked by package visibility/device policy.
- No physical-TV or emulator test has been completed yet.
