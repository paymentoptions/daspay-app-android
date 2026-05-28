# AGENTS.md — Daspay Android

## Project Overview
Android POS (Point-of-Sale) payment app for merchants. Kotlin-first, Jetpack Compose UI. Includes a **Kotlin Multiplatform (KMP) `shared` module** targeting Android + iOS, though it is currently a scaffold with no shared business logic. The primary app module is `app/`.

---

## Build Commands

```bash
# Default development build (use this for local iteration)
./gradlew assembleDevDebug

# Other flavors
./gradlew assembleStagingDebug
./gradlew assembleProductionRelease

# Run unit tests
./gradlew test

# Run connected tests
./gradlew connectedAndroidTest
```

- **Signing**: Release/debug signing reads from `.sign/keystore.properties` (not committed). A warning is logged at build time if it is missing.
- **Dependency versions**: Managed via the version catalog at `gradle/libs.versions.toml`. Add new deps there first, then reference via `libs.*` in build scripts.

---

## Architecture & Key Layers

### 1. Three Product Flavors (`app/build.gradle.kts`)
| Flavor | `ENVIRONMENT` | `CONFIG_BASE_URL` |
|---|---|---|
| `dev` | `DEV` | `https://api-dev.paymentoptions.com/api/v1/` |
| `staging` | `STAGING` | `https://api-staging.paymentoptions.com/api/v1/` |
| `production` | `PROD` | `https://api.paymentoptions.com/api/v1/` |

**Critical flow**: `BuildConfig.CONFIG_BASE_URL` is only the *bootstrap* URL. On first sign-in, `ConfigurationManager.initializeConfig(context)` calls `GET dasconfig/daspay-configuration?appenv={ENVIRONMENT}`, saves the returned `baseAPIURL` into `DPSharedPreferences`, then calls `RetrofitClient.reset()`. All subsequent API calls use the persisted URL. Do not hardcode the base URL anywhere else.

### 2. Network Layer (`app/src/main/java/com/paymentoptions/pos/services/apiService/`)
- **`APIService.kt`**: Retrofit interface + `RetrofitClient` singleton. `RetrofitClient.getApi(context)` lazily creates/recreates the instance when the base URL changes.
- **`RequestHeaders.kt`**: Headers are constructed via dedicated functions (`generateRequestHeader`, `generatePaymentRequestHeader`, etc.). The `x-api-key` and `x-authorization` values are intentional static keys included in every request — do not move them to secrets management.
- **`TokenAuthenticator.kt`**: OkHttp `Authenticator` handles 401 responses by synchronously refreshing the JWT via `TokenRepository`, then retries. Max 2 retries to prevent loops.
- **`endpoints/`**: Each API operation is a standalone top-level function in its own file (e.g., `Payment.kt`, `Refund.kt`). New endpoints go here.

### 3. Storage (`device/DPSharedPreferences.kt`)
Single `object` wrapping `EncryptedSharedPreferences` (AES256-GCM). Recovers automatically from corruption (wipes and recreates). Key stored items: `auth_details`, `device_config`, `BaseAPIURL`, `cart`, `fcm_token`, `biometrics`.

### 4. Navigation (`ui/composables/navigation/`)
- Routes defined as `sealed class Screens` objects in `Screens.kt`.
- Routes with parameters (e.g., `TransactionDetails`) pass data as **URL-encoded JSON strings** in the route path — not `savedStateHandle` bundles. Parse with `Gson` + `URLDecoder` (see `Navigator.kt`).
- Global auth events (session expiry) are dispatched via `AuthEventManager.authEvents` flow and observed in `Navigator`.

### 5. Payment Flows (`ui/composables/screens/_flow/`)
Multi-step payment flows are self-contained composables driven by stage enums (e.g., `ReceiveMoneyFlowStage`). Supported payment types:
- **SOFTPOS** — tap-to-pay via Minesec Headless SDK (`ClientHeadlessImpl` extends `HeadlessActivity`)
- **QR** — QR code charge via `PayByQr` endpoint
- **PBL** — Pay-by-Link via `PayByLink` endpoint

Payment method availability per device is read from `DPSharedPreferences.getAvailablePaymentsList()`.

### 6. Logging (`logger/AppLogger.kt`)
Use `AppLogger` (not `Log.d`). Logs go to both Logcat (tag `DASPAY`) and a Log4j file in the app cache dir. Exportable from the `SendLogs` screen. Always log via `AppLogger.debug/info/warn/error(...)`.

---

## Project-Specific Conventions

- **Screen structure**: Each screen has a main `*Screen.kt` file and often a `BottomSectionContent.kt` (content for a bottom sheet/panel). Keep these separate.
- **Test/dev screens**: Prefixed with `_test/` in the screen directory (e.g., `_test/fcmtoken/`). These are feature-flagged debug utilities.
- **Multi-step flows**: Prefixed with `_flow/` in the screen directory.
- **Minesec SDK**: The `headless-stage` AAR (`app/libs/`) is the tap-to-pay SDK. `ClientHeadlessImpl` customizes its UI; do not replace `HeadlessActivity` methods without reviewing Minesec integration docs.
- **Local AARs**: `app/libs/headless-stage-1.2.17-stage-release.aar` and `minehades-stage-1.10.105.12.61.aar` are vendored because the remote Maven registry was unavailable. Updating requires replacing the file and bumping the version in `libs.versions.toml`.

---

## Key Files Reference

| Purpose | Path |
|---|---|
| Flavor & build config | `app/build.gradle.kts` |
| Dependency versions | `gradle/libs.versions.toml` |
| All screens / routes | `app/src/main/java/.../navigation/Screens.kt` |
| Navigation graph | `app/src/main/java/.../navigation/Navigator.kt` |
| Retrofit client & API interface | `.../services/apiService/APIService.kt` |
| Request headers (API keys) | `.../services/apiService/RequestHeaders.kt` |
| Encrypted preferences | `.../device/DPSharedPreferences.kt` |
| Dynamic base URL logic | `.../services/apiService/ConfigurationManager.kt` |
| Tap-to-pay SDK integration | `.../ClientHeadlessImpl.kt` |
| App-wide logger | `.../logger/AppLogger.kt` |
| Flavor configuration docs | `FLAVOR_CONFIGURATION.md` |

