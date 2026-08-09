# Aurora Browser Lite

A lightweight Android browser built on **Android System WebView** (Chromium) with a **Jetpack Compose** UI, designed for **TV / couch navigation** (D-pad focus, pointer cursor, remote control) while remaining fully usable on phones.

![Platform](https://img.shields.io/badge/platform-android-green) ![minSdk](https://img.shields.io/badge/minSdk-30-green) ![targetSdk](https://img.shields.io/badge/targetSdk-37-green) ![tests](https://img.shields.io/badge/tests-70%20passing-green)

## Highlights

- **Lightweight engine** — Android System WebView, updated through the OS, no bundled engine binary
- **Tabs with lifecycle** — RAM-based limits, idle sleep/discard, session restore, private tabs, popup routing
- **Encrypted passwords** — AES-256-GCM vault (AndroidKeyStore) with JS form-capture
- **Full library** — history (day-grouped, search, clear), bookmarks with folders, downloads (resume/pause/notifications/foreground service)
- **Reader mode, find-in-page, voice search, media viewers** (video/image/audio), picture-in-picture
- **TV-first input** — custom D-pad focus engine, pointer cursor, virtual keyboard, remote-control panel
- **Crash resilience** — in-process `onRenderProcessGone` recovery with automatic tab reload
- **Performance center** — live CPU / RAM / FPS / network metrics
- **70 unit tests** passing
- Desktop mode, permission management, site settings, encrypted credential store

## Architecture

Multi-module Gradle project. The shipped launcher (`com.aurora.ui.MainActivity`) lives in `Aurora_UI_Compose`; `:app` is the APK shell (manifest, signing, ABI splits, R8).

```
:app                    APK shell — manifest, strings, resources, release config
├─ :Aurora_UI_Compose   Real app UI: screens, controllers, SessionManager, overlays
│
├─ :engine:api          Engine abstraction (BrowserEngine / Session / Callbacks / Settings / LoginStorage)
├─ :engine:webview      Android WebView engine: sessions, JS login capture, thumbnail + metadata, pure mappers
│
├─ :data                Room DB (v6) + DataStore prefs + AES-GCM vault, repositories, search engines,
│                       favicon/thumbnail/metadata services, in-memory fallbacks
├─ :browser             Browser domain: BrowserController, DownloadManager + foreground service,
│                       HistoryService, ReaderContentExtractor, SystemMetricsCollector
├─ :home                Home domain: HomeController, URL classifier / search, HomeState
├─ :core                shared leaf module (place-holder)
│
├─ :ui:focus            TV D-pad focus engine (grouped graph, ring navigation, select/back)
├─ :design              design tokens — glass, glow, shapes, HSL accent engine
└─ :motion              motion design system — specs, spring library, aurora* modifiers
```

### Key entry points

| Concern | File |
|---|---|
| Launcher | `Aurora_UI_Compose/src/main/kotlin/com/aurora/ui/MainActivity.kt` |
| App shell & overlays | `Aurora_UI_Compose/src/main/kotlin/com/aurora/ui/App.kt` |
| Tab/session management | `Aurora_UI_Compose/src/main/kotlin/com/aurora/ui/viewmodel/BrowserEngineProvider.kt` |
| Engine contract | `engine/api/src/main/kotlin/com/aurora/engine/BrowserEngine.kt` |
| WebView engine | `engine/webview/src/main/kotlin/com/aurora/engine/webview/WebViewBrowserSession.kt` |
| Persistence | `data/src/main/kotlin/com/aurora/data/` (db / repository / preferences / security) |
| Motion system | `motion/src/main/kotlin/com/aurora/motion/MotionSpec.kt` |
| Focus navigation | `ui/focus/src/main/kotlin/com/aurora/ui/focus/FocusEngine.kt` |

## Build & Run

Requirements: JDK 17, Android SDK (compileSdk 37).

```bash
# assemble a debug APK
./gradlew :app:assembleDebug

# install on device/emulator
./gradlew :app:installDebug

# run unit tests
./gradlew test
```

## Release notes

- **2026-08-09** — Engine swap: migrated from GeckoView to Android System WebView for a lighter APK (~50 MB smaller) with OS-managed engine updates.
- **2026-08-08** — Unit-test milestone: 65 JVM tests (browser, data, home, ui:focus).
- Earlier milestones: P0/P1 hardening (crash recovery, threading, password encryption, i18n, telemetry, tab lifecycle) — see `CHANGELOG.md`.

## Documentation

- `AUDIT_REPORT_2026-08-08.md` — production-readiness audit (achieved vs remaining gaps)
- `CHANGELOG.md` — build history

The codebase is the source of truth; design/marketing documentation is intentionally not shipped in the repo.

## License

Proprietary — all rights reserved. Not licensed for redistribution.
