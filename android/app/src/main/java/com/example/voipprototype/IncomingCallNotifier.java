package com.example.voipprototype;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;

/** The user-visible ringing surface required for self-managed Telecom calls. */
final class IncomingCallNotifier {
    static final String INCOMING_CHANNEL_ID = "incoming_calls";
    static final int INCOMING_NOTIFICATION_ID = 2001;

    private IncomingCallNotifier() {}

    static void show(Context context, VoipCallManager.CallSession call) {
        createChannel(context);
        int requestCode = call.id.hashCode();

        Intent fullscreen = new Intent(context, IncomingCallActivity.class)
                .putExtra(VoipCallManager.EXTRA_CALL_ID, call.id)
                .putExtra(VoipCallManager.EXTRA_CALLER, call.caller)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent fullscreenIntent = PendingIntent.getActivity(
                context,
                requestCode,
                fullscreen,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent answerIntent = answerIntent(context, requestCode);
        PendingIntent declineIntent = actionIntent(context, CallActionReceiver.ACTION_DECLINE, requestCode + 1);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, INCOMING_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(call.caller)
                .setContentText("Incoming mock VoIP call")
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(fullscreenIntent)
                .setFullScreenIntent(fullscreenIntent, true)
                .setStyle(NotificationCompat.CallStyle.forIncomingCall(
                        new Person.Builder().setName(call.caller).setImportant(true).build(),
                        declineIntent,
                        answerIntent
                ));

        notificationManager(context).notify(INCOMING_NOTIFICATION_ID, notification.build());
    }

    static void cancelIncoming(Context context) {
        notificationManager(context).cancel(INCOMING_NOTIFICATION_ID);
    }

    private static PendingIntent actionIntent(Context context, String action, int requestCode) {
        Intent intent = new Intent(context, CallActionReceiver.class).setAction(action);
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static PendingIntent answerIntent(Context context, int requestCode) {
        Intent intent = new Intent(context, CallAnswerActivity.class);
        return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static void createChannel(Context context) {
        AudioAttributes ringtoneAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build();
        NotificationChannel channel = new NotificationChannel(
                INCOMING_CHANNEL_ID,
                "Incoming calls",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Ringing alerts for incoming VoIP calls.");
        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), ringtoneAttributes);
        channel.enableVibration(true);
        notificationManager(context).createNotificationChannel(channel);
    }

    private static NotificationManager notificationManager(Context context) {
        return context.getSystemService(NotificationManager.class);
    }
}
