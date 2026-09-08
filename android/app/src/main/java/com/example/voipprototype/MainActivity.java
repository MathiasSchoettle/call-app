package com.example.voipprototype;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.WindowManager;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private static final String TAG = "MainActivity";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        // BridgeActivity creates the WebView bridge during super.onCreate(), so
        // custom plugins must be registered before that point.
        registerPlugin(PushDebugPlugin.class);
        registerPlugin(CallPlugin.class);
        super.onCreate(savedInstanceState);
        configureForLockscreen();
        VoipCallManager.registerPhoneAccount(this);
        requestNotificationPermission();
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        Log.i(TAG, "onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);
        configureForLockscreen();
    }

    private void configureForLockscreen() {
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void requestNotificationPermission() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[] { Manifest.permission.POST_NOTIFICATIONS },
                    NOTIFICATION_PERMISSION_REQUEST_CODE
            );
        }
    }
}
