package com.app.candles.model;

import com.app.candles.model.entity.CandleId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

class CandleIdTest {
    
    @Test
    void testEquals() throws Exception {
        CandleId id1 = createCandleId("BTC-USD", "1m", 1000L);
        CandleId id2 = createCandleId("BTC-USD", "1m", 1000L);
        CandleId id3 = createCandleId("ETH-USD", "1m", 1000L);
        CandleId id4 = createCandleId("BTC-USD", "1h", 1000L);
        CandleId id5 = createCandleId("BTC-USD", "1m", 2000L);
        
        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertNotEquals(id1, id4);
        assertNotEquals(id1, id5);
        assertNotEquals(id1, null);
        assertNotEquals(id1, "not a CandleId");
        assertEquals(id1, id1);
    }
    
    @Test
    void testHashCode() throws Exception {
        CandleId id1 = createCandleId("BTC-USD", "1m", 1000L);
        CandleId id2 = createCandleId("BTC-USD", "1m", 1000L);
        CandleId id3 = createCandleId("ETH-USD", "1m", 1000L);
        
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1.hashCode(), id3.hashCode());
    }
    
    @Test
    void getters() throws Exception {
        CandleId id = createCandleId("BTC-USD", "1m", 1234567890L);
        assertEquals("BTC-USD", id.getSymbol());
        assertEquals("1m", id.getInterval());
        assertEquals(1234567890L, id.getBucketStartMs());
    }
    
    private CandleId createCandleId(String symbol, String interval, long bucketStartMs) throws Exception {
        Constructor<CandleId> constructor = CandleId.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        CandleId id = constructor.newInstance();
        setField(id, "symbol", symbol);
        setField(id, "interval", interval);
        setField(id, "bucketStartMs", bucketStartMs);
        return id;
    }
    
    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}

