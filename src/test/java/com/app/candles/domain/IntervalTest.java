package com.app.candles.domain;

import com.app.candles.model.enums.Interval;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IntervalTest {
    
    @Test
    void parseValidIntervals() {
        assertEquals(Interval.S1, Interval.parse("1s"));
        assertEquals(Interval.S5, Interval.parse("5s"));
        assertEquals(Interval.M1, Interval.parse("1m"));
        assertEquals(Interval.M15, Interval.parse("15m"));
        assertEquals(Interval.H1, Interval.parse("1h"));
    }
    
    @Test
    void parseCaseInsensitive() {
        assertEquals(Interval.S1, Interval.parse("1S"));
        assertEquals(Interval.M1, Interval.parse("1M"));
        assertEquals(Interval.H1, Interval.parse("1H"));
    }
    
    @Test
    void parseWithWhitespace() {
        assertEquals(Interval.M1, Interval.parse(" 1m "));
        assertEquals(Interval.S5, Interval.parse(" 5s "));
    }
    
    @Test
    void parseInvalidInterval() {
        assertThrows(IllegalArgumentException.class, () -> Interval.parse("2m"));
        assertThrows(IllegalArgumentException.class, () -> Interval.parse("1d"));
        assertThrows(IllegalArgumentException.class, () -> Interval.parse("invalid"));
    }
    
    @Test
    void parseNull() {
        assertThrows(IllegalArgumentException.class, () -> Interval.parse(null));
    }
    
    @Test
    void parseBlank() {
        assertThrows(IllegalArgumentException.class, () -> Interval.parse(""));
        assertThrows(IllegalArgumentException.class, () -> Interval.parse("   "));
    }
    
    @Test
    void getCode() {
        assertEquals("1s", Interval.S1.getCode());
        assertEquals("5s", Interval.S5.getCode());
        assertEquals("1m", Interval.M1.getCode());
        assertEquals("15m", Interval.M15.getCode());
        assertEquals("1h", Interval.H1.getCode());
    }
    
    @Test
    void getDuration() {
        assertEquals(java.time.Duration.ofSeconds(1), Interval.S1.getDuration());
        assertEquals(java.time.Duration.ofSeconds(5), Interval.S5.getDuration());
        assertEquals(java.time.Duration.ofMinutes(1), Interval.M1.getDuration());
        assertEquals(java.time.Duration.ofMinutes(15), Interval.M15.getDuration());
        assertEquals(java.time.Duration.ofHours(1), Interval.H1.getDuration());
    }
}
