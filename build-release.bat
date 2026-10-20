@echo off
setlocal enabledelayedexpansion

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
set "ANDROID_SDK_ROOT=C:\Users\Club\Desktop\android-sdk"
set "KEYSTORE_PASSWORD=animeworld123"
set "KEY_ALIAS=animeworld"
set "KEY_PASSWORD=animeworld123"

cd /d "C:\Users\Club\Desktop\animeworld-android-kotlin-main"

echo === Build :app:assembleRelease ===
powershell -NoProfile -Command "& { .\gradlew.bat :app:assembleRelease --stacktrace 2>&1 | Tee-Object -FilePath build.log }"

echo.
echo Attesa chiusura processi gradle...
:waitloop
powershell -NoProfile -Command "if (Get-Process -Name java -ErrorAction SilentlyContinue) { exit 1 } else { exit 0 }"
if %errorlevel%==1 (
    timeout /t 3 /nobreak >nul
    goto waitloop
)

echo Processi chiusi.
if exist "app\build\outputs\apk\release\app-release.apk" (
    echo Rinomino e sposto l'APK...
    move /y "app\build\outputs\apk\release\app-release.apk" "openanime-1.0.9.apk"
    echo APK spostato: openanime-1.0.9.apk
) else (
    echo APK non trovato in app\build\outputs\apk\release\
)

echo.
echo Build terminata. Log: build.log
pause
