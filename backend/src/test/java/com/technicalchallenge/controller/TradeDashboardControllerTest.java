package com.technicalchallenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.technicalchallenge.dto.DailySummaryDTO;
import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeSummaryDTO;
import com.technicalchallenge.mapper.TradeMapper;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.service.TradeDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TradeDashboardController.class)
public class TradeDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeDashboardService tradeDashboardService;

    @MockBean
    private TradeMapper tradeMapper;

    private ObjectMapper objectMapper;
    private Trade trade;
    private TradeDTO tradeDTO;
    private TradeSummaryDTO tradeSummaryDTO;
    private DailySummaryDTO dailySummaryDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // --- Mock Trade and DTO setup ---
        trade = new Trade();
        trade.setTradeId(101L);
        trade.setVersion(1);
        trade.setTradeDate(LocalDate.now());
        trade.setTradeStartDate(LocalDate.now().plusDays(1));
        trade.setTradeMaturityDate(LocalDate.now().plusYears(2));

        tradeDTO = new TradeDTO();
        tradeDTO.setTradeId(101L);
        tradeDTO.setVersion(1);
        tradeDTO.setBookName("EQUITY-BOOK");
        tradeDTO.setTraderUserName("JohnTrader");
        tradeDTO.setCounterpartyName("AcmeBank");
        tradeDTO.setTradeStatus("LIVE");
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(1));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(2));

        when(tradeMapper.toDto(any(Trade.class))).thenReturn(tradeDTO);

        // --- Mock summary DTOs ---
        tradeSummaryDTO = new TradeSummaryDTO();
        tradeSummaryDTO.setTradeCountByStatus(Map.of("LIVE",5L));
        tradeSummaryDTO.setTotalNotionalByCurrency(Map.of("USD", new BigDecimal("5000000")));

        dailySummaryDTO = new DailySummaryDTO();
        dailySummaryDTO.setSummaryDate(LocalDate.now());
        dailySummaryDTO.setTodaysTradeCount(2);
        dailySummaryDTO.setTodaysNotional(new BigDecimal("2000000"));
        dailySummaryDTO.setTradesByBook(Map.of("EQUITY-BOOK", 2L));
        dailySummaryDTO.setNotionalByBook(Map.of("EQUITY-BOOK", new BigDecimal("2000000")));
    }

    @Test
    void testGetPersonalTrades_Success() throws Exception {
        when(tradeDashboardService.getPersonalTrades("user123"))
                .thenReturn(List.of(trade));

        mockMvc.perform(get("/api/dashboard/my-trades")
                        .param("userId", "user123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tradeId", is(101)))
                .andExpect(jsonPath("$[0].bookName", is("EQUITY-BOOK")))
                .andExpect(jsonPath("$[0].tradeStatus", is("LIVE")));

        verify(tradeDashboardService).getPersonalTrades("user123");
    }

    @Test
    void testGetTradesByBook_Success() throws Exception {
        when(tradeDashboardService.getTradesByBook("EQUITY-BOOK", "user123"))
                .thenReturn(List.of(trade));

        mockMvc.perform(get("/api/dashboard/book/EQUITY-BOOK/trades")
                        .param("userId", "user123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tradeId", is(101)))
                .andExpect(jsonPath("$[0].bookName", is("EQUITY-BOOK")));

        verify(tradeDashboardService).getTradesByBook("EQUITY-BOOK", "user123");
    }

    @Test
    void testGetTradeSummary_Success() throws Exception {
        when(tradeDashboardService.getTradeSummaryForUser("user123"))
                .thenReturn(tradeSummaryDTO);

        mockMvc.perform(get("/api/dashboard/summary")
                        .param("userId", "user123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.tradeCountByStatus.LIVE", is(5)))
            .andExpect(jsonPath("$.totalNotionalByCurrency.USD", is(5000000)));

        verify(tradeDashboardService).getTradeSummaryForUser("user123");
    }

    @Test
    void testGetDailySummary_Success() throws Exception {
        when(tradeDashboardService.getDailySummaryForUser("user123"))
                .thenReturn(dailySummaryDTO);

        mockMvc.perform(get("/api/dashboard/daily-summary")
                        .param("userId", "user123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todaysTradeCount", is(2)))
                .andExpect(jsonPath("$.todaysNotional", is(2000000)))
                .andExpect(jsonPath("$.tradesByBook.EQUITY-BOOK", is(2)))
                .andExpect(jsonPath("$.notionalByBook.EQUITY-BOOK", is(2000000)));

        verify(tradeDashboardService).getDailySummaryForUser("user123");
    }

    @Test
    void testGetPersonalTrades_EmptyList() throws Exception {
        when(tradeDashboardService.getPersonalTrades("user123"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/my-trades")
                        .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(tradeDashboardService).getPersonalTrades("user123");
    }

    @Test
    void testGetPersonalTrades_UserNotFound() throws Exception {
        when(tradeDashboardService.getPersonalTrades("invalidUser"))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/dashboard/my-trades")
                        .param("userId", "invalidUser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error fetching trades: User not found"));

        verify(tradeDashboardService).getPersonalTrades("invalidUser");
    }

    @Test
    void testGetTradesByBook_UserNotFound() throws Exception {
        when(tradeDashboardService.getTradesByBook("EQUITY-BOOK", "invalidUser"))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/dashboard/book/EQUITY-BOOK/trades")
                        .param("userId", "invalidUser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error fetching trades: User not found"));

        verify(tradeDashboardService).getTradesByBook("EQUITY-BOOK", "invalidUser");
    }

    @Test
    void testGetTradeSummary_UserNotFound() throws Exception {
        when(tradeDashboardService.getTradeSummaryForUser("invalidUser"))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/dashboard/summary")
                        .param("userId", "invalidUser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error fetching trades: User not found"));

        verify(tradeDashboardService).getTradeSummaryForUser("invalidUser");
    }

    @Test
    void testGetDailySummary_UserNotFound() throws Exception {
        when(tradeDashboardService.getDailySummaryForUser("invalidUser"))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/dashboard/daily-summary")
                        .param("userId", "invalidUser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error fetching trades: User not found"));

        verify(tradeDashboardService).getDailySummaryForUser("invalidUser");
    }

}
