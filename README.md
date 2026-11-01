# Ripper Rotation — Fortnite per-app resize prototype

Ripper Rotation now uses Shizuku (ADB shell privileges) to apply Android's `FORCE_RESIZE_APP` compatibility override to the Fortnite package only.

## What it does

- Detects the Fortnite package `com.epicgames.fortnite`.
- Connects to Shizuku and asks the user to approve shell access.
- Runs `am compat enable FORCE_RESIZE_APP com.epicgames.fortnite` to enable Android's per-package resize compatibility override.
- Can undo the setting with `am compat disable FORCE_RESIZE_APP com.epicgames.fortnite`.
- Launches Fortnite normally after the setting is applied.

The command names one package only. It does not change the Quest's global display resolution or other apps.

## Important limitation

`FORCE_RESIZE_APP` makes an app resizable and bypasses Android size-compatibility mode. It does **not** set a custom 4:3 aspect ratio, create a virtual display, spoof an iPad, or guarantee that Fortnite will render in an iPad layout. Quest firmware may also reject or ignore this compatibility override. The app displays the actual shell command output so a failure is visible rather than claiming success.

This is not the virtual-display solution. Android restricts launching unrelated apps onto private virtual displays, and this project does not bypass that restriction.

## Setup on Quest

1. Install the Shizuku app from https://shizuku.rikka.app/download/.
2. Start Shizuku's service using its Wireless debugging instructions or ADB instructions. On non-rooted devices, Shizuku must be started again after a headset reboot.
3. Open Ripper Rotation and tap **CONNECT / GRANT SHIZUKU ACCESS**.
4. Approve the permission prompt.
5. Tap **ENABLE FORTNITE RESIZE MODE**.
6. Fully close Fortnite and relaunch it using **LAUNCH FORTNITE**.
7. If it does not help, tap **RESTORE FORTNITE DEFAULT**.

## Package ID

The default package is `com.epicgames.fortnite`. If Fortnite uses a different package ID on the Quest, update `FORTNITE_PACKAGE` in `MainActivity.java` and the package query in `AndroidManifest.xml`.

## Build without Android Studio

Requirements: JDK 17, Android SDK command-line tools, platform SDK 35, and Gradle 8.9.

```powershell
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
gradle --no-daemon assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## References

- Android per-app compatibility overrides: https://developer.android.com/guide/practices/device-compatibility-mode
- Shizuku API: https://github.com/RikkaApps/Shizuku-API
- Shizuku setup: https://shizuku.rikka.app/guide/setup/
