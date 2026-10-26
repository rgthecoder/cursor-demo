# TV Home — Simple Android TV Launcher

Personal sideloadable **home launcher** for Android TV (Kotlin + Jetpack Compose for TV).  
No built-in browser. Optional Accessibility Service for reading on-screen text from other apps.

## Features

- Focusable app grid (D-pad / remote)
- Launch apps with OK / center
- Can be set as the default **Home** app (`HOME` + `LEANBACK_LAUNCHER`)
- Hide apps from the grid (long-press a tile, or use **Hidden apps**)
- Toggle **Show system apps**
- **Read screen** via Accessibility Service (enable once in Settings)

## Requirements

- Android Studio Hedgehog+ (or JDK 17+ and Android SDK)
- Android TV / Google TV device with Developer options
- USB or network ADB (recommended)

## Build

### Android Studio

1. Open the `tv-launcher/` folder in Android Studio.
2. Let Gradle sync finish.
3. **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. Debug APK path: `app/build/outputs/apk/debug/app-debug.apk`

### Command line

```bash
cd tv-launcher
./gradlew :app:assembleDebug
```

Release (unsigned unless you configure signing):

```bash
./gradlew :app:assembleRelease
```

## Install (ADB)

1. On the TV: **Settings → System → About** → click Build number 7× to enable Developer options.
2. Enable **USB debugging** (and/or **Network debugging**).
3. Connect:

```bash
adb connect <TV_IP>:5555   # if using network debugging
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Debug package id: `com.personal.tvlauncher.debug`  
Release package id: `com.personal.tvlauncher`

You can also copy the APK to the TV and install with a file manager if unknown sources are allowed.

## Set as Home launcher

1. Press the **Home** button on the remote.
2. When Android asks which home app to use, choose **TV Home**.
3. Optionally set it as always / default so Home opens this launcher.

If no picker appears: **Settings → Apps → Special app access / Default apps → Home app** (wording varies by OEM) → select **TV Home**.

## Enable Accessibility (screen content)

1. Open **TV Home** → **Read screen** → **Enable accessibility**,  
   or go to **Settings → Accessibility**.
2. Enable **TV Home Screen Reader**.
3. Confirm the system warning (personal / trusted sideload use only).
4. Open another app so content is on screen, press Home, then **Read screen** again to capture text.

Accessibility is **not** required for the launcher or Home role — only for reading other apps’ UI text.

## Tips

- **Long-press** an app tile to hide it from the grid.
- **Hidden apps** panel can unhide packages.
- **Refresh** reloads the installed-app list after installs/uninstalls.

## Project layout

```
tv-launcher/
  app/src/main/
    AndroidManifest.xml
    java/com/personal/tvlauncher/
      LauncherActivity.kt
      data/          # PackageManager + DataStore prefs
      ui/            # Compose for TV home grid
      a11y/          # AccessibilityService
    res/xml/accessibility_service_config.xml
```

## Out of scope (v1)

Channels / watch-next, voice search, uninstalling OEM apps, Play Store publishing.
