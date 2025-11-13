# Build APK Script for Cursor IDE
# Run this script after installing Java JDK 11+

Write-Host "Building APK..." -ForegroundColor Green

# Check if Java is available
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java JDK 11+ from https://adoptium.net/" -ForegroundColor Yellow
    Write-Host "And set JAVA_HOME environment variable" -ForegroundColor Yellow
    exit 1
}

# Navigate to project root
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

Write-Host "Project directory: $projectRoot" -ForegroundColor Cyan

# Clean previous builds (optional)
Write-Host "Cleaning previous builds..." -ForegroundColor Yellow
.\gradlew.bat clean

# Build debug APK
Write-Host "Building debug APK..." -ForegroundColor Yellow
.\gradlew.bat assembleDebug

# Check if APK was created
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    $apkSize = (Get-Item $apkPath).Length / 1MB
    Write-Host "`n✅ APK built successfully!" -ForegroundColor Green
    Write-Host "Location: $apkPath" -ForegroundColor Cyan
    Write-Host "Size: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
    Write-Host "`nYou can now install this APK on your Android device!" -ForegroundColor Green
} else {
    Write-Host "`n❌ APK build failed. Check the error messages above." -ForegroundColor Red
    exit 1
}

