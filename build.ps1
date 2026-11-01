$ErrorActionPreference = "Stop"

Write-Host "Ripper Rotation - command-line APK build" -ForegroundColor Cyan

if (-not $env:JAVA_HOME) {
    Write-Error "JAVA_HOME is not set. Install JDK 17 and set JAVA_HOME."
}
$java = Join-Path $env:JAVA_HOME "bin\java.exe"
if (-not (Test-Path $java)) { Write-Error "JAVA_HOME does not point to a valid JDK." }

if (-not $env:ANDROID_HOME -and -not $env:ANDROID_SDK_ROOT) {
    Write-Error "Set ANDROID_HOME or ANDROID_SDK_ROOT to your Android SDK directory."
}
$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { $env:ANDROID_SDK_ROOT }
$sdkmanager = Join-Path $sdk "cmdline-tools\latest\bin\sdkmanager.bat"
if (Test-Path $sdkmanager) {
    & $sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
}

if (-not (Get-Command gradle -ErrorAction SilentlyContinue)) {
    Write-Error "Gradle is not installed or not on PATH. Install Gradle 8.x, then rerun."
}

& gradle assembleDebug
if ($LASTEXITCODE -ne 0) { throw "Gradle build failed." }

$apk = Join-Path $PSScriptRoot "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apk)) { throw "Build returned success but APK was not found at $apk" }
Write-Host "APK created: $apk" -ForegroundColor Green
