package com.wakereality.thunderstrike.dataexchange;

/**
 */

public class EventIncomingRemGlkPayload {
    public final int format;
    public final String payload;

    public EventIncomingRemGlkPayload(int dataFormat, String dataPayload) {
        format = dataFormat;
        payload = dataPayload;
    }
}
