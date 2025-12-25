package com.app.candles.model.records;

import java.util.List;

public record HistoryResponse(String s, List<Long> t, List<Double> o, List<Double> h, List<Double> l, List<Double> c,
        List<Long> v) {
    public static HistoryResponse emptyOk() {
        return new HistoryResponse("ok", List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
