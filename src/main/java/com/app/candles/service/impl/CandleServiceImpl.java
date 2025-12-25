package com.app.candles.service.impl;

import com.app.candles.model.records.Candle;
import com.app.candles.model.entity.CandleKey;
import com.app.candles.repository.CandleRepository;
import com.app.candles.service.CandleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CandleServiceImpl implements CandleService {

    private final CandleRepository candleRepository;

    public CandleServiceImpl(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    @Override
    @Transactional
    public void update(CandleKey key, long bucketStartMillis, double price, long eventTsMillis) {
        candleRepository.upsertTick(key.symbol(), key.interval().getCode(), bucketStartMillis, price, eventTsMillis);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Candle> history(CandleKey key, long fromMillis, long toMillis) {
        List<com.app.candles.model.entity.Candle> rows = candleRepository.findRange(key.symbol(),
                key.interval().getCode(), fromMillis, toMillis);
        System.out.println(rows.size());
        return rows.stream()
                .map(e -> new Candle(
                        e.getId().getBucketStartMs() / 1000L,
                        e.getOpen(),
                        e.getHigh(),
                        e.getLow(),
                        e.getClose(),
                        e.getVolume()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void evictOlderThan(long cutoffMillis) {
        candleRepository.deleteOlderThan(cutoffMillis);
    }
}
