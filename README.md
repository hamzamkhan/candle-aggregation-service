# Candle Aggregation Service (Spring Data JPA + PostgreSQL)

This project uses **proper Spring Data JPA repositories** (JpaRepository) for persistence.
PostgreSQL runs automatically from the application via **Testcontainers** (Docker required).

## Why a native UPSERT inside a JPA repository?
Candle updates are high-frequency and concurrent. A naive `find -> mutate -> save` loop causes race conditions.
So the repository exposes a single atomic SQL `INSERT ... ON CONFLICT DO UPDATE` as a `@Modifying @Query`.
It's still a JPA repository, but with the correct atomic update.

## Run
```bash
mvn spring-boot:run
```

## Call history
```bash
curl "http://localhost:8080/history?symbol=BTC-USD&interval=1m&from=0&to=9999999999"
```
