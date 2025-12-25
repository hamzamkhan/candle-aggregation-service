package com.app.candles.service;

import com.app.candles.model.records.Candle;
import com.app.candles.model.entity.CandleKey;

import java.util.List;

public interface CandleService {
    void update(CandleKey key, long bucketStartMillis, double price, long eventTsMillis);
    List<Candle> history(CandleKey key, long fromMillis, long toMillis);
    void evictOlderThan(long cutoffMillis);
}
