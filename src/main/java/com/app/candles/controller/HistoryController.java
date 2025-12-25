package com.app.candles.controller;

import com.app.candles.model.entity.CandleKey;
import com.app.candles.model.enums.Interval;
import com.app.candles.model.records.Candle;
import com.app.candles.model.records.HistoryResponse;
import com.app.candles.service.CandleService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Validated
public class HistoryController {

    private final CandleService candleService;

    public HistoryController(CandleService candleService) {
        this.candleService = candleService;
    }

    @GetMapping("/history")
    public HistoryResponse history(@RequestParam("symbol") @NotBlank String symbol,
                                   @RequestParam("interval") @NotBlank String interval,
                                   @RequestParam("from") @Min(0) long from,
                                   @RequestParam("to") @Min(0) long to
    ) {
        Interval i = Interval.parse(interval);
        CandleKey key = new CandleKey(symbol, i);

        long fromMillis = from * 1000L;
        long toMillis = to * 1000L;

        List<Candle> candles = candleService.history(key, fromMillis, toMillis);
        if (candles.isEmpty()) {
            return HistoryResponse.emptyOk();
        }

        List<Long> t = new ArrayList<>(candles.size());
        List<Double> o = new ArrayList<>(candles.size());
        List<Double> h = new ArrayList<>(candles.size());
        List<Double> l = new ArrayList<>(candles.size());
        List<Double> c = new ArrayList<>(candles.size());
        List<Long> v = new ArrayList<>(candles.size());

        for (Candle candle : candles) {
            t.add(candle.time());
            o.add(candle.open());
            h.add(candle.high());
            l.add(candle.low());
            c.add(candle.close());
            v.add(candle.volume());
        }

        return new HistoryResponse("ok", t, o, h, l, c, v);
    }
}
