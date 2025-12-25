package com.app.candles.model.entity;

import  jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;

@Entity
@Getter
@Table(name = "candles")
public class Candle {
    @EmbeddedId
    private CandleId id;

    @Column(nullable = false)
    private double open;

    @Column(nullable = false)
    private double high;

    @Column(nullable = false)
    private double low;

    @Column(nullable = false)
    private double close;

    @Column(nullable = false)
    private long volume;

    @Column(name = "first_event_ts_ms", nullable = false)
    private long firstEventTsMs;

    @Column(name = "last_event_ts_ms", nullable = false)
    private long lastEventTsMs;
    // For optimistic lock
    @Version
    private long version;

}
