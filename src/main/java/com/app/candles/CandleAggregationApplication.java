package com.app.candles;

import com.app.candles.database.PostgresAutoStarter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class CandleAggregationApplication {
    public static void main(String[] args) {
        Map<String, Object> overrides = new HashMap<>();
        PostgresAutoStarter.maybeStartAndConfigure(overrides);

        SpringApplication app = new SpringApplication(CandleAggregationApplication.class);
        if (!overrides.isEmpty()) {
            app.setDefaultProperties(overrides);
        }
        app.run(args);
    }
}
