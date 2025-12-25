package com.app.candles.model.enums;

import lombok.Getter;

import java.time.Duration;
import java.util.Locale;

@Getter
public enum Interval {
    S1("1s", Duration.ofSeconds(1)),
    S5("5s", Duration.ofSeconds(5)),
    M1("1m", Duration.ofMinutes(1)),
    M15("15m", Duration.ofMinutes(15)),
    H1("1h", Duration.ofHours(1));

    private String code;
    private Duration duration;

    Interval(String code, Duration duration) {
        this.code = code;
        this.duration = duration;
    }

    public static Interval parse(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("interval is required");
        String s = raw.trim().toLowerCase(Locale.ROOT);
        for (Interval i : values()) {
            if (i.getCode().equals(s)) return i;
        }
        throw new IllegalArgumentException("Unsupported interval: " + raw + " (supported: 1s, 5s, 1m, 15m, 1h)");
    }
}
