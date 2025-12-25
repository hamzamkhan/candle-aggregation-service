package com.app.candles.model.records;

public record BidAskEvent(String symbol, double bid, double ask, long timestampMillis) {}
