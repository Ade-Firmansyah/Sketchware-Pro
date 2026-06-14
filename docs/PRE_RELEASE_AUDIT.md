# Sketchware Complete Pre-Release Audit

This document records the verified scope of the pre-release modernization
branch. It intentionally separates implemented work from future architecture
so experimental features are not presented as production-ready.

## Implemented And Verified

- Material 3 modernization for the main project experience.
- Application localization with 14 languages, English fallback, persistence,
  immediate activity refresh, and Arabic RTL support.
- File picker lifecycle recovery when Android recreates an activity or nested
  fragment after a configuration or locale change.
- Backup archive validation before restore, including corrupt ZIP detection,
  required project metadata checks, and zip-path traversal protection.
- Debug APK, release APK, and signed Android App Bundle export paths.
- Android Studio source export with module sources, Gradle build files, Gradle
  Wrapper 8.13, and a `local.properties.template`.
- Generated project defaults of `compileSdk 36`, `targetSdk 35`, Gradle 8.13,
  Android Gradle Plugin 8.12.0, and AndroidX.
- Local contributor recognition for Ade Firmansyah (LotusVolt), linked to the
  `Ade-Firmansyah` GitHub profile.

## Existing Features Preserved

- Project creation, import/export, clone, rename, delete, and templates.
- Block editor, components, events, resources, and custom logic.
- APK/AAB generation, signing, manifest/resource/source generation.
- Firebase, AdMob, file picker, storage, notification, intent, and networking
  components.

## Deferred After Community Testing

- Scheduled automatic backups and transactional rollback.
- Importing arbitrary Android Studio projects into Sketchware.
- AI provider integrations and extension marketplace execution.
- A full adaptive tablet/foldable editor panel redesign.
- Complete translation of every legacy UI string.

These items require separate design, migration, and device-testing phases.
They are not silently enabled in this pre-release.

## Validation Commands

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleRelease
.\gradlew.bat :app:lintDebug
```

Device testing should cover locale changes while a file picker is open,
backup restore with valid and intentionally corrupt archives, APK/AAB export,
and Android Studio Gradle sync of an exported source ZIP.
