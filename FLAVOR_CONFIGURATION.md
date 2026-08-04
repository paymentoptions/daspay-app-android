# Flavor Configuration Setup

This document describes the flavor configuration setup for the Daspay Android application.

## Overview

The app is configured with **three product flavors** for different environments:
- **dev** (Development) - Default flavor
- **staging** (Staging)
- **production** (Production)

Each flavor automatically fetches and stores the appropriate base API URL from the backend configuration service.

## Flavors Configuration

### 1. Dev (Development)
- **Application ID**: `com.paymentoptions.pos`
- **Environment**: `DEV`
- **Config Base URL**: `https://api-dev.paymentoptions.com/api/v1/`
- **Version Suffix**: `-dev`

### 2. Staging
- **Application ID**: `com.paymentoptions.pos`
- **Environment**: `STAGING`
- **Config Base URL**: `https://api-staging.paymentoptions.com/api/v1/`
- **Version Suffix**: `-staging`

### 3. Production
- **Application ID**: `com.paymentoptions.pos`
- **Environment**: `PROD`
- **Config Base URL**: `https://api.paymentoptions.com/api/v1/`
- **Version Suffix**: None

## How It Works

### 1. Build Configuration
Each flavor is configured in `app/build.gradle.kts` with:
```kotlin
buildConfigField("String", "ENVIRONMENT", "\"DEV\"")
buildConfigField("String", "CONFIG_BASE_URL", "\"https://api-dev.paymentoptions.com/api/v1/\"")
```

### 2. Configuration Manager
The `ConfigurationManager` handles the initialization of app configuration:

- **On First Launch**: When the user signs in, the app calls the config API (`/dasconfig/daspay-configuration?appenv={ENVIRONMENT}`) to fetch the base URL for the current environment.
- **Storage**: The fetched base URL is stored in `DPSharedPreferences` using `storeBaseUrl()`.
- **Subsequent Launches**: The app checks if a base URL is already saved. If yes, it uses the saved URL; otherwise, it downloads the config again.

### 3. Retrofit Client
The `RetrofitClient` dynamically uses the base URL from shared preferences:
```kotlin
val savedBaseUrl = DPSharedPreferences.getBaseUrl(context)
val effectiveBaseUrl = if (!savedBaseUrl.isNullOrEmpty()) {
    savedBaseUrl
} else {
    BuildConfig.CONFIG_BASE_URL
}
```

### 4. Configuration Flow

#### Sign-In Flow:
1. User enters credentials and signs in
2. Auth tokens are saved in `DPSharedPreferences`
3. `ConfigurationManager.initializeConfig(context)` is called
4. Config API is called with the current environment (DEV/STAGING/PROD)
5. Base URL from response is saved in shared preferences
6. RetrofitClient is reset to use the new base URL
7. User proceeds to Token screen

#### Splash Screen (Already Logged In):
1. App checks if user is authenticated
2. If authenticated, `ConfigurationManager.initializeConfig(context)` is called
3. If base URL exists in preferences, it's used directly
4. If not, config is downloaded from API
5. User proceeds to Dashboard

## Building Different Flavors

### Command Line
```bash
# Build Dev Debug
./gradlew assembleDevDebug

# Build Dev Release
./gradlew assembleDevRelease

# Build Staging Debug
./gradlew assembleStagingDebug

# Build Staging Release
./gradlew assembleStagingRelease

# Build Production Debug
./gradlew assembleProductionDebug

# Build Production Release
./gradlew assembleProductionRelease
```

### Android Studio
1. Open **Build Variants** panel (View > Tool Windows > Build Variants)
2. Select the desired variant (e.g., `devDebug`, `stagingRelease`, etc.)
3. Build/Run the app

## API Endpoints

### Config Download Endpoint
- **Path**: `/dasconfig/daspay-configuration`
- **Method**: GET
- **Query Parameter**: `appenv` (DEV, STAGING, or PROD)
- **Response**: 
```json
{
  "statusCode": 200,
  "message": "Success",
  "success": true,
  "data": [
    {
      "id": 1,
      "appENV": "DEV",
      "baseAPIURL": "https://api-dev.paymentoptions.com/api/v1/",
      "registryLogin": "...",
      "registryToken": "..."
    }
  ]
}
```

## DPSharedPreferences Methods

### Store Base URL
```kotlin
DPSharedPreferences.storeBaseUrl(context: Context, baseAPIURL: String)
```

### Retrieve Base URL
```kotlin
val baseUrl: String? = DPSharedPreferences.getBaseUrl(context: Context)
```

## ConfigurationManager Methods

### Initialize Config
```kotlin
suspend fun initializeConfig(context: Context): Boolean
```
- Checks if base URL is saved in preferences
- If not saved, downloads config from API
- Returns `true` if successful, `false` otherwise

### Refresh Config
```kotlin
suspend fun refreshConfig(context: Context): Boolean
```
- Forces a refresh of configuration from API
- Updates stored base URL
- Returns `true` if successful, `false` otherwise

### Get Current Environment
```kotlin
fun getCurrentEnvironment(): String
```
- Returns the current environment name (DEV, STAGING, or PROD)

### Get Effective Base URL
```kotlin
fun getEffectiveBaseUrl(context: Context): String
```
- Returns the base URL being used (from preferences or BuildConfig)

## Firebase Configuration

The `google-services.json` file has been updated to include all three package names:
- `com.paymentoptions.pos` (production)
- `com.paymentoptions.pos` (dev)
- `com.paymentoptions.pos` (staging)

## Testing

### To test the flavor setup:
1. Build the dev variant: `./gradlew assembleDevDebug`
2. Install on device/emulator
3. Sign in with credentials
4. Check logs for:
   - `Config downloaded successfully for DEV - Base URL: ...`
   - `Creating Retrofit instance with base URL: ...`

### To verify the environment:
Check the app logs on startup:
```
App started - Build variant: debug, Flavor: dev
```

## Troubleshooting

### Issue: Config download fails
- **Cause**: User not authenticated or invalid credentials
- **Solution**: Ensure user is signed in before config download is attempted

### Issue: Wrong base URL being used
- **Cause**: Cached base URL from different environment
- **Solution**: Clear app data or call `ConfigurationManager.refreshConfig(context)`

### Issue: Build fails with google-services.json error
- **Cause**: Package name not registered in Firebase
- **Solution**: Add the package name (with flavor suffix) to `google-services.json`

## Notes

- The **dev flavor is set as the default** during development
- Base URL is fetched **after authentication** to ensure proper API access
- The system falls back to `BuildConfig.CONFIG_BASE_URL` if no saved URL exists
- RetrofitClient automatically recreates the API service when base URL changes

