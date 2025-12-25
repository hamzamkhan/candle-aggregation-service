package com.app.candles.repository;

import com.app.candles.model.entity.Candle;
import com.app.candles.model.entity.CandleId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CandleRepository extends JpaRepository<Candle, CandleId> {

    @Query(value = """
    SELECT *
    FROM candles
    WHERE symbol = :symbol
      AND interval = :interval
      AND bucket_start_ms >= :fromMs
      AND bucket_start_ms < :toMs
    ORDER BY bucket_start_ms ASC
    """, nativeQuery = true)
    List<Candle> findRange(@Param("symbol") String symbol,
                           @Param("interval") String interval,
                           @Param("fromMs") long fromMs,
                           @Param("toMs") long toMs);

    @Modifying
    @Transactional
    @Query(value =
            "INSERT INTO candles(symbol, interval, bucket_start_ms, open, high, low, close, volume, first_event_ts_ms, last_event_ts_ms, version) " +
            "VALUES (:symbol, :interval, :bucketStartMs, :price, :price, :price, :price, 1, :eventTsMs, :eventTsMs, 0) " +
            "ON CONFLICT (symbol, interval, bucket_start_ms) DO UPDATE SET " +
            "high = GREATEST(candles.high, EXCLUDED.high), " +
            "low  = LEAST(candles.low,  EXCLUDED.low), " +
            "volume = candles.volume + 1, " +
            "open = CASE WHEN EXCLUDED.first_event_ts_ms < candles.first_event_ts_ms THEN EXCLUDED.open ELSE candles.open END, " +
            "first_event_ts_ms = LEAST(candles.first_event_ts_ms, EXCLUDED.first_event_ts_ms), " +
            "close = CASE WHEN EXCLUDED.last_event_ts_ms > candles.last_event_ts_ms THEN EXCLUDED.close ELSE candles.close END, " +
            "last_event_ts_ms = GREATEST(candles.last_event_ts_ms, EXCLUDED.last_event_ts_ms), " +
            "version = candles.version + 1",
            nativeQuery = true)
    int upsertTick(@Param("symbol") String symbol,
                   @Param("interval") String interval,
                   @Param("bucketStartMs") long bucketStartMs,
                   @Param("price") double price,
                   @Param("eventTsMs") long eventTsMs);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM candles WHERE bucket_start_ms < :cutoffMs", nativeQuery = true)
    int deleteOlderThan(@Param("cutoffMs") long cutoffMs);
}
