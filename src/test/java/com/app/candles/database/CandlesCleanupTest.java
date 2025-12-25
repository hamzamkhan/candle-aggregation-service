package com.app.candles.database;

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
class CandlesCleanupTest {
    
    @Mock
    private CandleService candleService;
    
    private CandlesCleanup candlesCleanup;
    
    @BeforeEach
    void setUp() {}
    
    @Test
    void evictCallsServiceWithCorrectCutoff() {
        long retentionHours = 48;
        candlesCleanup = new CandlesCleanup(candleService, retentionHours);
        
        long beforeEvict = System.currentTimeMillis();
        candlesCleanup.evict();
        long afterEvict = System.currentTimeMillis();
        
        ArgumentCaptor<Long> cutoffCaptor = ArgumentCaptor.forClass(Long.class);
        verify(candleService).evictOlderThan(cutoffCaptor.capture());
        
        long cutoff = cutoffCaptor.getValue();
        long expectedRetentionMillis = retentionHours * 60L * 60L * 1000L;
        
        assertTrue(cutoff >= beforeEvict - expectedRetentionMillis - 1000);
        assertTrue(cutoff <= afterEvict - expectedRetentionMillis + 1000);
    }
    
    @Test
    void evictWithDifferentRetentionHours() {
        long retentionHours = 24;
        candlesCleanup = new CandlesCleanup(candleService, retentionHours);
        
        candlesCleanup.evict();
        
        ArgumentCaptor<Long> cutoffCaptor = ArgumentCaptor.forClass(Long.class);
        verify(candleService).evictOlderThan(cutoffCaptor.capture());
        
        long cutoff = cutoffCaptor.getValue();
        long expectedRetentionMillis = retentionHours * 60L * 60L * 1000L;
        long currentTime = System.currentTimeMillis();
        
        long diff = currentTime - cutoff;
        long expectedDiff = expectedRetentionMillis;
        
        assertTrue(Math.abs(diff - expectedDiff) < 1000);
    }
    
    @Test
    void evictWithZeroRetention() {
        long retentionHours = 0;
        candlesCleanup = new CandlesCleanup(candleService, retentionHours);
        
        candlesCleanup.evict();
        
        ArgumentCaptor<Long> cutoffCaptor = ArgumentCaptor.forClass(Long.class);
        verify(candleService).evictOlderThan(cutoffCaptor.capture());
        
        long cutoff = cutoffCaptor.getValue();
        long currentTime = System.currentTimeMillis();
        
        assertTrue(Math.abs(cutoff - currentTime) < 1000);
    }
}

