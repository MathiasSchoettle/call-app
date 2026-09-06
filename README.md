# VoIP Capacitor prototype

This is the first milestone only: a Nuxt hello-world app packaged in an Android
Capacitor WebView. It deliberately contains no Firebase, WebSocket, JsSIP,
Telecom, foreground-service, or audio implementation yet. Those arrive after
the environment has been proven on a real device.

## Requirements

- Node.js 20 or later
- Android Studio with an Android 13 / API 33 SDK platform and an emulator or
  USB-debuggable Android device
- A JDK supported by the installed Android Studio (use Android Studio's embedded
  JDK when possible)

The Android wrapper compiles against API 36 because current Android tooling
expects a recent compile SDK, but its `targetSdkVersion` is explicitly pinned
to **33** (Android 13) in `android/variables.gradle`.

## Run the web app in a browser

```powershell
npm install
npm run dev
```

Open the local URL Nuxt prints in the terminal. You should see “VoIP Prototype”
and “Hello from the Capacitor WebView.”

## Build and run Android

```powershell
npm install
npm run android:sync
npm run android:open
```

The sync command statically generates Nuxt into `.output/public` and copies it
into the Android app. The final command opens the native project in Android
Studio; select a device and press Run.

To build an APK without opening Android Studio:

```powershell
.\android\gradlew.bat assembleDebug
```

The debug APK is written to
`android/app/build/outputs/apk/debug/app-debug.apk`.

## Quick verification

1. Launch the Android app on an API 33 emulator or device.
2. Confirm the title reads **VoIP Prototype**.
3. Confirm the message reads **Hello from the Capacitor WebView.**

If that works, the Nuxt build, Capacitor asset sync, and Android WebView bridge
are correctly wired for the next milestone.

## Firebase test push

Native Android Firebase Cloud Messaging handling is now included. Add your
Firebase project’s `google-services.json` to `android/app/`, rebuild the app,
and follow [the FCM test-push tutorial](docs/fcm-test-push.md).
