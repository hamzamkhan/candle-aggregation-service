package com.app.candles.model;

import com.app.candles.model.entity.CandleKey;
import com.app.candles.model.enums.Interval;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CandleKeyTest {
    
    @Test
    void createValidCandleKey() {
        CandleKey key = new CandleKey("BTC-USD", Interval.M1);
        assertEquals("BTC-USD", key.symbol());
        assertEquals(Interval.M1, key.interval());
    }
    
    @Test
    void createWithDifferentIntervals() {
        CandleKey key1 = new CandleKey("BTC-USD", Interval.S1);
        CandleKey key2 = new CandleKey("BTC-USD", Interval.H1);
        
        assertEquals(Interval.S1, key1.interval());
        assertEquals(Interval.H1, key2.interval());
        assertNotEquals(key1, key2);
    }
    
    @Test
    void createWithNullSymbol() {
        assertThrows(IllegalArgumentException.class, () -> 
            new CandleKey(null, Interval.M1));
    }
    
    @Test
    void createWithBlankSymbol() {
        assertThrows(IllegalArgumentException.class, () -> 
            new CandleKey("", Interval.M1));
        assertThrows(IllegalArgumentException.class, () -> 
            new CandleKey("   ", Interval.M1));
    }
    
    @Test
    void createWithNullInterval() {
        assertThrows(NullPointerException.class, () -> 
            new CandleKey("BTC-USD", null));
    }
    
    @Test
    void equals() {
        CandleKey key1 = new CandleKey("BTC-USD", Interval.M1);
        CandleKey key2 = new CandleKey("BTC-USD", Interval.M1);
        CandleKey key3 = new CandleKey("ETH-USD", Interval.M1);
        CandleKey key4 = new CandleKey("BTC-USD", Interval.H1);
        
        assertEquals(key1, key2);
        assertNotEquals(key1, key3);
        assertNotEquals(key1, key4);
        assertNotEquals(key1, null);
        assertNotEquals(key1, "not a CandleKey");
    }
    
    @Test
    void testHashCode() {
        CandleKey key1 = new CandleKey("BTC-USD", Interval.M1);
        CandleKey key2 = new CandleKey("BTC-USD", Interval.M1);
        CandleKey key3 = new CandleKey("ETH-USD", Interval.M1);
        
        assertEquals(key1.hashCode(), key2.hashCode());
        assertNotEquals(key1.hashCode(), key3.hashCode());
    }
}

