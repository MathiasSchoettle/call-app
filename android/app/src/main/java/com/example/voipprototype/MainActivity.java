package com.example.voipprototype;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.WindowManager;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        // BridgeActivity creates the WebView bridge during super.onCreate(), so
        // custom plugins must be registered before that point.
        registerPlugin(PushDebugPlugin.class);
        registerPlugin(CallPlugin.class);
        super.onCreate(savedInstanceState);
        configureForLockscreen(getIntent());
        VoipCallManager.registerPhoneAccount(this);
        requestNotificationPermission();
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        configureForLockscreen(intent);
    }

    private void configureForLockscreen(android.content.Intent intent) {
        boolean showOverLockscreen = intent.getBooleanExtra(
                VoipCallManager.EXTRA_SHOW_OVER_LOCKSCREEN,
                false
        );
        setShowWhenLocked(showOverLockscreen);
        setTurnScreenOn(showOverLockscreen);
        if (showOverLockscreen) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
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
