# Status and remaining validation

Implemented:
- Kotlin / Compose for TV app and reproducible Gradle wrapper build
- Locale-aware clock/date, greeting and four persistent, configurable favorites
- Refreshing app library, D-pad controls, Back navigation and launch feedback
- TV Settings and input-settings shortcut with fallback
- Persisted local photo selection, slideshow, dark contrast overlay and uncropped images
- Opt-in Android DreamService photo screensaver
- Instrumentation coverage and build/lint CI

Still requires physical TCL validation:
- Remote focus and layout at the TV's resolution
- Firmware support for input settings, document providers and screensaver selection
- Ordinary APK install and use before any default Home change
- Optional third-party HOME eligibility, user selection and verified recovery

No automatic debloating, package disabling or stock Home replacement.
