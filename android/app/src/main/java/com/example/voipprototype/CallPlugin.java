package com.example.voipprototype;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** Exposes the native call state and controls to the Capacitor app. */
@CapacitorPlugin(name = "Call")
public final class CallPlugin extends Plugin {
    private static CallPlugin instance;

    @Override
    public void load() {
        instance = this;
    }

    @PluginMethod
    public void getCurrentCall(PluginCall call) {
        call.resolve(toJSObject(VoipCallManager.getCurrentCall()));
    }

    @PluginMethod
    public void hangUp(PluginCall call) {
        VoipCallManager.hangUp(getContext());
        call.resolve();
    }

    static void notifyCallState(String state, VoipCallManager.CallSession session) {
        CallPlugin plugin = instance;
        if (plugin == null) {
            return;
        }

        JSObject data = toJSObject(session);
        data.put("state", state);
        plugin.notifyListeners("callStateChanged", data);
    }

    private static JSObject toJSObject(VoipCallManager.CallSession session) {
        JSObject data = new JSObject();
        if (session == null) {
            data.put("state", "ended");
            return data;
        }

        data.put("callId", session.id);
        data.put("caller", session.caller);
        data.put("state", session.answerRequested ? "active" : "ringing");
        return data;
    }
}
