# TCL Launcher

A minimal Android TV launcher for TCL 32V4C (2025), Google TV / Android 14, built with Kotlin and Compose for TV.

- Clock and date respect the TV's locale and 12/24-hour preference
- Four persistent favorites, configurable under **Personalize**
- Full launchable app library, refreshed when returning to the launcher
- D-pad focus, Select activation and Back navigation
- Settings and TV input settings shortcuts with firmware fallbacks
- Up to 20 local photo backgrounds, rotating every 30 seconds
- Optional Android photo screensaver, enabled through TV screensaver settings

Photos are selected through Android's document picker and stay on the device. A document provider is required; some TV firmware does not include one. Missing or inaccessible photos fall back to the dark background. The app needs no network or broad storage permission.

## Build and try

Use JDK 17 (or 21), Android SDK Platform 35 and the included Gradle wrapper:

```sh
./gradlew :app:assembleDebug :app:lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n in.athulanilthomas.tcllauncher/.MainActivity
```

See [build, validation and recovery](docs/build-and-install.md). GitHub Actions publishes a debug APK artifact for each build.

Install and test as an ordinary TV app first. HOME registration does not guarantee that Google TV permits a third-party default launcher. No system packages are disabled or changed by this app.

Physical TCL validation is still required for vendor-specific Inputs, screensaver selection and optional default Home behavior.
