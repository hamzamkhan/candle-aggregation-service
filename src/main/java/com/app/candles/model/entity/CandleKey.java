package com.app.candles.model.entity;

import com.app.candles.model.enums.Interval;

import java.util.Objects;

public final class CandleKey {
    private final String symbol;
    private final Interval interval;

    public CandleKey(String symbol, Interval interval) {
        if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("symbol is required");
        this.symbol = symbol;
        this.interval = Objects.requireNonNull(interval, "interval");
    }

    public String symbol() { return symbol; }
    public Interval interval() { return interval; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CandleKey that)) return false;
        return symbol.equals(that.symbol) && interval == that.interval;
    }

    @Override
    public int hashCode() { return Objects.hash(symbol, interval); }
}
