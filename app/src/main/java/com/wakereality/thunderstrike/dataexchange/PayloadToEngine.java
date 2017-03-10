package com.wakereality.thunderstrike.dataexchange;

/**
 */

public class PayloadToEngine {
    final public String payload;
    final public int useIndicator;
    final public int windowId;
    final public long windowGeneration;

    public PayloadToEngine(String payloadData, int payloadUseIndicatorCode, int glkWindowId, long glkWindowInputGeneration) {
        payload = payloadData;
        useIndicator = payloadUseIndicatorCode;
        windowId = glkWindowId;
        windowGeneration = glkWindowInputGeneration;
    }
}