package com.app.candles.repository;

import com.app.candles.model.entity.Candle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import jakarta.persistence.EntityManager;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CandleRepositoryIntegrationTest {
    
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("test_candles")
            .withUsername("test")
            .withPassword("test");
    
    @BeforeAll
    static void beforeAll() {
        try {
            postgres.start();
        } catch (Exception e) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "Docker not available: " + e.getMessage());
        }
    }
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.show-sql", () -> "false");
    }
    
    @Autowired
    private CandleRepository candleRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("""
            CREATE TABLE IF NOT EXISTS candles (
              symbol              TEXT              NOT NULL,
              interval            TEXT              NOT NULL,
              bucket_start_ms     BIGINT            NOT NULL,
              open                DOUBLE PRECISION  NOT NULL,
              high                DOUBLE PRECISION  NOT NULL,
              low                 DOUBLE PRECISION  NOT NULL,
              close               DOUBLE PRECISION  NOT NULL,
              volume              BIGINT            NOT NULL,
              first_event_ts_ms   BIGINT            NOT NULL,
              last_event_ts_ms    BIGINT            NOT NULL,
              version             BIGINT            NOT NULL DEFAULT 0,
              PRIMARY KEY (symbol, interval, bucket_start_ms)
            )
            """).executeUpdate();
    }
    
    @Test
    void upsertTickCreatesNewCandle() throws Exception {
        int result = candleRepository.upsertTick("BTC-USD", "1m", 1609459200000L, 30000.0, 1609459200000L);
        
        assertEquals(1, result);
        
        List<Candle> candles = candleRepository.findAll();
        assertEquals(1, candles.size());
        
        Candle candle = candles.get(0);
        assertEquals("BTC-USD", candle.getId().getSymbol());
        assertEquals("1m", candle.getId().getInterval());
        assertEquals(1609459200000L, candle.getId().getBucketStartMs());
        assertEquals(30000.0, candle.getOpen());
        assertEquals(30000.0, candle.getHigh());
        assertEquals(30000.0, candle.getLow());
        assertEquals(30000.0, candle.getClose());
        assertEquals(1L, candle.getVolume());
    }
    
    @Test
    void upsertTickUpdatesExistingCandle() throws Exception {
        candleRepository.upsertTick("BTC-USD", "1m", 1609459200000L, 30000.0, 1609459200000L);
        
        candleRepository.upsertTick("BTC-USD", "1m", 1609459200000L, 30100.0, 1609459201000L);
        
        candleRepository.upsertTick("BTC-USD", "1m", 1609459200000L, 29900.0, 1609459202000L);
        
        List<Candle> candles = candleRepository.findAll();
        assertEquals(1, candles.size());
        
        Candle candle = candles.get(0);
        assertEquals(30000.0, candle.getOpen());
        assertEquals(30100.0, candle.getHigh());
        assertEquals(29900.0, candle.getLow());
        assertEquals(29900.0, candle.getClose());
        assertEquals(3L, candle.getVolume());
    }
    
    @Test
    void findRangeReturnsCandlesInRange() throws Exception {
        candleRepository.upsertTick("BTC-USD", "1m", 1609459200000L, 30000.0, 1609459200000L);
        candleRepository.upsertTick("BTC-USD", "1m", 1609459260000L, 30100.0, 1609459260000L);
        candleRepository.upsertTick("BTC-USD", "1m", 1609459320000L, 30200.0, 1609459320000L);
        candleRepository.upsertTick("BTC-USD", "1m", 1609459380000L, 30300.0, 1609459380000L);
        
        List<Candle> result = candleRepository.findRange("BTC-USD", "1m", 1609459260000L, 1609459320000L);
        
        assertEquals(1, result.size());
        assertEquals(1609459260000L, result.get(0).getId().getBucketStartMs());
    }
    
    @Test
    void findRangeReturnsEmptyForNoMatches() {
        candleRepository.upsertTick("BTC-USD", "1m", 1609459200000L, 30000.0, 1609459200000L);
        
        List<Candle> result = candleRepository.findRange("ETH-USD", "1m", 1609459200000L, 1609459260000L);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void deleteOlderThanRemovesOldCandles() throws Exception {
        candleRepository.upsertTick("BTC-USD", "1m", 1609459200000L, 30000.0, 1609459200000L);
        candleRepository.upsertTick("BTC-USD", "1m", 1609459800000L, 30100.0, 1609459800000L);
        candleRepository.upsertTick("BTC-USD", "1m", 1609460400000L, 30200.0, 1609460400000L);
        
        int deleted = candleRepository.deleteOlderThan(1609459800000L);
        
        assertTrue(deleted >= 1);
        
        List<Candle> remaining = candleRepository.findAll();
        assertTrue(remaining.stream().noneMatch(c -> c.getId().getBucketStartMs() < 1609459800000L));
    }
    
    @Test
    void upsertTickWithDifferentSymbols() throws Exception {
        candleRepository.upsertTick("BTC-USD", "1m", 1609459200000L, 30000.0, 1609459200000L);
        candleRepository.upsertTick("ETH-USD", "1m", 1609459200000L, 2000.0, 1609459200000L);
        
        List<Candle> candles = candleRepository.findAll();
        assertEquals(2, candles.size());
        
        assertTrue(candles.stream().anyMatch(c -> c.getId().getSymbol().equals("BTC-USD")));
        assertTrue(candles.stream().anyMatch(c -> c.getId().getSymbol().equals("ETH-USD")));
    }
    
    @Test
    void upsertTickWithDifferentIntervals() throws Exception {
        candleRepository.upsertTick("BTC-USD", "1m", 1609459200000L, 30000.0, 1609459200000L);
        candleRepository.upsertTick("BTC-USD", "1h", 1609459200000L, 30000.0, 1609459200000L);
        
        List<Candle> candles = candleRepository.findAll();
        assertEquals(2, candles.size());
        
        assertTrue(candles.stream().anyMatch(c -> c.getId().getInterval().equals("1m")));
        assertTrue(candles.stream().anyMatch(c -> c.getId().getInterval().equals("1h")));
    }
}

