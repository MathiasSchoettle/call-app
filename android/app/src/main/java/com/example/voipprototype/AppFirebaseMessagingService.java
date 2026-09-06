package com.example.voipprototype;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/** Receives FCM data and foreground notification messages outside the WebView. */
public final class AppFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "VoipFcm";
    private static final String CHANNEL_ID = "push_messages";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        getSharedPreferences("push_debug", MODE_PRIVATE)
                .edit()
                .putString("registration_token", token)
                .apply();
        Log.i(TAG, "FCM registration token refreshed");
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        Log.i(TAG, "FCM message received from " + message.getFrom());
        if ("incoming_call".equals(message.getData().get("type"))) {
            String callId = message.getData().getOrDefault("call_id", "mock-call");
            String caller = message.getData().getOrDefault("caller", "Mock caller");
            VoipCallManager.showIncomingCall(this, callId, caller);
            return;
        }
        showNotification(message);
    }

    private void showNotification(RemoteMessage message) {
        createChannel();

        RemoteMessage.Notification notification = message.getNotification();
        String title = notification != null && notification.getTitle() != null
                ? notification.getTitle()
                : message.getData().getOrDefault("title", "VoIP Prototype");
        String body = notification != null && notification.getBody() != null
                ? notification.getBody()
                : message.getData().getOrDefault("body", "Hello from Firebase Cloud Messaging.");

        Intent openApp = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID, builder.build());
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "General messages",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Test and service messages for the VoIP prototype.");
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .createNotificationChannel(channel);
    }
}
