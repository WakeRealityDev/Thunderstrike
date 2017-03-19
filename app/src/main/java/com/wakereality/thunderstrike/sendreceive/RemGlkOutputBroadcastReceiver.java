package com.wakereality.thunderstrike.sendreceive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wakereality.thunderstrike.dataexchange.EventIncomingRemGlkPayload;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.zip.GZIPInputStream;

/**
 */

public class RemGlkOutputBroadcastReceiver extends BroadcastReceiver {
    protected static int previousIndex = -1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String payload = intent.getStringExtra("payload");
        if (payload == null) {
            // If payload is null, check if a compressed version was sent instead.
            byte[] payloadCompressedBytes = intent.getByteArrayExtra("payloadcompressed");
            if (payloadCompressedBytes != null) {
                payload = uncompressString(payloadCompressedBytes);
                if (payload != null) {
                    // TESTING_NOTE:  Level 9 story "Return to Eden" logs: uncompressed resulted in 1129917 from bytes 43911
                    Log.d("RemGlkOutputR", "uncompressed resulted in " + payload.length() + " from bytes " + payloadCompressedBytes.length);
                }
                // let payload pass through as null if compression fails.
            }
        }

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

    public static String uncompressString(byte[] bytes)  {
        try {
            InputStream ungzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes));
            Reader reader = new InputStreamReader(ungzipInputStream,  "UTF-8");
            Writer writer = new StringWriter();
            char[] buffer = new char[16384];

            for (int length = 0; (length = reader.read(buffer)) > 0;) {
                writer.write(buffer, 0, length);
            }

            reader.close();
            writer.close();

            return writer.toString();
        }
        catch (Exception e) {
            Log.e("RemGlkOutputR", "Exception decompressing", e);
            return null;
        }
    }
}
