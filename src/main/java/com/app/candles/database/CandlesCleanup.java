package com.app.candles.database;

import com.app.candles.service.CandleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CandlesCleanup {
    private final CandleService store;
    private final long retentionMillis;

    public CandlesCleanup(CandleService store, @Value("${app.retentionHours:48}") long retentionHours) {
        this.store = store;
        this.retentionMillis = retentionHours * 60L * 60L * 1000L;
    }

    @Scheduled(fixedDelay = 60_000)
    public void evict() {
        store.evictOlderThan(System.currentTimeMillis() - retentionMillis);
    }
}
