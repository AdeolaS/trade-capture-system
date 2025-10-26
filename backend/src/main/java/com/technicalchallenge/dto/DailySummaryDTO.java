package com.technicalchallenge.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import com.technicalchallenge.model.BookActivitySummary;

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
    private int bookLevelTradeCount;
    private BigDecimal bookLevelNotional;
}
