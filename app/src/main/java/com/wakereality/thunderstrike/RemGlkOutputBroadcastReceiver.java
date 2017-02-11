package com.wakereality.thunderstrike;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wakereality.thunderstrike.dataexchange.EventIncomingRemGlkPayload;

import org.greenrobot.eventbus.EventBus;

/**
 */

public class RemGlkOutputBroadcastReceiver extends BroadcastReceiver {
    protected static int previousIndex = -1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String payload = intent.getStringExtra("payload");
        if (payload != null) {
            int index = intent.getIntExtra("index", -1);
            int format = intent.getIntExtra("format", -1);
            if (previousIndex != index) {
                previousIndex = index;
                // ALog.v("[ThunderClap][shareRemGlk] #" + index + " length " + payload.length() + " format " + format + " when? " + intent.getLongExtra("when", -1L));
                EventBus.getDefault().post(new EventIncomingRemGlkPayload(format, payload));
            } else {
                Log.w("RemGlkOutputR", "[ThunderClap][shareRemGlk] DUPLICATE_INCOMING #" + index + " length " + payload.length() + " format " + format + " when? " + intent.getLongExtra("when", -1L));
            }
        }
    }
}
