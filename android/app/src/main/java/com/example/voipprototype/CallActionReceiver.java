package com.example.voipprototype;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Handles the notification actions without routing user input through the WebView. */
public final class CallActionReceiver extends BroadcastReceiver {
    static final String ACTION_ANSWER = "com.example.voipprototype.ANSWER";
    static final String ACTION_DECLINE = "com.example.voipprototype.DECLINE";
    static final String ACTION_HANG_UP = "com.example.voipprototype.HANG_UP";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_ANSWER.equals(action)) {
            VoipCallManager.answer(context);
            Intent openApp = new Intent(context, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(openApp);
        } else if (ACTION_DECLINE.equals(action) || ACTION_HANG_UP.equals(action)) {
            VoipCallManager.decline(context);
        }
    }
}
