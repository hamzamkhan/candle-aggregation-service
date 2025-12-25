package com.app.candles.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
public class CandleId implements Serializable {

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "interval", nullable = false)
    private String interval;

    @Column(name = "bucket_start_ms", nullable = false)
    private long bucketStartMs;

    protected CandleId() {}

//    public CandleId(String symbol, String interval, long bucketStartMs) {
//        this.symbol = symbol;
//        this.interval = interval;
//        this.bucketStartMs = bucketStartMs;
//    }
//
//    public String getSymbol() { return symbol; }
//    public String getInterval() { return interval; }
//    public long getBucketStartMs() { return bucketStartMs; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CandleId candleId)) {
            return false;
        }
        return bucketStartMs == candleId.bucketStartMs &&
                Objects.equals(symbol, candleId.symbol) &&
                Objects.equals(interval, candleId.interval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, interval, bucketStartMs);
    }
}
