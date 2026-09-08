package com.example.voipprototype;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.splashscreen.SplashScreen;

/** Full-screen native UI shown when a mocked incoming call is ringing. */
public final class IncomingCallActivity extends Activity {
    private static final String TAG = "IncomingCallActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Switch from the Android 12+ starting theme to the real call theme.
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_incoming_call);

        String caller = getIntent().getStringExtra(VoipCallManager.EXTRA_CALLER);
        TextView callerName = findViewById(R.id.incoming_caller);
        callerName.setText(caller == null ? "Mock caller" : caller);

        Button answer = findViewById(R.id.incoming_answer);
        answer.setOnClickListener(view -> {
            Log.i(TAG, "Fullscreen accept clicked");
            VoipCallManager.answer(this);
            openMainActivity();
            finish();
        });

        Button decline = findViewById(R.id.incoming_decline);
        decline.setOnClickListener(view -> {
            VoipCallManager.decline(this);
            finish();
        });
    }

    private void openMainActivity() {
        Log.i(TAG, "Opening MainActivity after fullscreen accept");
        getApplicationContext().startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }
}
