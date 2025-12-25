package com.app.candles.ingestion;

import com.app.candles.aggregation.CandleAggregation;
import com.app.candles.model.records.BidAskEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class MarketDataSimulator {

    private final CandleAggregation aggregator;
    private final List<String> symbols;
    private final Random random = new Random();

    private double btc = 30000.0;
    private double eth = 2000.0;

    public MarketDataSimulator(CandleAggregation aggregator,
                               @Value("${app.symbols}") List<String> symbols) {
        this.aggregator = aggregator;
        this.symbols = symbols;
    }

    @Scheduled(fixedRateString = "${app.tickRateMs:200}")
    public void tick() {
        long ts = System.currentTimeMillis();
        for (String symbol : symbols) {
            double mid = nextMid(symbol);
            double spread = Math.max(0.5, mid * 0.0002);
            double bid = mid - spread / 2.0;
            double ask = mid + spread / 2.0;
            aggregator.onEvent(new BidAskEvent(symbol, bid, ask, ts));
        }
    }

    private double nextMid(String symbol) {
        double change = random.nextGaussian() * 0.5;
        if ("BTC-USD".equalsIgnoreCase(symbol)) {
            btc = Math.max(1000.0, btc + change * 10);
            return btc;
        }
        if ("ETH-USD".equalsIgnoreCase(symbol)) {
            eth = Math.max(100.0, eth + change);
            return eth;
        }
        //For other symbols
        return 1000.0 + random.nextGaussian() * 5;
    }
}
