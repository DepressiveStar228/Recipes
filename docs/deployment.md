# DevOps guide

## Device requirements
- Android 8.0 (API 26) or later
- CPU: ARM64
- RAM: 2 GB+
- Storage: 100 MB of free space

## Required software for assembly
- Android SDK
- Android Debug Bridge (ADB)
- Java JDK
- Gradle

## Network settings
- Most of the application's functions work **offline**, without an internet connection
- The integration function with **ChatGPT API** requires an active network connection

## Building a release build
```bash
./gradlew assembleRelease
```

## Installing on the device
```bash
adb install app/build/outputs/apk/release/app-release.apk
```

## Performance check
- The application opens without errors
- Data is successfully stored locally
- Basic functions work correctly
- ChatGPT function responds when there is internet access