# Firebase Cloud Messaging test push

This prototype receives Firebase Cloud Messaging (FCM) natively on Android. It
does not use Web Push in the Nuxt WebView, because native FCM delivery is what
the later incoming-call experience requires.

## 1. Connect the Android app to Firebase

1. In the Firebase Console, create or select a project.
2. Add an Android app with the exact package name:

   ```text
   com.example.voipprototype
   ```

3. Download the generated `google-services.json` file and put it here:

   ```text
   android/app/google-services.json
   ```

4. Enable the Cloud Messaging API in the project’s **Cloud Messaging** settings
   if it is not already enabled.
5. Build, install, and open the app:

   ```powershell
   npm run android:sync
   .\android\gradlew.bat assembleDebug
   ```

6. On Android 13, accept the notification permission prompt. The FCM token is
   then shown on the app screen; copy it.

## 2. Send the quickest first test from Firebase Console

Put the app in the background, then open **Firebase Console → DevOps &
Engagement → Messaging**, create a notification campaign, choose **Send test
message**, paste the FCM token, and send it. The Android system displays
notification-payload messages that arrive while the app is backgrounded.

## 3. Send a repeatable test from the command line

There is no `firebase` CLI command that sends an arbitrary FCM HTTP v1 message.
The Firebase CLI is still useful to authenticate and select the Firebase
project; the actual send below uses the Google Cloud CLI to mint the OAuth token
that FCM HTTP v1 requires.

Install and authenticate the Firebase CLI if it is not already available:

```powershell
npm install --global firebase-tools
firebase login
firebase projects:list
```

Install the [Google Cloud CLI](https://cloud.google.com/sdk/docs/install), then
authenticate application-default credentials for the same Google account:

```powershell
gcloud auth application-default login
```

In PowerShell, replace the two placeholder values and run:

```powershell
$projectId = 'YOUR_FIREBASE_PROJECT_ID'
$deviceToken = 'PASTE_THE_TOKEN_FROM_THE_APP'
$accessToken = gcloud auth application-default print-access-token

$payload = @{
  message = @{
    token = $deviceToken
    notification = @{
      title = 'Hello from FCM'
      body = 'The Android native push handler received this test.'
    }
    android = @{
      priority = 'high'
      notification = @{ channel_id = 'push_messages' }
    }
  }
} | ConvertTo-Json -Depth 6 -Compress

Invoke-RestMethod `
  -Method Post `
  -Uri "https://fcm.googleapis.com/v1/projects/$projectId/messages:send" `
  -Headers @{ Authorization = "Bearer $accessToken" } `
  -ContentType 'application/json' `
  -Body $payload
```

A successful response contains a `name` field with the FCM message ID. If it
returns `PERMISSION_DENIED`, ensure the signed-in account can send FCM messages
for the chosen Google Cloud/Firebase project.

## Expected behavior

- **App foreground:** `AppFirebaseMessagingService` receives the message and
  posts a native notification.
- **App background:** Android/FCM posts notification-payload messages to the
  notification tray.
- **App stopped:** delivery is still handled by native FCM, subject to normal
  Android and FCM delivery constraints.

The later call implementation will send high-priority data-only messages from
the server. This hello-world uses a visible notification payload intentionally,
because it provides the clearest configuration test.
