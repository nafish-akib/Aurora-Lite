# Changelog

All notable changes are drawn from the git history. Builds prior to the 2026-07 stabilization rounds are omitted; see `git log` for full history.

## 2026-08-08 — Testing milestone

- **65 JVM unit tests** added across `:browser` (error mapping, reader extraction, state), `:data` (search engines, favorite/history/download repositories), `:home` (URL classification), `:ui:focus` (focus graph)
- Reader mode: pure-Kotlin HTML entity decode fallback (works off-device)
- Added `testImplementation(junit)` to the four tested modules
- New `AUDIT_REPORT_2026-08-08.md` (production-readiness audit); all legacy docs removed; README rewritten to match the current module structure

## 2026-08-07 — P2 hardening

- **Crash telemetry**: uncaught exceptions saved to `cache/aurora_crash.log` (timestamp, thread, stacktrace); previous-session crashes reported at next launch
- **Tab lifecycle**: 5-state model (Active → Background → Sleeping → Discarded) with automatic transitions (5 min idle → sleep, 10 min idle → discard), 30 s monitor, cleanup of collectors and Gecko sessions on discard
- **i18n**: base English string table (~90 keys) covering navigation, actions, status, settings, library, passwords, performance, errors

## 2026-08-06 — P1 hardening

- **Download foreground service** (`dataSync`) with stop action and notification channel
- Autofill API availability check before wiring
- Reader mode extraction verified/robustified
- **Password vault**: AES-256-GCM encryption via AndroidKeyStore (`EncryptedPrefs`); `GeckoLoginStorage` form capture; PasswordManagerScreen to view saved logins
- History `visitCount` increment fix; tab-state save debounce (500 ms)
- Theme unification (single design-token source), find-in-page UI panel, overlay wiring
- Dead code and simulated metrics removed from core paths; log noise cleanup

## 2026-07 — P0 fixes

- **Renderer crash recovery**: dead-session handling replaced with session recreation + reload; `crashRecoveryUsed` flag wired
- **Threading fix**: engine calls on main thread (GeckoRuntime enforcement)
- Global uncaught-exception handler auto-relaunches app; proactive memory monitor (85% RAM warning), `onTrimMemory` tab closing
- Hardware-accelerated rendering (WebRender / JIT / canvas) — MotionMark 19 → 150+
- Functional theme/accent system (HSL dynamic color engine)
- Real voice search (SpeechRecognizer) replacing simulation
- Real system metrics (Choreographer FPS, TrafficStats, ActivityManager)
- Streaming hub on dashboard, D-pad Tab Management, focus-engine integration across all TV screens
- Downloads: consume GeckoView response body correctly, MIME passthrough, permissions, premium progress UI
- Rename to "Aurora Browser"; release build config (ABI splits, R8, proguard keep rules)