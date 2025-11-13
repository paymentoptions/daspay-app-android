# Building APK in Cursor IDE

## Prerequisites

1. **Java JDK 11+** - Download from:
   - https://adoptium.net/ (Recommended - Eclipse Temurin)
   - Or Oracle JDK: https://www.oracle.com/java/technologies/downloads/

2. **Set JAVA_HOME** (Windows):
   ```powershell
   # Check if Java is installed
   java -version
   
   # If not found, set JAVA_HOME manually:
   # System Properties → Environment Variables → Add:
   # JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-11.0.x
   # Add to PATH: %JAVA_HOME%\bin
   ```

## Building APK in Cursor Terminal

### Option 1: Build Debug APK (No signing required)

```powershell
# Navigate to project directory
cd "C:\Users\ashis\OneDrive\Desktop\Project\Das Pay\daspay-app-android"

# Build debug APK
.\gradlew.bat assembleDebug

# APK will be created at:
# app\build\outputs\apk\debug\app-debug.apk
```

### Option 2: Build Release APK (Requires signing)

```powershell
# Build release APK
.\gradlew.bat assembleRelease

# APK will be created at:
# app\build\outputs\apk\release\app-release.apk
```

### Option 3: Clean and Build

```powershell
# Clean previous builds
.\gradlew.bat clean

# Then build
.\gradlew.bat assembleDebug
```

### Option 4: Install on Connected Device

```powershell
# Connect Android device via USB (with USB debugging enabled)
# Then install directly:
.\gradlew.bat installDebug
```

## Troubleshooting

### If JAVA_HOME is not set:
```powershell
# Find Java installation
where java

# Set JAVA_HOME temporarily for this session:
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-11.0.x"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Then run gradlew
.\gradlew.bat assembleDebug
```

### If Gradle sync fails:
- Check internet connection (dependencies need to download)
- Verify MineSec credentials in `gradle.properties`
- Check if Android SDK is needed (usually Gradle handles this)

### If build fails:
```powershell
# Check build output for errors
.\gradlew.bat assembleDebug --stacktrace

# Or with more details:
.\gradlew.bat assembleDebug --info
```

## APK Location

After successful build:
- **Debug APK**: `app\build\outputs\apk\debug\app-debug.apk`
- **Release APK**: `app\build\outputs\apk\release\app-release.apk`

## Limitations in Cursor vs Android Studio

- ❌ No visual UI preview
- ❌ No built-in emulator
- ❌ No visual debugging
- ❌ No APK analyzer GUI
- ✅ Can build APK via command line
- ✅ Can edit code
- ✅ Can use terminal for all operations

## Quick Start (After Java is Installed)

```powershell
# 1. Navigate to project
cd "C:\Users\ashis\OneDrive\Desktop\Project\Das Pay\daspay-app-android"

# 2. Build APK
.\gradlew.bat assembleDebug

# 3. Find your APK
# Location: app\build\outputs\apk\debug\app-debug.apk
```

