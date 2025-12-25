package com.app.candles.domain;

import com.app.candles.model.enums.Interval;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IntervalTest {
    @Test void parse() {
        assertEquals(Interval.S1, Interval.parse("1s"));
        assertEquals(Interval.M1, Interval.parse("1m"));
        assertThrows(IllegalArgumentException.class, () -> Interval.parse("2m"));
    }
}
