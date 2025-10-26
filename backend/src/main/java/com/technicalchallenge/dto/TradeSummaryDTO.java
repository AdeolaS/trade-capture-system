package com.technicalchallenge.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeSummaryDTO {
    private LocalDateTime summaryTimeStamp;
    // Total number of trades by status e.g., {"LIVE": 10, "CANCELLED": 2}
    private Map<String, Long> tradeCountByStatus;

    // Total notional amounts by currency e.g. {"USD": 5_000_000, "EUR": 3_200_000}
    private Map<String, Double> totalNotionalByCurrency;

    // Breakdown by trade type e.g. {"IRS": 6, "FXSWAP": 4}
    private Map<String, Long> tradeCountByTradeType;

    // Breakdown by  counterparty e.g., {"Barclays": 3, "JPM": 7}
    private Map<String, Long> tradeCountByCounterparty;

    // Risk exposure summaries e.g. {"BOOK_A": 2_000_000, "BOOK_B": 1_500_000}
    private Map<String, BigDecimal> riskExposure;
}
