# CLAUDE.md — android-support-integration

Drop-in Jetpack Compose support widget. Equivalent of
`ios-support-integration` for Android, and the Android arm of the
client-platform fan-out (laravel/nuxt/react/wp/slack/browser/macos/zapier).

## Layout

- `library/build.gradle.kts` — Compose BOM 2024.02, Material 3, Kotlin 1.9
- `library/src/main/java/life/ubiqui/support/`
  - `SupportConfig.kt` — apiBaseUrl, apiKey, appName, optional reporter + identimeUserId
  - `SupportClient.kt` — suspending HTTP client (HttpURLConnection, no third-party deps)
  - `SupportFAB.kt` — Composable floating action button + ModalBottomSheet form
- `library/src/main/res/values{,-es,-ja,-tl,-mi}/strings.xml` — 5 locales
- `library/src/main/AndroidManifest.xml` — INTERNET permission

## Mandatory rules

- Public package — keep API stable.
- No third-party network deps (Retrofit/OkHttp) — keep the install footprint
  minimal. HttpURLConnection is sufficient.
- All UI strings live in `strings.xml`, never inline. Five locales must
  stay in sync.
- Snake_case wire format, camelCase Kotlin types.
