package com.technicalchallenge.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryDTO {
    private LocalDate summaryDate;
    // Today's trade count
    private int todaysTradeCount;
    // Today's notional
    private BigDecimal todaysNotional;
    
    // Book-level activity summaries
    private Map<String, Long> tradesByBook; // e.g. FX_BOOK: 10, EQUITY_BOOK: 4
    private Map<String, BigDecimal> notionalByBook; // e.g. FX_BOOK: 5,000,000

    // === Historical comparison ===
    private BigDecimal previousDayNotional;
    private int previousDayTradeCount;
    private BigDecimal notionalChangePercentage;
    private BigDecimal tradeCountChangePercentage;

    private BigDecimal avgTradeCount30Days;
    private BigDecimal avgNotional30Days;
    private BigDecimal notionalChange30Days;
    private BigDecimal tradeCountChange30Days;
}
