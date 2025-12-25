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
);

CREATE INDEX IF NOT EXISTS idx_candles_symbol_interval_time
  ON candles(symbol, interval, bucket_start_ms);
