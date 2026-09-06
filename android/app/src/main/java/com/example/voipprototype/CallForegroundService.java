package com.example.voipprototype;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

/** Keeps an answered call visible to Android as an active phone-call foreground service. */
public final class CallForegroundService extends Service {
    private static final String ACTION_START = "com.example.voipprototype.START_ACTIVE_CALL";
    private static final String CHANNEL_ID = "active_calls";
    private static final int NOTIFICATION_ID = 2002;
    private static final String EXTRA_CALLER = "caller";

    static void start(Context context, VoipCallManager.CallSession call) {
        Intent intent = new Intent(context, CallForegroundService.class)
                .setAction(ACTION_START)
                .putExtra(EXTRA_CALLER, call.caller);
        ContextCompat.startForegroundService(context, intent);
    }

    static void stop(Context context) {
        context.stopService(new Intent(context, CallForegroundService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_START.equals(intent.getAction())) {
            startAsForeground(intent.getStringExtra(EXTRA_CALLER));
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startAsForeground(String caller) {
        createChannel();
        Intent hangUp = new Intent(this, CallActionReceiver.class).setAction(CallActionReceiver.ACTION_HANG_UP);
        PendingIntent hangUpIntent = PendingIntent.getBroadcast(
                this,
                3001,
                hangUp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(caller == null ? "Mock caller" : caller)
                .setContentText("Mock VoIP call in progress")
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setOngoing(true)
                .addAction(0, "Hang up", hangUpIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL);
        } else {
            startForeground(NOTIFICATION_ID, notification.build());
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Active calls",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Ongoing VoIP calls.");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
}
