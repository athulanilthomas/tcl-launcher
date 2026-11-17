# Safety and recovery

The target TV runs Google TV on Android 14. Installing a launcher APK is different from replacing the OS.

**Never disable the stock launcher until a working alternative and a recovery method have been verified on the real device.** Some Google TV builds restrict changing the default HOME application.

Before any ADB system changes:
- Confirm the TV is reachable and you can access Settings using the physical remote.
- Save a local inventory of packages, disabled packages, system properties and settings.
- Identify every package before considering disabling it. Do not use generic debloat lists blindly.
- Keep Wi-Fi, Bluetooth remote pairing, HDMI, TV tuner, firmware updates and core Android services intact.
- Keep personal data and device logs out of Git.

Use `scripts/backup.sh` to collect a **read-only configuration inventory**. It is not a full firmware backup.

A package disabled with `adb shell pm disable-user --user 0 PACKAGE` can often be restored with `adb shell pm enable PACKAGE`, but recovery is not guaranteed if ADB becomes unavailable. Never execute such commands automatically.

After testing, revoke ADB authorizations and turn off debugging on the TV.
