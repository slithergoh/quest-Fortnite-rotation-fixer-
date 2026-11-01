# Ripper Rotation 0.3.0 — safe Quest Fortnite launcher

## What this version does

- Detects the Fortnite package `com.epicgames.fortnite`.
- Launches Fortnite through Android's normal app-launch API.
- Opens Fortnite's Android app-info page when Horizon OS exposes it.
- Runs without Shizuku, root, ADB, shell commands, or privileged permissions.

## Important limitation

A normal Android/Quest app can request orientation for its own activity only. It cannot force a separate Fortnite activity to rotate or render at a custom 4:3 aspect ratio. Resizing a floating Quest panel is controlled by Horizon OS and does not change the game's internal render aspect ratio.

This build deliberately removes the earlier Shizuku shell implementation. It does not write compatibility overrides, change global display settings, alter resolution, create virtual displays, or modify system files. This makes it safer, but it is a launcher rather than a working Fortnite rotation/4:3 patch.

## Quest install

Install the generated APK using your normal Quest sideloading method. Open Ripper Rotation and choose **LAUNCH FORTNITE**. **OPEN FORTNITE APP SETTINGS** opens the Android app-info page if Horizon OS permits it.

## Build

Requirements: JDK 17, Android SDK command-line tools, platform SDK 35, and Gradle 8.9.

```powershell
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
gradle --no-daemon assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## References

- Meta Quest developer setup: https://developers.meta.com/vr/documentation/native/android/mobile-device-setup/
- Android app compatibility and aspect-ratio controls: https://developer.android.com/guide/practices/device-compatibility-mode
