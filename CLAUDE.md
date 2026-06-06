# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Android – assemble a specific flavor+buildType
./gradlew :app:assembleDevDebug
./gradlew :app:assembleStagingDebug
./gradlew :app:assembleProductionRelease

# Verify all 6 flavor variants build cleanly
./verify_flavors.sh

# Build the shared KMP module (also generates the iOS xcframework)
./gradlew :shared:assemble

# iOS xcframework for Xcode (run from repo root)
./gradlew :shared:assembleSharedKitXCFramework

# Lint
./gradlew :app:lint
./gradlew :shared:lint

# Tests
./gradlew :app:testDevDebugUnitTest          # unit tests for dev flavor
./gradlew :shared:androidHostTest            # shared module host tests
./gradlew :shared:iosSimulatorArm64Test      # shared module iOS simulator tests (macOS only)
```

**Gradle credentials** – the MineSec private Maven registry requires `MINESEC_REGISTRY_LOGIN` and `MINESEC_REGISTRY_TOKEN` in `~/.gradle/gradle.properties` (never in the project file). A placeholder is committed in `gradle.properties`; override it locally.

## Product Flavors

Three flavors map to three backends:

| Flavor | `ENVIRONMENT` | Base URL |
|---|---|---|
| `dev` | `DEV` | `https://api-dev.paymentoptions.com/api/v1/` |
| `staging` | `STAGING` | `https://api-staging.paymentoptions.com/api/v1/` |
| `production` | `PROD` | `https://api.paymentoptions.com/api/v1/` |

`BuildConfig.CONFIG_BASE_URL` carries the compile-time default. At runtime `ConfigurationManager` (in `:shared`) may override this from `AppStorage.baseUrl` (stored after the first splash-screen config fetch).

## Module Architecture

### `:app` — thin Android host

The Android application shell. After the Compose Multiplatform migration its only responsibilities are:

- **`MainActivity`** – initialises `AppStorage` with `EncryptedSharedPreferences`, sets `currentActivity`, calls `setContent { App(buildTimeBaseUrl) }`.
- **`ClientApp`** – `Application` class: initialises `AppLogger` and the MineSec Headless SDK (`HeadlessSetup.initSoftPos`).
- **`ClientHeadlessImpl`** – customises the MineSec `HeadlessActivity` payment UI.
- **Android-only dependencies** kept here: MineSec Headless SDK, Firebase Analytics, vico/ycharts/MPAndroidChart charts, ZXing QR generation, Log4j, WorkManager, Accompanist, `GeoRestrictionManager`.

### `:shared` — Compose Multiplatform (Android + iOS)

All business logic **and** UI live here. Source sets:

| Source set | Purpose |
|---|---|
| `commonMain` | All shared code – UI, network, storage, auth |
| `androidMain` | Android-specific `expect/actual` impls (biometrics, FCM, encrypted storage) |
| `iosMain` | iOS-specific `expect/actual` impls (LAContext, NSUserDefaults, `ComposeUIViewController`) |

iOS consumes the module as `sharedKit.xcframework`. The Swift entry point is `iosApp/iosApp/ContentView.swift`, which wraps `MainViewControllerKt.MainViewController()` in a `UIViewControllerRepresentable`.

## Key Patterns

### Storage — `AppStorage`
`AppStorage` is a singleton wrapping `com.russhwolf.settings.Settings`. Call `AppStorage.init(settings)` once at startup before any read/write. On Android the backing is `EncryptedSharedPreferences` (via `createEncryptedSettings(context)`); on iOS it is `NSUserDefaults`. Access is via typed properties (`AppStorage.accessToken`, `AppStorage.deviceNumber`, etc.). **Never use `DPSharedPreferences` in new code** – it is the legacy Android-only predecessor.

### Networking — Ktor (`KtorClient`)
`KtorClient.instance` is a lazy-initialised `HttpClient`. All endpoint functions are top-level suspend functions in `shared/.../network/endpoints/`. They use `ConfigurationManager.url("path")` to resolve URLs and `applyDaspayHeaders()` to attach auth headers. The `Auth` plugin handles 401 refresh automatically via `TokenRefreshService`. **Never use `RetrofitClient` in new code.**

### Auth events — `AuthEventManager`
Emits `AuthEvent.RequireReAuthentication` or `AuthEvent.RequireManualSignIn` into a `SharedFlow`. `Navigator` collects these and clears the back stack. Emit from any layer that detects an expired/invalid session.

### Platform abstractions — `Platform.kt`
`expect` declarations for biometrics (`getBiometricAuthenticator()`), push token (`getPushToken()`), device info, logging, and orientation lock. On Android, `currentActivity` (a `var` in `Platform.android.kt`) must be set in `MainActivity.onCreate` / cleared in `onDestroy`. URL encoding helpers `urlEncode()`/`urlDecode()` replace `java.net.URLEncoder/URLDecoder`.

### Navigation
`Navigator.kt` uses Jetpack Navigation Compose (JetBrains CMP fork: `org.jetbrains.androidx.navigation:navigation-compose`). Routes are defined in `Screens.kt` as `sealed class` objects. `ScreenDelegates.kt` contains placeholder composables; replace each `LoadingPlaceholder()` with the real screen composable as screens are migrated into `commonMain`.

### Theme
`AppTheme` is a `@Composable` that loads the Inter font family from CMP resources (`shared/src/commonMain/composeResources/font/`) and provides four `CompositionLocal`s: `LocalAppColorScheme`, `LocalAppTypography`, `LocalAppShape`, `LocalAppSize`. Access via `AppTheme.colorScheme`, `AppTheme.typography`, etc.

## Screen Migration (In Progress)

Screens currently still live in `app/src/main/java/.../ui/composables/screens/`. To migrate a screen to `commonMain`:

1. Move the file to `shared/src/commonMain/kotlin/.../ui/screens/`.
2. `DPSharedPreferences.getXxx(context)` → `AppStorage.xxx`.
3. `Gson().fromJson/toJson` → `Json { ignoreUnknownKeys = true }.decodeFromString/encodeToString`.
4. `java.net.URLEncoder/Decoder` → `urlEncode()`/`urlDecode()`.
5. `RetrofitClient.getApi(context).endpoint(headers, …)` → the corresponding Ktor function in `network/endpoints/`.
6. `LocalContext.current` for Android-only features (biometrics, printing, NFC) → `expect/actual` in `Platform.kt`.
7. Update the matching function in `ScreenDelegates.kt` to call the real composable.

## Data Models

All API models are `@Serializable` data classes in `shared/.../network/DataModels.kt`. The project uses `kotlinx.serialization` throughout — do not introduce Gson in `commonMain`. Note: `PayByLinkResponseData.ReturnUrl` is `JsonObject` (kotlinx), not the legacy `org.json.JSONObject`.

## iOS Xcode Project

Open `iosApp/iosApp.xcodeproj` in Xcode. The project consumes `sharedKit.xcframework`; build the framework first with `./gradlew :shared:assembleSharedKitXCFramework`. The app entry point is `iOSApp.swift` → `ContentView.swift` → `MainViewControllerKt.MainViewController()`.
