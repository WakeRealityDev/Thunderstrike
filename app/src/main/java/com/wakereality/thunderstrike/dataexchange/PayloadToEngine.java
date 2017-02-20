package com.wakereality.thunderstrike.dataexchange;

/**
 */

public class PayloadToEngine {
    final public String payload;
    final public int useIndicator;

    public PayloadToEngine(String payloadData, int payloadUseIndicatorCode) {
        payload = payloadData;
        useIndicator = payloadUseIndicatorCode;
    }
}
