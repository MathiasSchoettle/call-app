package com.example.voipprototype;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.splashscreen.SplashScreen;

/** Full-screen native UI shown when a mocked incoming call is ringing. */
public final class IncomingCallActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Switch from the Android 12+ starting theme to the real call theme.
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(16, 19, 26));
        getWindow().setNavigationBarColor(android.graphics.Color.rgb(16, 19, 26));
        setContentView(R.layout.activity_incoming_call);

        String caller = getIntent().getStringExtra(VoipCallManager.EXTRA_CALLER);
        TextView callerName = findViewById(R.id.incoming_caller);
        callerName.setText(caller == null ? "Mock caller" : caller);

        Button answer = findViewById(R.id.incoming_answer);
        answer.setOnClickListener(view -> {
            VoipCallManager.answer(this);
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        });

        Button decline = findViewById(R.id.incoming_decline);
        decline.setOnClickListener(view -> {
            VoipCallManager.decline(this);
            finish();
        });
    }
}
