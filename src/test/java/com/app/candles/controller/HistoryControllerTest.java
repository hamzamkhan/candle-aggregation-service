package com.app.candles.controller;

import com.app.candles.model.entity.CandleKey;
import com.app.candles.model.enums.Interval;
import com.app.candles.model.records.Candle;
import com.app.candles.service.CandleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HistoryController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
})
class HistoryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CandleService candleService;
    
    @BeforeEach
    void setUp() {
        reset(candleService);
    }
    
    @Test
    void historyReturnsValidResponse() throws Exception {
        List<Candle> candles = List.of(
            new Candle(1609459200L, 30000.0, 30100.0, 29900.0, 30050.0, 100L),
            new Candle(1609459260L, 30050.0, 30150.0, 30000.0, 30100.0, 150L)
        );
        
        when(candleService.history(any(CandleKey.class), anyLong(), anyLong()))
            .thenReturn(candles);
        
        mockMvc.perform(get("/history")
                .param("symbol", "BTC-USD")
                .param("interval", "1m")
                .param("from", "0")
                .param("to", "9999999999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.s").value("ok"))
            .andExpect(jsonPath("$.t").isArray())
            .andExpect(jsonPath("$.t[0]").value(1609459200L))
            .andExpect(jsonPath("$.t[1]").value(1609459260L))
            .andExpect(jsonPath("$.o[0]").value(30000.0))
            .andExpect(jsonPath("$.h[0]").value(30100.0))
            .andExpect(jsonPath("$.l[0]").value(29900.0))
            .andExpect(jsonPath("$.c[0]").value(30050.0))
            .andExpect(jsonPath("$.v[0]").value(100L))
            .andReturn();
        
        verify(candleService).history(argThat(key -> 
            key.symbol().equals("BTC-USD") && key.interval() == Interval.M1), 
            eq(0L), eq(9999999999000L));
    }
    
    @Test
    void historyWithEmptyResult() throws Exception {
        when(candleService.history(any(CandleKey.class), anyLong(), anyLong()))
            .thenReturn(List.of());
        
        mockMvc.perform(get("/history")
                .param("symbol", "BTC-USD")
                .param("interval", "1m")
                .param("from", "0")
                .param("to", "9999999999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.s").value("ok"))
            .andExpect(jsonPath("$.t").isEmpty())
            .andExpect(jsonPath("$.o").isEmpty())
            .andExpect(jsonPath("$.h").isEmpty())
            .andExpect(jsonPath("$.l").isEmpty())
            .andExpect(jsonPath("$.c").isEmpty())
            .andExpect(jsonPath("$.v").isEmpty());
    }
    
    @Test
    void historyWithDifferentIntervals() throws Exception {
        when(candleService.history(any(CandleKey.class), anyLong(), anyLong()))
            .thenReturn(List.of());
        
        mockMvc.perform(get("/history")
                .param("symbol", "BTC-USD")
                .param("interval", "1s")
                .param("from", "0")
                .param("to", "9999999999"))
            .andExpect(status().isOk());
        
        mockMvc.perform(get("/history")
                .param("symbol", "BTC-USD")
                .param("interval", "1h")
                .param("from", "0")
                .param("to", "9999999999"))
            .andExpect(status().isOk());
    }
    
    @Test
    void historyConvertsTimestampsToMillis() throws Exception {
        when(candleService.history(any(CandleKey.class), anyLong(), anyLong()))
            .thenReturn(List.of());
        
        mockMvc.perform(get("/history")
                .param("symbol", "BTC-USD")
                .param("interval", "1m")
                .param("from", "1609459200")
                .param("to", "1609459260"))
            .andExpect(status().isOk());
        
        verify(candleService).history(any(), eq(1609459200000L), eq(1609459260000L));
    }
    
    @Test
    void historyValidatesMissingSymbol() throws Exception {
        mockMvc.perform(get("/history")
                .param("interval", "1m")
                .param("from", "0")
                .param("to", "9999999999"))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void historyValidatesMissingInterval() throws Exception {
        mockMvc.perform(get("/history")
                .param("symbol", "BTC-USD")
                .param("from", "0")
                .param("to", "9999999999"))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void historyValidatesMissingFrom() throws Exception {
        mockMvc.perform(get("/history")
                .param("symbol", "BTC-USD")
                .param("interval", "1m")
                .param("to", "9999999999"))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void historyValidatesMissingTo() throws Exception {
        mockMvc.perform(get("/history")
                .param("symbol", "BTC-USD")
                .param("interval", "1m")
                .param("from", "0"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void historyValidatesInvalidInterval() throws Exception {
        mockMvc.perform(get("/history")
                .param("symbol", "BTC-USD")
                .param("interval", "invalid")
                .param("from", "0")
                .param("to", "9999999999"))
            .andExpect(status().isBadRequest());
    }
}

