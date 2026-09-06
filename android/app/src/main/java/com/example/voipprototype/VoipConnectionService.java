package com.example.voipprototype;

import android.os.Bundle;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;

/** Self-managed Telecom entry point for incoming mocked VoIP calls. */
public final class VoipConnectionService extends ConnectionService {
    @Override
    public Connection onCreateIncomingConnection(
            PhoneAccountHandle connectionManagerPhoneAccount,
            ConnectionRequest request
    ) {
        Bundle extras = request.getExtras() == null ? new Bundle() : request.getExtras();
        return VoipCallManager.createIncomingConnection(this, extras);
    }

    @Override
    public void onCreateIncomingConnectionFailed(
            PhoneAccountHandle connectionManagerPhoneAccount,
            ConnectionRequest request
    ) {
        VoipCallManager.clear(this);
    }
}

final class MockVoipConnection extends Connection {
    private final android.content.Context context;
    private final String callId;

    MockVoipConnection(android.content.Context context, String callId, String caller) {
        this.context = context.getApplicationContext();
        this.callId = callId;
        setConnectionProperties(PROPERTY_SELF_MANAGED);
        setConnectionCapabilities(CAPABILITY_MUTE);
        setAddress(
                android.net.Uri.fromParts(PhoneAccount.SCHEME_SIP, "mock-caller", null),
                TelecomManager.PRESENTATION_ALLOWED
        );
        setCallerDisplayName(caller, TelecomManager.PRESENTATION_ALLOWED);
        setAudioModeIsVoip(true);
        setRinging();
    }

    @Override
    public void onAnswer() {
        VoipCallManager.answer(context);
    }

    @Override
    public void onReject() {
        VoipCallManager.decline(context);
    }

    @Override
    public void onDisconnect() {
        markDisconnected();
        VoipCallManager.onConnectionDisconnected(context, this);
    }

    void markAnswered() {
        setActive();
    }

    void markDisconnected() {
        setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
        destroy();
    }
}
