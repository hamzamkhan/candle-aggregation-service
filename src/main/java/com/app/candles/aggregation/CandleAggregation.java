package com.app.candles.aggregation;

import com.app.candles.model.entity.CandleKey;
import com.app.candles.model.enums.Interval;
import com.app.candles.model.records.BidAskEvent;
import com.app.candles.service.CandleService;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Service
public class CandleAggregation {

    private final CandleService store;
    private final Set<Interval> intervals = EnumSet.allOf(Interval.class);

    public CandleAggregation(CandleService store) {
        this.store = store;
    }

    public void onEvent(BidAskEvent event) {
        double mid = (event.bid() + event.ask()) / 2.0;
        for (Interval interval : intervals) {
            CandleKey key = new CandleKey(event.symbol(), interval);
            long bucketStart = alignToBucket(event.timestampMillis(), interval.getDuration().toMillis());
            store.update(key, bucketStart, mid, event.timestampMillis());
        }
    }

    static long alignToBucket(long epochMillis, long durationMillis) {
        return (epochMillis / durationMillis) * durationMillis;
    }
}
