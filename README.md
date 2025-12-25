# Candle Aggregation Service

A high-performance candle aggregation service built with Spring Boot and PostgreSQL. This service aggregates real-time market data (bid/ask events) into OHLCV (Open, High, Low, Close, Volume) candles across multiple time intervals.

## Features

- **Real-time Candle Aggregation**: Processes bid/ask events and aggregates them into candles for multiple intervals (1s, 5s, 1m, 15m, 1h)
- **Market Data Simulator**: Built-in simulator for testing with configurable symbols and tick rates
- **REST API**: Query candle history with time range filtering
- **Auto-configured PostgreSQL**: Automatic database startup via Testcontainers, Docker is required

## Running the Application

### Option 1: Using Maven (with Testcontainers)

This automatically starts PostgreSQL in a Docker container:

```bash
mvn spring-boot:run
```

### Option 2: Using Docker Compose

Start PostgreSQL and the application together:

```bash
docker compose up --build
```

This will start:
- PostgreSQL on port 5432
- pgAdmin on port 5050
- Application on port 8080

## API Documentation

### Get Candle History

Retrieve aggregated candles for a specific symbol and interval within a time range.

**Endpoint:** `GET /history`

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `symbol` | String | Yes | Trading symbol (e.g., `BTC-USD`, `ETH-USD`) |
| `interval` | String | Yes | Candle interval: `1s`, `5s`, `1m`, `15m`, or `1h` |
| `from` | Long | Yes | Start timestamp (Unix timestamp in seconds) |
| `to` | Long | Yes | End timestamp (Unix timestamp in seconds) |

**Example Request:**

```bash
curl "http://localhost:8080/history?symbol=BTC-USD&interval=1m&from=0&to=9999999999"
```

**Response Fields:**

- `status`: Response status ("ok")
- `t`: Array of timestamps (bucket start times in seconds)
- `o`: Array of open prices
- `h`: Array of high prices
- `l`: Array of low prices
- `c`: Array of close prices
- `v`: Array of volumes


### Candle Aggregation Logic

1. **Event Processing**: Market data simulator generates bid/ask events at configurable intervals
2. **Price Calculation**: Mid price is calculated as `(bid + ask) / 2.0`
3. **Multi-Interval Aggregation**: Each event updates candles for all supported intervals (1s, 5s, 1m, 15m, 1h)
4. **Upsert Operation**: Atomic upsert ensures correct OHLCV values even under concurrent updates

### Database Schema

Can be found in src/resources/db/migration. Migration can be applied via Flyway

## Building

```bash
mvn clean package
```

## Testing

```bash
mvn test
```

## Bonus

1. **PgAdmin**: Added pgadmin UI to access database. It is exposed on port 5050. Login credentials include `admin@admin.com` as username and `admin` as password. After logging in, in servers, when clicking on the server (Candles DB) you will be asked to enter password which is `admin`. After that, go into `candles` database -> Schemas -> public -> Tables -> candles

2. **Cleanup**: A cleanup task that periodically deletes old candles from the database so the system doesn’t grow forever and slow down, named as `CandlesCleanup.java`
