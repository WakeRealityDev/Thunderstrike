package com.wakereality.thunderstrike;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wakereality.thunderstrike.dataexchange.PayloadToEngine;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 */

public class RemGlkInputSender {
    public static final String broadcastTarget = "interactivefiction.remglk.JSON_INPUT";

    private final Context appContext;

    public RemGlkInputSender(Context applicationContext) {
        appContext = applicationContext;
        EventBus.getDefault().register(this);
        Log.v("GlkInputSender", "[dataToEngine] registered");
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    @SuppressWarnings("unused")
    public void onEvent(PayloadToEngine event) {
        Intent update = new Intent(broadcastTarget);
        update.putExtra("payload", event.payload);
        Log.v("GlkInputSender", "[dataToEngine] send: " + event.payload);
        appContext.sendBroadcast(update);
    }
}
