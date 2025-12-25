package com.app.candles.service;

import com.app.candles.model.entity.Candle;
import com.app.candles.model.entity.CandleId;
import com.app.candles.model.entity.CandleKey;
import com.app.candles.model.enums.Interval;
import com.app.candles.repository.CandleRepository;
import com.app.candles.service.impl.CandleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandleServiceImplTest {
    
    @Mock
    private CandleRepository candleRepository;
    
    private CandleServiceImpl candleService;
    
    @BeforeEach
    void setUp() {
        candleService = new CandleServiceImpl(candleRepository);
    }
    
    @Test
    void updateCallsRepositoryUpsert() {
        CandleKey key = new CandleKey("BTC-USD", Interval.M1);
        long bucketStart = 1609459200000L;
        double price = 30000.0;
        long eventTs = 1609459205000L;
        
        when(candleRepository.upsertTick(anyString(), anyString(), anyLong(), anyDouble(), anyLong()))
            .thenReturn(1);
        
        candleService.update(key, bucketStart, price, eventTs);
        
        verify(candleRepository).upsertTick("BTC-USD", "1m", bucketStart, price, eventTs);
    }
    
    @Test
    void historyReturnsConvertedCandles() throws Exception {
        CandleKey key = new CandleKey("BTC-USD", Interval.M1);
        long fromMillis = 1609459200000L;
        long toMillis = 1609459260000L;
        
        Candle entity1 = createCandleEntity("BTC-USD", "1m", 1609459200000L,
            30000.0, 30100.0, 29900.0, 30050.0, 100);
        Candle entity2 = createCandleEntity("BTC-USD", "1m", 1609459260000L, 
            30050.0, 30150.0, 30000.0, 30100.0, 150);
        
        when(candleRepository.findRange("BTC-USD", "1m", fromMillis, toMillis))
            .thenReturn(List.of(entity1, entity2));
        
        List<com.app.candles.model.records.Candle> result = candleService.history(key, fromMillis, toMillis);
        
        assertEquals(2, result.size());
        assertEquals(1609459200L, result.get(0).time());
        assertEquals(30000.0, result.get(0).open());
        assertEquals(30100.0, result.get(0).high());
        assertEquals(29900.0, result.get(0).low());
        assertEquals(30050.0, result.get(0).close());
        assertEquals(100L, result.get(0).volume());
        
        verify(candleRepository).findRange("BTC-USD", "1m", fromMillis, toMillis);
    }
    
    @Test
    void historyWithEmptyResult() {
        CandleKey key = new CandleKey("BTC-USD", Interval.M1);
        long fromMillis = 1609459200000L;
        long toMillis = 1609459260000L;
        
        when(candleRepository.findRange(anyString(), anyString(), anyLong(), anyLong()))
            .thenReturn(List.of());
        
        List<com.app.candles.model.records.Candle> result = candleService.history(key, fromMillis, toMillis);
        
        assertTrue(result.isEmpty());
        verify(candleRepository).findRange("BTC-USD", "1m", fromMillis, toMillis);
    }
    
    @Test
    void evictOlderThanCallsRepository() {
        long cutoffMillis = 1609459200000L;
        
        when(candleRepository.deleteOlderThan(anyLong())).thenReturn(10);
        
        candleService.evictOlderThan(cutoffMillis);
        
        verify(candleRepository).deleteOlderThan(cutoffMillis);
    }
    
    private Candle createCandleEntity(String symbol, String interval, long bucketStartMs,
                                     double open, double high, double low, double close, long volume) throws Exception {
        Candle entity = new Candle();
        CandleId id = createCandleId(symbol, interval, bucketStartMs);
        setField(entity, "id", id);
        setField(entity, "open", open);
        setField(entity, "high", high);
        setField(entity, "low", low);
        setField(entity, "close", close);
        setField(entity, "volume", volume);
        setField(entity, "firstEventTsMs", bucketStartMs);
        setField(entity, "lastEventTsMs", bucketStartMs + 1000);
        setField(entity, "version", 1L);
        return entity;
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

