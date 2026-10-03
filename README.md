# Azad Launcher

Azad Launcher is an original Android home-screen application. It is being built as a
clean-room project: it does not reuse Nova Launcher source code, assets, branding, or
proprietary UI.

## Incremental delivery

The project is delivered in small, testable commits. The first milestone provides a
responsive home surface, a searchable app drawer, and standard Android Home intent
support. Subsequent milestones will add independent customization, organization,
backup, and accessibility features.

## Build

```bash
./gradlew testDebugUnitTest assembleDebug
```

The debug APK is produced under `app/build/outputs/apk/debug/`.
