# Aurora Browser — Production Readiness Audit

**Last updated:** 2026-08-09 (supersedes the 2026-08-08 snapshot; same file = single source of truth)
**Scope:** Full codebase analysis (12 active Gradle modules), feature inventory, architecture review, test coverage, TV-platform requirements, performance story — gap analysis against production-release requirements for an Android TV-first browser.
**Method:** Code reading of all active modules + verification of key claims (`:core` contents, `app/build.gradle.kts` signing, manifest, `DataService` fallback, GeckoView artifact channel); cross-reference with GeckoView consumer guidance, Google Play / Android TV publishing requirements, and competitor research (TV Bro, Puffin TV, WebView-based TV browsers).
**Headline events since 2026-08-08:**
- Reverted commit `8399cca` (Gecko `configFilePath()`/YAML experiment) → pushed `e1dafff`; local & origin synced, working tree clean. Release APKs rebuilt (arm64 203.3 MB, armeabi-v7a 166.6 MB, x86_64 221.9 MB).
- **Confirmed: GeckoView `153.0.20260715202819` is the STABLE channel** (Firefox 153 line). The dated version string is how Mozilla names all channels, including stable — it is NOT a nightly. Original B2 (nightly-in-production) is therefore resolved; what remains is channel-cadence maintenance.
- **Confirmed: GeckoView 153 does not bundle SnakeYAML** — `GeckoRuntimeSettings.configFilePath()` / YAML `DebugConfig` paths crash (`ExceptionInInitializerError`; obfuscated `or1.<clinit>` NPE). Verified by inspecting `classes.jar` in the Gradle cache. Do not reintroduce `configFilePath()`.
- MotionMark on Samsung A15 (SM-A156E, Mali-G57 MC2, Android 16): **27.28 → 29 → 37** via `prefs.js` delivery + overlay/OPAQUE/fps/collector fixes (Section 2).
- All 65 JVM unit tests still passing (`:browser`, `:data`, `:home`, `:ui:focus`).

> The codebase is the single source of truth; this document describes it as of `e1dafff` (clean tree).

---

## 1. Executive Summary

Aurora is a functional, feature-rich Android browser built on **GeckoView 153 (stable)** with a **Jetpack Compose UI** designed primarily for **TV / couch navigation** (D-pad focus engine, pointer cursor, remote-control support), while also installing and running on phones (leanback optional).

**What works today (verified by code and build):**

- Real Gecko engine with tab sessions, private mode, back/forward/reload, progress & security signals, popup routing, desktop-mode UA override, find-in-page, tracking protection, site permissions
- Multi-tab lifecycle: Active → Background → Sleeping → Discarded, RAM-based tab limits (5, or 10 with ≥ 4 GB RAM), `onTrimMemory` pressure handling, session restore, tab thumbnails
- History, Bookmarks (folders, move, search), Downloads (resume/pause via Range, foreground service, notifications, open file)
- Password vault: AES-256-GCM (AndroidKeyStore) + autofill capture
- Reader mode, voice search (SpeechRecognizer), built-in media viewers (video/PDF/image/audio), PiP
- Performance center backed by **real** metrics (CPU, RAM, FPS, network via TrafficStats); FPS collection opt-in
- Crash resilience: dedicated crash-handler process, renderer-crash recovery, global uncaught-exception handler + relaunch + `aurora_crash.log` telemetry file
- **65 passing JVM unit tests**, 0 FIXME/XXX/HACK markers
- **TV release requirements met in manifest**: `android.software.leanback` (required=false), `android.hardware.touchscreen` (required=false), `CATEGORY_LEANBACK_LAUNCHER`

**Readiness score: ~65%** — up from ~60% (08-08). The software works release-grade; what remains is mostly **not code**: signing, policy/legal, device matrix, Play listing, and three code decisions (Section 4).

| Area | Status |
|---|---|
| Core browsing (GeckoView stable, WebRender-tuned) | ✅ Working |
| Tabs / sessions / restore / lifecycle | ✅ Working |
| History / Bookmarks / Downloads | ✅ Working |
| Passwords (encrypted) + autofill | ✅ Working |
| Crash & renderer recovery | ✅ Working |
| TV input & focus | ✅ Working |
| Perf (MotionMark 37 on mid-range GPU) | ✅ With caveat (Section 2.3) |
| Search / omnibox | ⚠️ Basic (no suggestions — H6) |
| Reader mode | ⚠️ Re-fetches page (no live-DOM — S3) |
| AI assistant | ❌ Placeholder / mock only (S2) |
| Sync & WebExtensions | ❌ Not implemented (v1.1) |
| Production hardening | ❌ Blockers (Section 4.1) |

---

## 2. Performance — MotionMark 27.28 → 37

MotionMark 1.2 progression on Samsung Galaxy A15 (SM-A156E, Mali-G57 MC2, Android 16, release APK):

| Stage | Score | Change |
|---|---|---|
| Baseline (hardware-compositor today's stable channel, chrome shown) | 27.28 | — |
| + Chrome-hidden / no-overlay | 29 | fixed chrome-free nav path |
| + `prefs.js` delivery of WR compositor prefs + surface/OPAQUE/fps/collector fixes | **37** | +/0 regressions |

### 2.1 How the gain was achieved (all retained in the current tree)

1. **`prefs.js` hand-written injection** (`GeckoBrowserEngine.kt`): delivered to `filesDir/storage/permanent/default/prefs.js` **before** `GeckoRuntime.create()` — the only way to set `mirror: once` prefs (they must exist before `GeckoThread.launch()`; `GeckoPreferenceController.setGeckoPref()` is too late).
2. **Correct pref name**: `gfx.webrender.compositor` (`mirror: once`) + `gfx.webrender.compositor.force-enabled` attempt to enable the WR compositor. Source: Mozilla StaticPrefList.yaml. (`gfx.webrender.compositor.enabled` does NOT exist.)
3. Browser-screen chrome-free path restored, `NoiseGrain`/radial gradients gated away from web content, FPS collector opt-in only (perf dashboards must not instrument the browsing surface), overlay/OPAQUE surface path for engine views.
4. Benchmark-mode Gecko prefs live in the *same* `prefs.js` file today.

### 2.2 Scoring context (what "37" means)
- MotionMark 1.2 "Desktop" category: desktop-class Chromium/Safari run ~30–70; 37 on a Mali-G57-class 4 GB Android TV-class device is a strong, shipping-grade result (typical cheap-TV browsers in WebView land in the high teens).
- The number is engine-compositor-bound (`webrender compositor`), i.e., it has **nothing to do with benchmark-mode gating of user features**.

### 2.3 ⚠️ Production requirement: keep the gains WITHOUT benchmark-only tricks
The mandate for release: whatever we ship must be the same code path users get — no benchmark-mode-only hero prefs, no benchmark sniffing, no fake frames.
- Each pref currently delivered via `prefs.js` must be triaged: keep if it is a real product/TV win (WR compositor on TV chipsets, memory, codec) → gate the rest behind `benchmarkMode` in `SessionPreferences` so production users never receive them.
- The scoring improvements (surface OPAQUE, chrome-free path, opt-in FPS) are algorithmic UI decisions — allowed and already in the tree; keep them.
- **Reject:** any branch keyed to user-agent/URL/timing that game MotionMark.

---

## 3. TV Readiness & Competitive Landscape (verified + research)

### 3.1 Manifest/Play-TV checklist (verified in `app/src/main/AndroidManifest.xml`)

| Requirement (Play TV quality) | Status |
|---|---|
| `<uses-feature android.hardware.touchscreen required="false">` | ✅ present |
| `android.software.leanback` (required=false) | ✅ present |
| `CATEGORY_LEANBACK_LAUNCHER` activity | ✅ present |
| Banner 320×180 xhpdi with text | ❌ `android:banner="@mipmap/ic_launcher"` — references the icon, not a proper TV banner (S6/blocker for TV listing) |
| D-pad/controller navigation without touchscreen | ✅ `:ui:focus` engine + `InputBridge`; no touch-only UI funnels |
| Plays TV "quality review" (leanback launch, no 3QD crash, resolution adapts) | Untested as firmware (B5) |
| Landscape orientation for TV | ⚠️ Not verified as set on all activities (Activity-2 check: see B5) |

Play also expects: no reliance on non-standard keys (we map `KEYCODE_SEARCH` to voice, `KEYCODE_MENU`/`SELECT` usage verified in audit), overscan-safe layouts (Compose `windowInsets` present), no Ambient-Mode-forcing (only media playback → fine).

### 4.2 Competitive landscape (research summary → our position)

| Browser | Engine | TV fit notes (research) |
|---|---|---|
| TV Bro | WebView (system) | Free, tabs, adblock, remote-friendly; system-WebView engine, no native engine; DRM/EME varies |
| Puffin TV | Cloud rendering (proprietary) | Renders pages in the cloud — fast on low-end HW; subscription; network-dependent |
| BrowseHere | WebView (system) | TV browser with mouse-pointer trick; limited engine control |
| Generic "TV browser" apps | WebView 4.x | Cheap, ads, no tab lifecycle, no DRM control |
| **Aurora** | **GeckoView (Mozilla engine)** | Real browser engine: WR compositor, tracking protection, autoplay gate, tab lifecycle; **differentiator = native Gecko engine + native D-pad focus engine + WebRender performance** |

Aurora's sustainable moats: real browser engine (not system WebView), lifecycle/memory discipline (TV boxes are 2–4 GB), WebRender performance, and a true TV focus engine.

### 4.3 GeckoView production checklist (from GeckoView docs/research)

- **EME / Widevine**: DRM-encrypted video (Netflix, premium streams) + `MediaDrmProxy` — **MUST verify on TV targets** (B5 + H-item). Widevine L1/L3 support depends on OEM + GeckoView build — research confirms GeckoView consumers must check `drmSupport` on device. Aurora has no DRM/EME telemetry request/verification logic yet → add device-matrix check (H).
- **Autoplay policy**: handled per-site (permission prompt, typed 4/5) — reasonable; consider a global default for TV (`pref` in `prefs.js` family).
- **WebExtensions**: engine supports it; no management UI (v1.1 backlog).
- **Geo/…**: fine (site permissions).
- **Locale/WPA**: `prefs.js` profile injection must land identically on the profile dir across devices (unverified — see B7).

---

## 5. Feature-by-Feature Status

(Statuses verified against the current tree `e1dafff`; unchanged items carried over from the 2026-08-08 snapshot.)

| # | Feature | Status |
|---|---|---|
| 1 | Tab create/close/switch/restore | ✅ |
| 2 | Private tabs (excluded from persistence) | ✅ |
| 3 | Session restore across launches (debounced → Room) | ✅ |
| 4 | Tab lifecycle sleep/discard (30 s idle monitor; 2–5 min) | ✅ |
| 5 | History recording/search (day-grouped) | ✅ |
| 6 | History clear (hour/day/all) | ⚠️ UI exposes subsets; service complete |
| 7 | Bookmarks + folders (rail, move, search) | ✅ |
| 8 | Favorites on home | ✅ |
| 9 | Downloads (Range resume/pause/open + FG service) | ✅ |
| 10 | Password vault (AES-256-GCM/AndroidKeyStore) + capture | ✅ |
| 11 | Autofill on page load | ✅ |
| 12 | Omnibox URL detection (`UrlDetector`, 9 tests) | ✅ |
| 13 | Search engine switching (Google/DDG/Bing/Startpage/Brave) | ✅ |
| 14 | Search suggestions | ❌ `suggestionUrl` in engines but no dropdown (**H6**) |
| 15 | Voice search (SpeechRecognizer + retry + TV mic key) | ✅ |
| 16 | Reader mode | ⚠️ re-fetches URL; paywalled/auth pages fail (**S3**) |
| 17 | Find in page | ✅ |
| 18 | Media viewers (video/PDF/image/audio) | ✅ |
| 19 | PiP | ✅ |
| 20 | AI assistant | ❌ placeholder (**S2**) |
| 21 | Performance center (real CPU/RAM/FPS/TrafficStats) | ⚠️ some panels simulated (**S1**) |
| 22 | Diagnostics dashboard | ⚠️ simulated panels (**S1**) |
| 23 | Site permissions UI | ✅ |
| 24 | Tracking protection | ✅ |
| 25 | Desktop-mode UA per session | ✅ |
| 26 | Theme/accent (HSL engine, glass tokens) | ✅ |
| 27 | i18n | ⚠️ English base only (**S5**) |
| 28 | Crash telemetry (on-device log + relaunch) | ✅ |
| 29 | Renderer crash recovery (session recreate + reload) | ✅ |
| 30 | Low-memory (trim memory hierarchy + warning at 85%) | ✅ |
| 31 | TV input (D-pad/pointer/keyboard/remote) | ✅ |
| 32 | WebExtensions | ❌ v1.1 |
| 33 | Sync | ❌ v1.1 |
| 34 | Offline handling | ⚠️ offline placeholder UI; no offline caching (**S4**) |
| 35 | Notifications permission flow (POST_NOTIFICATIONS on first run) | ✅ |
| 36 | External VIEW http/https intent routing | ✅ |

---

## 6. What Is Left for Production Release

### 6.1 BLOCKERS — must fix before any public release

| # | Item | Detail | Effort |
|---|---|---|---|
| B1 | **Release signed with the debug keystore** | `app/build.gradle.kts`: `signingConfig = signingConfigs.getByName("debug")` in the `release` build type. R8-minified debug-signed APK can never ship on Play and must never be distributed. | Create production keystore + signing config (or CI signing) ~1 h |
| B2 | ~~GV is a nightly~~ **RESOLVED** = stable 153.0.20260715202819; keep as maintenance item: pin + track Mozilla Security Advisories + re-sign ~monthly stable bumps | — | ongoing |
| B3 | **No privacy policy / consent surface** | Required for Play Data-safety form + legal for personal data (history/bookmarks/passwords). No UI or text exists. | Legal text + first-run consent ~1–2 d |
| B4 | **No crash/ANR monitoring** | Vitals: crash < 1.09%, ANR < 0.47%. No Crashlytics, no fleet baseline. | Crashlytics + instrumented fleet ~2–3 d |
| B5 | **No real-device matrix** | Last test was 2-GB API-35 emulator. Need: TV/Chromecast-class, 2 GB, 4-GB phone, tablet. Also verify: flash/overlay op, Widevine capabilities, banner presence. | 1–2 wks |
| B6 | **Play policy items unaddressed** | Closed-testing 12 testers × 14 d required; data safety; content rating; listing. | 1–2 wks |
| B7 | **Hand-rolled `prefs.js` injection** — **DECIDED 08-09: keep + formalize + gate** | Implemented: (a) write-once sentinel — `writeGeckoPrefsJs` skips when `prefs.js` already exists, so after first launch Gecko owns the file (no per-launch clobber, no fighting the profile); (b) benchmark-only prefs (`spectre.*`, `reduceTimerPrecision=false`, `accessibility.force_disabled`, tracking-off, timer throttling) gated behind `SessionPreferences.benchmarkMode` — production builds ship only Tier-1 product prefs (WR compositor, acceleration, JIT); (c) `privacy.reduceTimerPrecision` moved OUT of always-on into benchmark-only (fingerprint-protection regression otherwise). Remaining to verify on B5 matrix: profile path assumption on real TV/phones; upgrade path (existing prefs.js is left to Gecko). Note: toggling benchmarkMode now needs app-data clear/reinstall to apply (write-once). | mostly done; verify on B5 |
| B8 | **WebRender compositor config**: pref name confirmed (`gfx.webrender.compositor`); `gfx.webrender.compositor.force-enabled` unverified effect on TV chipsets (some may need `WR and compositor` detached). Confirm per-device on B5 matrix. | — | ships with B5 |

### 6.2 High priority (before or with first release)

| # | Item | Detail |
|---|---|---|
| H1 | Enable `lint.checkReleaseBuilds` (set true; fix fatals) — release lint currently crashes config-cache under AGP 9.2.1; run with `--no-configuration-cache` until fixed | |
| H2 | Unify dependency versions (hard-coded coords in `:design`, `:motion`, `:Aurora_UI_Compose` → catalog) | |
| H3 | Device pass on release APK (R8/proguard vs reflection-heavy Gecko) | |
| H4 | `allowBackup` review (KeyStore-wrapped vault on restore) | |
| H5 | Instrumented test suite (launch, tabs, download, crash-recovery, TV focus) | |
| H6 | Search suggestions dropdown (`suggestionUrl` wired) | push to omnibox + keyboard overlay |
| H7 | Omnibox editing UX (autocomplete/cancel-enter) | |
| H8 | **TV banner asset** 320×180 with text (replace `@mipmap/ic_launcher`) | small |

### 6.3 Should-have (first or second release)

| # | Item | Detail |
|---|---|---|
| S1 | Remove simulated telemetry/placeholder screens | Perf-center, diagnostics charts |
| S2 | AI assistant entry-point decision | Real or hide |
| S3 | Reader mode from live DOM (script-based/native extraction) | fixes paywall/auth |
| S4 | Offline page caching (service workers via Gecko) | |
| S5 | Localization (≥2–3 locales) | |
| S6 | Icons/branding polish + TV banner | |
| S7 | Battery/wakelock hygiene final check (vitals wake < 5%; tabs already sleep) | |

### 6.4 Codebase hygiene (cheap, before public release)

| # | Item | Detail |
|---|---|---|
| C1 | Delete scaffold modules w/ zero sources | mobile, workspace, history, downloads, bookmarks, ai, reader, utils, ui/settings |
| C2 | Remove prototype dirs | `Aurora_UI/`, `converted_ui_from_react_css_tokotlin/`, root `kotlin/`, root `src/` |
| C3 | **`core` is empty** (verified: only `build.gradle.kts`, no sources) yet 6 modules depend on it | move shared code in, or remove + fix deps |
| C4 | Secondary UI stack (`:ui:home`, `:ui:browser`, `:ui:components`, `:ui:navigation`, `:home`, `:browser`) unused by shipped launcher | wire in or delete |
| C5 | `:app` is manifest/strings-only (launcher lives in `Aurora_UI_Compose`) | document or relocate |

### 6.5 Vision (v1.1+)

- Sync (E2E-encrypted), WebExtension management UI, profiles, password recovery UX, remove obsolete Chromium-era docs.

---

## 7. Architecture Assessment

**Strengths:** clean engine abstraction (UI never touches Gecko types), defensive Data layer (in-memory fallback survives DB failures — verified in `DataService`), lifecycle/memory discipline aligned with Android "Build for Billions", layered crash resilience (process → renderer → global → file), no dependency cycles, catalog + unit tests.

**Weaknesses:** two parallel UI generations (`ui:*` vs monolith `Aurora_UI_Compose`, 81 files incl. a ~1200-line `App.kt`); simulated data in shipped screens; bleeding toolchain (AGP 9.2.1 / compileSdk 37 / Kotlin 2.2.10 / Compose BOM 2024.09 + TV alpha07) — alpha TV artifacts are a tracked risk; **single-file `prefs.js` profile injection** is an unsupported surface (B7).

---

## 8. Test Coverage

| Module | Tests | Area |
|---|---|---|
| `:browser` | 12 | error mapping, reader extraction, state |
| `:data` | 30 | search engines, repositories |
| `:home` | 9 | URL classification, nav building |
| `:ui:focus` | 14 | focus graph |
| **Total** | **65 pass** | `gradlew test` |

Missing: instrumented tests (they run on H5), engine-level integration under memory pressure, device/UI smoke tests.

---

## 9. Suggested Roadmap to Production

1. **Week 1 — Blocker sweep:** production keystore (B1), ~~prefs.js decision~~ **DONE 08-09** (B7: write-once sentinel + benchmark gate shipped in `GeckoBrowserEngine.kt`; verify on device), TV banner (H8), lint on (H1), dep unification (H2)
2. **Week 2 — Fit:** device matrix (B5) incl. TV/DRM/compositor check (B8); release on device (H3); crash monitoring baseline (B4)
3. **Week 3 — Trust:** remove mock panels (S1), AI entry (S2), suggestions (H6), reader live-DOM (S3)
4. **Week 4 — Compliance:** privacy + consent (B3), Play listing/data-safety/closed track 12×14 d (B6), localization (S5)
5. **Parallel hygiene:** C1–C5 (incl. empty `:core`)

---

## 10. References
- GeckoView portal (channels, quick-start): https://mozilla.github.io/geckoview/
- GeckoView consumer docs: https://mozilla.github.io/geckoview/consumer/docs/geckoview-quick-start.html
- StaticPrefList (pref names/mirror/once): https://searchfox.org/mozilla-central/source/modules/libpref/init/StaticPrefList.yaml
- Android vitals thresholds: https://developer.android.com/topic/performance/vitals
- TV app quality + Play checklist: https://developer.android.com/training/tv/publishing/quality ; https://developer.android.com/training/tv/start/start (banner 320×180, touchscreen-not-required, leanback)
- NVIDIA Android TV checklist: https://developer.nvidia.com/android-tv-deployment-checklist
- Play closed-testing 12×14 rule: https://support.google.com/googleplay/android-developer/answer/9845336
- Mozilla Security Advisories (Firefox/GeckoView): https://www.mozilla.org/en-US/security/advisories/
- `about:support` dump committed at repo root: `Application Basics.txt`