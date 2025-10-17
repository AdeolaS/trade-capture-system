package com.technicalchallenge.dto;

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
    // Total number of trades by status
    private Map<String, Long> totalByStatus;

    // Total notional amounts by currency
    private Map<String, Double> totalNotionalByCurrency;

    // Breakdown by trade type
    private Map<String, Long> breakdownByTradeType;

    // Breakdown by  counterparty
    private Map<String, Long> breakdownByCounterparty;

    // Risk exposure summaries
    private Map<String, Object> riskExposure;
}
