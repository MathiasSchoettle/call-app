package com.example.voipprototype;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/** Coordinates the single mocked call across FCM, Telecom, and notification UI. */
public final class VoipCallManager {
    private static final String TAG = "VoipCallManager";
    private static final String ACCOUNT_ID = "voip_prototype";
    static final String EXTRA_CALL_ID = "com.example.voipprototype.CALL_ID";
    static final String EXTRA_CALLER = "com.example.voipprototype.CALLER";
    static final String EXTRA_SHOW_OVER_LOCKSCREEN = "com.example.voipprototype.SHOW_OVER_LOCKSCREEN";

    private static CallSession currentCall;
    private static MockVoipConnection currentConnection;
    private static final Set<String> cancelledCallIds = new HashSet<>();

    private VoipCallManager() {}

    public static synchronized void registerPhoneAccount(Context context) {
        TelecomManager telecom = context.getSystemService(TelecomManager.class);
        if (telecom == null) {
            Log.e(TAG, "Telecom is unavailable on this device");
            return;
        }

        PhoneAccount account = PhoneAccount.builder(phoneAccountHandle(context), "VoIP Prototype")
                .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
                .addSupportedUriScheme(PhoneAccount.SCHEME_SIP)
                .build();
        telecom.registerPhoneAccount(account);
    }

    public static synchronized void showIncomingCall(Context context, String callId, String caller) {
        if (currentCall != null) {
            Log.w(TAG, "Ignoring a second incoming call while a mock call is active");
            return;
        }

        registerPhoneAccount(context);
        currentCall = new CallSession(callId, caller);
        IncomingCallNotifier.show(context, currentCall);

        TelecomManager telecom = context.getSystemService(TelecomManager.class);
        if (telecom == null) {
            Log.e(TAG, "Cannot add incoming call: Telecom is unavailable");
            clear(context);
            return;
        }

        Bundle extras = new Bundle();
        extras.putString(EXTRA_CALL_ID, callId);
        extras.putString(EXTRA_CALLER, caller);
        extras.putParcelable(
                TelecomManager.EXTRA_INCOMING_CALL_ADDRESS,
                Uri.fromParts(PhoneAccount.SCHEME_SIP, "mock-caller", null)
        );

        try {
            telecom.addNewIncomingCall(phoneAccountHandle(context), extras);
        } catch (SecurityException exception) {
            Log.e(TAG, "Telecom rejected the incoming call", exception);
            clear(context);
        }
    }

    static synchronized MockVoipConnection createIncomingConnection(Context context, Bundle extras) {
        String callId = extras.getString(EXTRA_CALL_ID, "mock-call");
        String caller = extras.getString(EXTRA_CALLER, "Mock caller");
        if (cancelledCallIds.remove(callId)) {
            MockVoipConnection rejected = new MockVoipConnection(context, callId, caller);
            rejected.markDisconnected();
            return rejected;
        }
        if (currentCall == null) {
            currentCall = new CallSession(callId, caller);
            CallPlugin.notifyCallState("ringing", currentCall);
        }
        currentConnection = new MockVoipConnection(context, currentCall.id, currentCall.caller);
        if (currentCall.answerRequested) {
            currentConnection.markAnswered();
        }
        return currentConnection;
    }

    public static synchronized void answer(Context context) {
        Log.i(TAG, "Answer requested");
        if (currentCall == null) {
            Log.w(TAG, "Ignoring answer because there is no current call");
            return;
        }
        currentCall.answeredLocked = isDeviceLocked(context);
        currentCall.answerRequested = true;
        if (currentConnection != null) {
            currentConnection.markAnswered();
        }
        IncomingCallNotifier.cancelIncoming(context);
        CallForegroundService.start(context, currentCall);
        CallPlugin.notifyCallState("active", currentCall);
    }

    public static synchronized void decline(Context context) {
        if (currentCall != null) {
            cancelledCallIds.add(currentCall.id);
        }
        if (currentConnection != null) {
            currentConnection.markDisconnected();
        }
        clear(context);
    }

    public static synchronized void hangUp(Context context) {
        decline(context);
    }

    static synchronized void onConnectionDisconnected(Context context, MockVoipConnection connection) {
        if (connection == currentConnection) {
            clear(context);
        }
    }

    public static synchronized void clear(Context context) {
        CallSession endedCall = currentCall;
        currentCall = null;
        currentConnection = null;
        IncomingCallNotifier.cancelIncoming(context);
        CallForegroundService.stop(context);
        if (endedCall != null) {
            CallPlugin.notifyCallState("ended", endedCall);
            if (endedCall.answeredLocked) {
                new Handler(Looper.getMainLooper()).postDelayed(
                        () -> closeAppTasks(context),
                        300
                );
            }
        }
    }

    static synchronized CallSession getCurrentCall() {
        return currentCall;
    }

    static synchronized Intent createMainActivityIntent(Context context) {
        return new Intent(context, MainActivity.class)
                .putExtra(
                        EXTRA_SHOW_OVER_LOCKSCREEN,
                        currentCall != null && currentCall.answeredLocked
                );
    }

    private static boolean isDeviceLocked(Context context) {
        KeyguardManager keyguard = context.getSystemService(KeyguardManager.class);
        return keyguard != null && keyguard.isKeyguardLocked();
    }

    private static void closeAppTasks(Context context) {
        ActivityManager activityManager = context.getSystemService(ActivityManager.class);
        if (activityManager == null) {
            return;
        }

        for (ActivityManager.AppTask task : activityManager.getAppTasks()) {
            task.finishAndRemoveTask();
        }
    }

    private static PhoneAccountHandle phoneAccountHandle(Context context) {
        return new PhoneAccountHandle(
                new ComponentName(context, VoipConnectionService.class),
                ACCOUNT_ID
        );
    }

    static final class CallSession {
        final String id;
        final String caller;
        boolean answerRequested;
        boolean answeredLocked;

        CallSession(String id, String caller) {
            this.id = id;
            this.caller = caller;
        }
    }
}
