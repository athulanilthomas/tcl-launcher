# TCL Launcher — Coding agent instructions

Target: TCL 32V4C (2025), Google TV, Android 14 (API 34).

Build a minimal Android TV launcher with Kotlin and Compose for TV. Preserve D-pad focus, Back navigation, TV settings access, and a full app library.

## Safety
- Never root, flash, factory reset, uninstall system packages, or open service menus.
- Do not disable any packages or the stock Home app without explicit user approval, a local backup and verified recovery.
- Protect Wi-Fi, Bluetooth remote, HDMI/input, TV tuner, Settings, System UI, Google Play Services, and OTA updates.
- Do not assume Google TV allows third-party HOME replacement; test first.
- Never commit device inventories, serial numbers, IP addresses, screenshots, or personal photos.
- Test on an emulator and install the new APK as an ordinary app before any HOME configuration changes.

## First implementation
1. Create a Gradle Android app, compile against a supported SDK, with Android 14 device compatibility.
2. Add a TV-friendly activity with clock, date, greeting and four configurable favorite apps.
3. Implement app discovery and launches, complete app library, Settings and input shortcuts.
4. Implement remote D-pad focus and Back behavior.
5. Add optional local photo backgrounds and screensaver after core usability.
6. Document install, validation and recovery before touching stock launcher.

## Design
- Top left: clock and date
- Top right: Settings, Inputs, App Library
- Bottom left: greeting and caption
- Bottom right: four favorite apps
- Background: optional local photos, with text contrast and crop safeguards

Reference: https://github.com/Shubhamsaboo/awesome-llm-apps/blob/main/advanced_llm_apps/take-back-your-tv/agent.md
