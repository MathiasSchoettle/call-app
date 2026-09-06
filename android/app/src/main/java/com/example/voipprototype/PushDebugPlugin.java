package com.example.voipprototype;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.firebase.messaging.FirebaseMessaging;

/** Exposes the device token to the prototype UI; it is not used to send messages. */
@CapacitorPlugin(name = "PushDebug")
public class PushDebugPlugin extends Plugin {
    @PluginMethod
    public void getRegistrationToken(PluginCall call) {
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful() || task.getResult() == null) {
                    Exception exception = task.getException();
                    call.reject(
                            "Could not obtain an FCM registration token. Check google-services.json and device connectivity.",
                            exception
                    );
                    return;
                }

                JSObject result = new JSObject();
                result.put("token", task.getResult());
                call.resolve(result);
            });
        } catch (Exception exception) {
            call.reject(
                    "Firebase is not configured. Add android/app/google-services.json and rebuild the app.",
                    exception
            );
        }
    }
}
