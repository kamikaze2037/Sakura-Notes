# Sakura Notes

Android note app prototype with a modern cherry blossom UI.

## Features

- Home screen for notes
- Search notes
- Tags
- Add note
- Edit note
- Delete note
- Pinned note UI
- Sakura pastel Material 3 style

## Build APK without Android Studio

### Option 1: GitHub Actions

1. Upload this whole project folder to a GitHub repository.
2. Go to **Actions**.
3. Run **Build Android APK**.
4. Download the APK from **Artifacts**.

APK output path:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Option 2: Command line

Install JDK 17, Android SDK, and Gradle, then run:

```bash
gradle assembleDebug
```

## Notes

This version stores notes in memory only. When you close the app, newly created notes are not saved permanently yet.
Next recommended upgrade: add Room Database.
