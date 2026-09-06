package com.example.voipprototype;

import android.Manifest;
import android.content.pm.PackageManager;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        // BridgeActivity creates the WebView bridge during super.onCreate(), so
        // custom plugins must be registered before that point.
        registerPlugin(PushDebugPlugin.class);
        super.onCreate(savedInstanceState);
        VoipCallManager.registerPhoneAccount(this);
        requestNotificationPermission();
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
