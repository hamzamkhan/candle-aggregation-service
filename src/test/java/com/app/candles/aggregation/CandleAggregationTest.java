package com.app.candles.aggregation;

import com.app.candles.model.entity.CandleKey;
import com.app.candles.model.enums.Interval;
import com.app.candles.model.records.BidAskEvent;
import com.app.candles.service.CandleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandleAggregationTest {
    
    @Mock
    private CandleService candleService;
    
    private CandleAggregation candleAggregation;
    
    @BeforeEach
    void setUp() {
        candleAggregation = new CandleAggregation(candleService);
    }
    
    @Test
    void onEventProcessesAllIntervals() {
        long timestamp = 1609459200000L;
        BidAskEvent event = new BidAskEvent("BTC-USD", 30000.0, 30010.0, timestamp);
        
        candleAggregation.onEvent(event);
        
        verify(candleService, times(5)).update(any(CandleKey.class), anyLong(), eq(30005.0), eq(timestamp));
    }
    
    @Test
    void onEventCalculatesMidPrice() {
        long timestamp = 1609459200000L;
        BidAskEvent event = new BidAskEvent("BTC-USD", 30000.0, 30010.0, timestamp);
        
        ArgumentCaptor<Double> priceCaptor = ArgumentCaptor.forClass(Double.class);
        candleAggregation.onEvent(event);
        
        verify(candleService, atLeastOnce()).update(any(), anyLong(), priceCaptor.capture(), eq(timestamp));
        assertEquals(30005.0, priceCaptor.getValue());
    }
    
    @Test
    void onEventAlignsToBucketBoundaries() {
        long timestamp = 1609459200123L;
        BidAskEvent event = new BidAskEvent("BTC-USD", 30000.0, 30010.0, timestamp);
        
        ArgumentCaptor<Long> bucketCaptor = ArgumentCaptor.forClass(Long.class);
        candleAggregation.onEvent(event);
        
        verify(candleService, times(1)).update(argThat(key -> key.interval() == Interval.S1), 
            bucketCaptor.capture(), anyDouble(), anyLong());
        
        long expectedBucket1s = (timestamp / 1000L) * 1000L;
        assertEquals(expectedBucket1s, bucketCaptor.getValue());
    }
    
    @Test
    void onEventWithDifferentSymbols() {
        long timestamp = 1609459200000L;
        BidAskEvent event1 = new BidAskEvent("BTC-USD", 30000.0, 30010.0, timestamp);
        BidAskEvent event2 = new BidAskEvent("ETH-USD", 2000.0, 2005.0, timestamp);
        
        ArgumentCaptor<CandleKey> keyCaptor = ArgumentCaptor.forClass(CandleKey.class);
        
        candleAggregation.onEvent(event1);
        candleAggregation.onEvent(event2);
        
        verify(candleService, times(10)).update(keyCaptor.capture(), anyLong(), anyDouble(), anyLong());
        
        var capturedKeys = keyCaptor.getAllValues();
        assertTrue(capturedKeys.stream().anyMatch(k -> k.symbol().equals("BTC-USD")));
        assertTrue(capturedKeys.stream().anyMatch(k -> k.symbol().equals("ETH-USD")));
    }
    
    @Test
    void alignToBucket() {
        long timestamp = 1609459265123L;
        
        long bucket1s = CandleAggregation.alignToBucket(timestamp, 1000L);
        assertEquals(1609459265000L, bucket1s);
        
        long bucket1m = CandleAggregation.alignToBucket(timestamp, 60000L);
        assertEquals(1609459260000L, bucket1m);
        
        long bucket1h = CandleAggregation.alignToBucket(timestamp, 3600000L);
        assertEquals(1609459200000L, bucket1h);
    }
    
    @Test
    void alignToBucketExactBoundary() {
        long timestamp = 1609459200000L;
        long bucket = CandleAggregation.alignToBucket(timestamp, 60000L);
        assertEquals(1609459200000L, bucket);
    }
}

