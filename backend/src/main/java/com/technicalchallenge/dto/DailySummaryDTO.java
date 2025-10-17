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
public class DailySummaryDTO {
    // Today's trade count
    private int todaysTradeCount;
    // Today's notional
    private double todaysNotional;
    
    // Book-level activity summaries
    private Map<String, Object> bookActivitySummaries;

    //private int dayOverDayChange;??
}
