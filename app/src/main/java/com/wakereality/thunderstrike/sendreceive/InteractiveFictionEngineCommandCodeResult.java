package com.wakereality.thunderstrike.sendreceive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Stephen A. Gutknecht on 3/25/17.
 * This serves to document the existance of a broadcast that some long-running command codes
 *   send to indicate results.  Notably the storage search (code 3000)
 */

public class InteractiveFictionEngineCommandCodeResult extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /*
        engineMetaIntent.putExtra("commandcode", 1);
        engineMetaIntent.putExtra("result", 1)
        engineMetaIntent.putExtra("when", System.currentTimeMillis());
        engineMetaIntent.putExtra("sender", "com.wakereality.thunderword");
         */
    }
}
