# AUDIT REPORT — WebView Browsing Experience: Compatibility, Stability & Feature Roadmap

**Date:** 2026-08-09 · **Scope:** `engine/webview` (Android System WebView, Chromium), `app` shell, WebView-only build
**Method:** Full code reading of the engine bridge + platform/Chromium research (UA reduction, UA-CH, cookie policy, renderer termination, Safe Browsing, WebAuthn, Web Push, storage isolation)

---

## 0. Executive summary

Aurora's WebView engine is functionally rich (tabs, private mode, downloads, permissions, fullscreen, find-in-page, crash callback) but has **five compatibility landmines** that cause "site won't open", "not permitted" style failures, and streaming-site breakage:

1. **The app ships a spoofed *desktop* Chrome/120 UA by default** (`WebViewBrowserSession.kt:193`). Since WebView only sends `Sec-CH-UA` client hints when the **default** UA is used, Aurora sends *no* client hints at all — exactly what modern anti-bot protection (Cloudflare, Google, streaming platforms) looks for. The UA is also frozen at Chrome/120 in August 2026 (current is ~14x), which itself trips "browser not supported" checks.
2. **`shouldInterceptRequest` re-fetches streaming-site traffic through `HttpURLConnection`** (`WebViewBrowserSession.kt:225–251`) — a different TLS stack (different JA3/JA4 fingerprint), no cookie jar attachment, and **POST bodies always empty** (`readRequestBody` returns `null`, line 453). This is the classic cause of Cloudflare "checking if the site connection is secure… verify you are human" loops.
3. **Third-party cookies are OFF by default.** Apps targeting LOLLIPOP+ default to disallowing third-party cookies, and Aurora never calls `setAcceptThirdPartyCookies(...)` — breaking embedded sign-in widgets, oAuth iframes, comment/chat embeds on many sites.
4. **`onRenderProcessGone` reloads the *same* WebView instance** (`WebViewBrowserSession.kt:289–300`). Official guidance is that the instance is unusable after renderer death — it must be removed, destroyed and recreated. This is the #1 source of "crash then blank/stuck screen" reports.
5. **Several platform trades are silently missing:** geolocation is never enabled (`setGeolocationEnabled` absent → the whole permission flow is dead), notifications permission requests are always denied (only camera/mic handled, line 401–433), WebAuthn/passkeys are **unsupported in WebView** (MDN: Android WebView = None) with no fallback, and there is no "open in browser" escape hatch.

Fixing the first four items is small (hours) and unlocks the majority of "works in Chrome but not Aurora" complaints. Items 6–15 in §5 are the full recovery/feature program.

---

## 1. Current engine snapshot (ground truth, from code)

`engine/webview/src/main/kotlin/com/aurora/engine/webview/`

| Concern | State | Location |
|---|---|---|
| JS, DOM storage, DB storage, multi-window, autoplay, wide viewport, zoom controls | ✅ enabled | `WebViewBrowserSession.kt:176–189` |
| Mixed content | ✅ `ALWAYS_ALLOW` (compat-first; flagged risk) | `:190` |
| File access | ✅ locked (`allowFileAccess=false`) | `:186` |
| **UA** | ⚠️ **Desktop Chrome/120 string by default**, even in "mobile" mode | `:192–194`, `:437–438` |
| Desktop-mode toggle | ⚠️ no-op when a custom UA is set (`setDesktopMode` returns early) | `:117–123` |
| **Network interception** | ⚠️ `HttpURLConnection` re-fetch for 10 streaming hosts + any "API-like" request | `:225–251`, `:440–451` |
| Request body | ❌ always dropped (`readRequestBody → null`) | `:453` |
| Cookies | ✅ global accept follows pref; **no third-party control** | `:196` |
| Geolocation | ❌ callback exists (`:371`) but `setGeolocationEnabled(true)` **never called** → permission prompt never fires; manifest also lacks `ACCESS_COARSE/FINE_LOCATION` | `configureView` |
| Camera/mic (getUserMedia) | ✅ routed via Compose permission UI | `:401–433` |
| Notifications | ❌ non-camera/mic resources always `request.deny()` | `:406–408` |
| File chooser (input[type=file]) | ✅ basic; no camera-capture option, no default `image/*` handling | `:346–369` |
| Popups | ✅ block→route, or real new session via `onCreateWindow` | `:326–344` |
| Fullscreen video | ✅ `onShowCustomView`/`onHideCustomView` | `:313–324` |
| Downloads | ✅ `setDownloadListener` → `DownloadForegroundService` | `:200–208` |
| SSL errors | ⚠️ hard-cancel; no user bypass (self-signed boxes, legacy certs) | `:281–287` |
| Load errors | ✅ mapped & surfaced (`onReceivedError`/`onReceivedHttpError`) | `:268–279` |
| Renderer crash | ❌ reload of **same** WebView (contrary to docs), no crash-limit guard, no error UI fallback | `:289–300` |
| Private mode | ⚠️ only login vault excluded; **cookies/DOM-storage/history shared** with normal mode (CookieManager is process-global), no cleanup hook on close | `WebViewBrowserEngine.kt:53`, `close()` |
| Safe Browsing | ⚠️ untouched (default state applies; no custom interstitial, no proceed path) | — |
| JS dialogs | ⚠️ system WebView dialogs (alert/confirm/prompt), not themed | — |
| Remote debugging | ⚠️ never enabled (even in debug builds) — hurts QA | — |
| WebView version | ❌ no version read/telemetry → no "WebView outdated" detection on old TVs | — |

Cleartext (§ the "NOT PERMITTED" error): `app/src/main/AndroidManifest.xml:24` already sets `android:usesCleartextTraffic="true"`, so http:// main-frame loads are allowed. Any residual `ERR_CLEARTEXT_NOT_PERMITTED` comes from redirects back to http, localhost/IP literals (platform-enforced), or subresources on systems that ignore the flag — treat via HTTPS-upgrade + error-page UX, not more manifest flags.

---

## 2. Why sites fail — the real causes (ranked)

### P0 blockers (fix these; each is tiny)

**B1 — UA spoofing kills UA-CH and looks stale.**
- WebView only sends low-entropy client hints (`Sec-CH-UA`, `Sec-CH-UA-Mobile`, `Sec-CH-UA-Platform`) when the app uses the **default** UA string (confirmed by Android Developers blog, Dec 2024: "Android WebView has supported User-Agent Client Hints since version 116, but only for applications that send the default User-Agent string").
- Aurora sends a Windows-desktop Chrome/120 string → zero `Sec-CH-UA` headers → fingerprint = "unknown browser with mismatched platform". Cloudflare/Google/Netflix-class fingerprinting treats this as suspicious.
- Default UA is also **auto-renewed** by OS WebView updates; a frozen custom UA ages out of "supported browsers" lists.
- Fix: default UA = `WebSettings.getDefaultUserAgent(context)`; desktop-mode becomes an explicit per-site toggle (it already exists — just stop pre-empting it, remove `.takeUnless { settings.userAgentValue... }` guard problem, see §5 #1).

**B2 — `shouldInterceptRequest` re-fetch (TLS fingerprint + cookies + POST).**
- Re-fetching through `java.net.HttpURLConnection` presents a completely different TLS client fingerprint (JA3/JA4 — Java's stack vs Chromium's) and does **not** attach the WebView cookie jar (`CookieManager` is not automatically applied to `HttpURLConnection`) → sessions break on the 10 hard-coded streaming hosts; body-less POSTs break APIs.
- Android docs also state that **modifying request headers and returning null** from `shouldInterceptRequest` has no effect — so header-based UA/CH fixes are impossible without re-fetching, which is worse.
- Fix: delete the interception block (or keep a documented, opt-in, main-frame-only version with `CookieManager` sync — not recommended). Native network stack + default UA is the robust path.

**B3 — Third-party cookies default-off.**
- Apps targeting LOLLIPOP+ default `CookieManager.setAcceptThirdPartyCookies` to **false**. Sites with embedded sign-in (oAuth iframes), CDN widgets, chat/comments, and social logins degrade or break entirely.
- Fix: per-session `view.setAcceptThirdPartyCookies(true)` tied to a user setting ("Third-party cookies", default ON for compat / OFF for privacy-hardened mode).

**B4 — Renderer-crash recovery reuses a dead WebView.**
- `onRenderProcessGone` → the given WebView **cannot be used again**; official guidance: remove from hierarchy, `destroy()`, recreate, clear references; guard against repeated crashes of the same URL; defer recovery while backgrounded.
- Fix: destroy+recreate path, per-session crash counter (max ~3 → friendly error UI), pending-recovery on resume, and log `detail.didCrash()` (OOM vs kill) into telemetry.

**B5 — Geolocation dead.**
- `setGeolocationEnabled(true)` is missing, and manifest lacks `ACCESS_COARSE_LOCATION`/`ACCESS_FINE_LOCATION` → every maps/location site silently fails (our `onGeolocationPermissionsShowPrompt` never fires).
- Fix: enable in `configureView`, add manifest permissions, route runtime request through the existing Compose permission flow.

**B6 — WebAuthn/passkeys unsupported + no escape hatch.**
- WebAuthn is not implemented in Android WebView (per MDN compatibility: "Android WebView None"; Google's own decision).
- Fix: feature-detect in Document-Start JS → offer **"Open in Chrome"** via Chrome Custom Tabs (or `ACTION_VIEW`) for passkey/unsupported flows; also expose CCT as a general per-site "Open in browser" action.

### P1 gaps (needed for real-browser parity)

**B7 — Notifications:** `onPermissionRequest` denies everything except camera/mic; runtime `POST_NOTIFICATIONS` never requested. Note: **Web Push delivery is not supported in WebView** (service workers exist, but background push receipt does not) — keep scope honest: support site notification *display* while page is alive + direct the user to the external browser for true push; do not promise FCM-level push without a native architecture.

**B8 — SSL hard-cancel:** blocked sites show an error with no recourse (`:281`). Provide "Proceed anyway (warning)" — cart/legacy-device use cases.

**B9 — Error-page UX:** errors print `WebView`-default or Composed text; for "Not permitted"-class failures (cleartext redirects, blocked-by-client, DNS, 4xx/5xx) render a real error card: error code humanized (`WebViewMappings.describeError`), retry, https-rewrite attempt (e.g. `http://x` → `https://x`), "Open in Chrome".

**B10 — Private mode leaks:** close() must clear session cookies + DOM storage + cache + form data created during the session (best-effort, global-level clear is acceptable and standard for WebView apps; true per-session isolation needs `WebView.setDataDirectorySuffix` which is **process-scoped, cannot switch per-session in one process** — document this).

**B11 — External schemes:** `shouldOverrideUrlLoading` currently returns false for everything → `intent://`, `tel:`, `mailto:`, `market:`, `geo:`, `blob:` mis-handled. Route non-http(s) schemes to system handlers; never auto-open `intent:`.

**B12 — WebView version radar:** read `WebView.getCurrentWebViewPackage()` version; if major < ~115 (feature cliff: reduced-UA era services) show "Update WebView" prompt (Play Store deep link). Old TV WebViews are the biggest silent breakage source.

---

## 3. The "Chrome mimicry" question — what we can and can't do

| Technique | Feasible in WebView? | Notes |
|---|---|---|
| Custom UA string | ✅ | Any string via `setUserAgentString`; **but** client hints stop being sent — makes spoofing counterproductive (§B1) |
| `Sec-CH-UA` / `Sec-CH-UA-Mobile` / `Sec-CH-UA-Platform` | ❌ | Forbidden request headers + WebView gates them behind default UA; cannot be injected from app code or `shouldInterceptRequest` (returning null without re-fetch has no header effect) |
| TLS/JA3-JA4 fingerprint | ❌ — and re-fetching makes it *worse* | Chromium's network stack is fixed in WebView; `HttpURLConnection` re-fetch advertises Java's TLS ⇒ more bot-detection, not less |
| `wv` token removal | ✅ via custom UA | Tempting but again kills UA-CH; a "hide webview" UA is the legacy workaround — prefer default UA + CCT fallback |
| User-Agent reduction (Android 16/17+) | N/A | OS now ships a reduced default UA (`Linux; Android 10; K`, `Chrome/125.0.0.0`+); it is **kept current by OS updates** — another reason to stay on default |

**Consequence:** the correct strategy is *not* full Chrome impersonation (technically impossible), but **"trust the platform's own identity"**: default UA (auto-updating, with client hints) + explicit desktop-mode toggle + honest error/fallback UX + one-tap "Open in Chrome" (CCT) for sites that refuse WebView. This is exactly how mainstream WebView-based TV browsers maximize site reach.

---

## 4. Stability & crash prevention

1. **Renderer process (`onRenderProcessGone`) — rewrite per official guidance:**
   - Remove view from hierarchy → `destroy()` → `webView = null` → recreate + restore URL.
   - Crash counter per session; ≥3 crashes for same URL → error UI, no auto-loop.
   - If app is backgrounded at crash time, defer recovery to `onResume`.
   - Log `didCrash()` (true = crash, false = system kill) + URL to telemetry.
2. **Memory:** each live WebView can cost ~50–100 MB. Existing sleep/discard lifecycle is right — extend: `onTrimMemory` passthrough (call `WebView.onPause()`/release refs aggressively), cap concurrent *created* WebViews (create-on-focus), and promote the active tab with `WebView.setRendererPriorityPolicy(RENDERER_PRIORITY_IMPORTANT, ...)` so Android doesn't kill the foreground renderer first on low-RAM TVs.
3. **Renderer unresponsiveness:** attach `WebViewRenderProcessClient` (API 29+) to surface `onRenderProcessUnresponsive` / `onRenderProcessResponsive` → "Page is not responding — wait / stop" UI instead of frozen-frame confusion.
4. **Cold start:** first WebView inflation is slow — create the first session eagerly at app start (swap in when user opens a tab); keep global `WebView.setDataDirectorySuffix` **unset** (single process = default dir; document for future multi-process).
5. **Crash telemetry:** WebView renderer kills don't appear in Play Console crash reports unless `onRenderProcessGone` handles them — the fix in (1) also restores crash visibility.

---

## 5. Feature roadmap (implementation order)

### Phase 1 — "Open almost everything" (P0, ~days)

| # | Change | Where |
|---|---|---|
| 1 | Default UA = `WebSettings.getDefaultUserAgent()` (build the mobile UA at session start); persist user's desktop-mode per-site; remove early-return so desktop toggle works | `WebViewBrowserSession.configureView`, `setDesktopMode` |
| 2 | Delete `shouldInterceptRequest` re-fetch hack (keep method only if we adopt main-frame-default-UA header policy — recommend deletion) | `:225–251`, `:440–453` |
| 3 | `view.setAcceptThirdPartyCookies(enabled)` from settings (default ON); flush via `CookieManager.flush()` on session close | `configureView`, `close()` |
| 4 | Rewrite `onRenderProcessGone` (destroy+recreate, guards, error UI, resume-deferred recovery) | `:289–300` |
| 5 | `setGeolocationEnabled(true)`; manifest `ACCESS_COARSE_LOCATION`+`ACCESS_FINE_LOCATION`; runtime request via existing flow; persist per-site | `configureView`, `app/AndroidManifest.xml`, `onGeolocationPermissionsShowPrompt` |
| 6 | Notifications: handle `RESOURCE_NOTIFICATIONS` → runtime `POST_NOTIFICATIONS` → grant `request.grant(resources)`; document Web-Push limitation; show "Open in Chrome" for PWA push sites | `onPermissionRequest`, `MainActivity` |
| 7 | External schemes (`tel:`, `mailto:`, `geo:`, `market:`, `intent:` policy, `blob:`) in `shouldOverrideUrlLoading`; never auto-follow `intent:` | client |
| 8 | SSL bypass dialog (warn + proceed) + "https upgrade" retry on cleartext-class errors | `onReceivedSslError`, error state |
| 9 | Error-page card (code→message, retry, https-attempt, Open-in-Chrome CCT) | UI + `WebViewMappings.describeError` expansion (add `ERROR_UNSPECIFIED`/code passthrough for readable 4xx/5xx) |
| 10 | Private-mode cleanup on close: `removeAllCookies`, `WebStorage.deleteAllData()`, `clearCache(true)`, `clearHistory`, form data | `close()` (private session only) |
| 11 | WebView version radar (`WebViewCompat.getCurrentWebViewPackage`) → update prompt if major < 115; show version in About | engine init + `AboutSettings` |
| 12 | Safe Browsing: explicit `setSafeBrowsingEnabled(true)` per session + custom `onSafeBrowsingHit` UI (block / proceed / back) + `WebViewCompat.setSafeBrowsingAllowlist` for known-safe domains | client + UI |
| 13 | JS dialogs (alert/confirm/prompt) via Compose overlays (themed, D-pad friendly) | `WebChromeClient` |
| 14 | `setTextZoom` hooked to accessibility/largerUI setting (TV readability) | `configureView` + settings |

### Phase 2 — Browser-feature parity (P1)

15. **File chooser polish** — camera capture option, "Any file" fallback (`*/*`), multi-file reality check on OEMs; keep ValueCallback lifecycle (cancel on rotation).
16. **Downloads** — `blob:` URL downloads (JS bridge → fetch blob → save); PDF: send to system PDF viewer / in-app `PdfRenderer`; MIME-validate to avoid mobile malware bait (already partially done via DownloadManager).
17. **Open in Chrome / share** — per-site and from menu via CCT (`ChromeCustomTabsIntent`) — also the WebAuthn/passkey escape hatch (§B6).
18. **Site data manager** — per-domain cookies/DOM storage clear (extends existing "Clear browsing data").
19. **Autofill** — keep vault JS-capture; also let Android Autofill framework service password fields (it works on WebView when the page uses proper form markup) — marketing as "works with your password manager".
20. **Media/TV fixes** — `KeepScreenOn` during fullscreen video; `zOrderOnTop` workaround for overlay video on some SoCs; exit-fullscreen on Back; audio focus handling.
21. **Remote debugging** — `WebView.setWebContentsDebuggingEnabled(true)` in debug builds only (never release).
22. **Focus/pointer polish** — suppress WebView scrollbars/overscroll glow for TV; `requestFocus()` on session attach so D-pad works immediately.
23. **Diagnostics** — per-session counters (loads, errors by code, renderer kills) surfaced in the existing Performance Center; helps site-compat triage.

### Phase 3 — Stretch (P2)

24. Content filtering (EasyList-style host list → `shouldInterceptRequest` returns empty response when enabled) — CPU cost per request; keep opt-in; re-validate every WebView major.
25. Reader-mode/AMP smoothing; per-site "force desktop UA" presets; translation (web page via JS or external service).
26. Multi-process architecture for true per-session isolation (`WebView.setDataDirectorySuffix` is process-scoped — would require a renderer-in-service redesign; document costs before attempting).
27. Predictive pre-connect / prefetch of top home tiles (thumbnails already cached; add HTTP warmup only if data-saver allows).

---

## 6. Testing matrix (acceptance checklist)

**Feature probes (open once, record result):**
- UA/CH: `httpbin.org/headers` (expect `Sec-CH-UA*` present on default UA), `browserleaks.com/tls`, `whatismybrowser.com`
- Crashing: `chrome://crash` inside a WebView session → expect auto-recover to error UI (not app death) — verify per P0#4
- Geolocation: `google.com/maps` → permission card → pin drop
- Camera/mic: `discord.com` or `meet.google.com` room → grant → green light
- Notifications: a PWA with notifications (e.g. `webpushtest.app`-style) → confirm display-when-open behavior + documented limit
- WebAuthn: `webauthn.io` → "Open in Chrome" fallback appears (never a dead end)
- Streaming: Paramount+, Hotstar, Disney+, Peacock, YouTube, Netflix (DRM) — after P0#2/#1 (no interception, default UA)
- Embeds: a site with Google oAuth iframe / social widgets → works with 3PC on (P0#3)
- Forms/sign-in with vault: non-public test form → captured, not double-filled

**Device matrix:** at least one modern phone (WebView ~now), one Android TV box (may carry *old* WebView — the radar in P0#11 is mandatory here), one low-RAM device (renderer-kill path).

**Regression guard:** keep the 70-unit suite green (mappers/`describeError` expansion must stay pure); add JVM tests for URL scheme routing + https-upgrade logic + error-description table.

---

## 7. Priority plan (effort estimate)

| Effort | Items | Owner |
|---|---|---|
| Hours | B1 UA default, B2 remove interception, B3 3PC, B5 geolocation, B11 schemes, B12 version radar | engine/webview |
| ~1 day | B4 renderer recovery rewrite, B9 error UX, B8 SSL bypass, B10 private cleanup | engine + UI |
| 2–3 days | B6/B7 CCT fallback + notifications, P1 15–19 | engine + UI |
| Week+ | P1 20–23, P2 24–27 | engine + UI + design |

**Sequencing advice:** Phase 1 in one sitting (all changes are localized to `WebViewBrowserSession.kt`, `WebViewBrowserEngine.kt`, `app/AndroidManifest.xml`, and one UI error/permission surface); ship decoder APK to the test matrix; then Phase 2.

---

## 8. Key sources

- Android Developers Blog — *User-Agent Reduction on Android WebView* (Dec 2024): default UA reduced from Android 16/17; UA-CH **only with default UA**; custom `setUserAgentString` keeps working (no CH). https://android-developers.googleblog.com/2024/12/user-agent-reduction-on-android-webview.html
- Privacy Sandbox — *User-Agent Reduction on Android WebView* (same content). https://privacysandbox.google.com/blog/user-agent-reduction-webview-update
- WICG ua-client-hints — WebView client-hints behavior (`Sec-CH-UA: "Android WebView"`…). https://github.com/WICG/ua-client-hints/issues/280
- MDN — `Sec-CH-UA` (forbidden header, low-entropy). https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-CH-UA
- Android Developers — *Network security configuration* (cleartext policy since API 28). https://developer.android.com/privacy-and-security/security-config
- Chromium issue — cleartext-to-localhost enforced by **platform**, not WebView. https://issues.chromium.org/issues/378835488
- Android Developers — *Handle WebView termination* (`onRenderProcessGone`: destroy + recreate, no reuse, background deferral, repeated-crash caution, Play-Console visibility gap). https://developer.android.com/develop/ui/views/layout/webapps/handle-termination
- Android Docs — `WebViewRenderProcess` (if any associated WebView doesn't handle termination, the app process dies). https://developer.android.com/reference/android/webkit/WebViewRenderProcess
- Chromium issue #325120865 — official stance: given WebView is unusable after renderer death (reload-appears-to-work is explicitly "Intended Behavior" — don't). https://issues.chromium.org/issues/325120865
- Android Docs — `CookieManager.setAcceptThirdPartyCookies` (per-WebView; LOLLIPOP+ default = disallow). https://developer.android.com/reference/android/webkit/CookieManager
- Chromium WebView docs — Safe Browsing (GMS blocklists, interstitial, `onSafeBrowsingHit`). https://chromium.googlesource.com/chromium/src/+/master/android_webview/browser/safe_browsing/README.md
- MDN — Web Authentication API (Android WebView: **None**). https://developer.mozilla.org/en-US/docs/Web/API/Web_Authentication_API
- Blink-dev + SO — WebAuthn intentionally not implemented in WebView; RFC 8252 (OAuth 2.0 for native apps) recommends system browser/CCT redirect. https://stackoverflow.com/questions/56258147
- Code2Native — Web View push notifications: Web Push not supported in WebView; FCM + JS bridge is the native route. https://code2native.com/blog/webview-push-notifications-android
- Android Docs — `WebView.setDataDirectorySuffix` (API 28, process-scoped, must be called before any WebView usage, one dir per process). https://developer.android.com/reference/android/webkit/WebView#setDataDirectorySuffix(java.lang.String)
- Cloudflare community — WebView + Cloudflare "checking connection" loops on custom-UA/intercepted-stack setups. https://community.cloudflare.com/t/android-system-webview-issue-with-some-sites/484530
- Android Developers — `WebViewCompat.setSafeBrowsingAllowlist` (androidx.webkit). https://developer.android.com/reference/androidx/webkit/WebViewCompat
- Android Developers — Notification runtime permission (Android 13+, `POST_NOTIFICATIONS`). https://developer.android.com/develop/ui/compose/notifications/notification-permission

---

*Audit is grounded in the actual codebase state (engine/webview + app manifest) as of 2026-08-09; all "verify on device" items below require the §6 matrix before being marked done.*