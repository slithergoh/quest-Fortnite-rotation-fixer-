# Ripper Rotation — Meta Quest 3 prototype

A small Android/Horizon OS 2D panel app that detects a Fortnite Android package and launches it through Android's package manager.

## Honest capability boundary

The orientation buttons call `Activity.setRequestedOrientation()` on **Ripper Rotation's own activity only**. They do not rotate Fortnite, change the headset's global orientation, or inject into/modify Fortnite. A normal app cannot call `setRequestedOrientation()` on another app's Activity. Android documents that API as changing the desired orientation of the Activity on which it is called.

Meta's Horizon OS documentation describes app panel sizing and app activity orientation, but does not document a public API that lets an unrelated app control another app's window orientation. So a wrapper that truly rotates Fortnite is not currently established as possible without privileged/system-level support or an OS feature.

## Package detection

Default package is `com.epicgames.fortnite`. Epic's Quest/Android installation channel may use a different package ID. If detection says NOT DETECTED, inspect the installed package with:

```powershell
adb shell pm list packages | Select-String -Pattern 'epic|fortnite'
```

Then update `FORTNITE_PACKAGE` in `MainActivity.java` and the `<queries>` entry in `AndroidManifest.xml` to match exactly.

## Build without Android Studio

Requirements:
- JDK 17
- Android SDK command-line tools
- Android platform SDK 35
- Gradle 8.9 or compatible 8.x

Set `JAVA_HOME` to JDK 17 and `ANDROID_HOME` to your SDK folder. Install SDK components:

```powershell
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
```

If Gradle is installed globally:

```powershell
gradle assembleDebug
```

APK output:
`app/build/outputs/apk/debug/app-debug.apk`

Install and launch:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p online.ripperheaven.rotation 1
```

Connect the Quest with USB debugging enabled and accepted.

## OpenCode task

Open this folder in OpenCode and ask it to:
1. Review and build the project using JDK 17 and command-line Android SDK tools only.
2. Do not add permissions for accessibility, overlays, root, or device-admin just to claim cross-app rotation.
3. Verify the actual Fortnite package ID from `adb shell pm list packages` when a Quest is connected.
4. Run `gradle assembleDebug`, fix compile errors, and report whether an APK was produced.
5. Keep the UI explicit that rotation is launcher-only unless a documented, tested Horizon OS capability proves otherwise.

## Meta references
- https://developers.meta.com/vr/documentation/android-apps/create-app/
- https://developers.meta.com/vr/documentation/native/android/mobile-native-manifest/
- https://developer.android.com/reference/android/app/Activity#setRequestedOrientation(int)
