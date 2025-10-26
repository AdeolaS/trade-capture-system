package com.technicalchallenge.mapper;

import org.springframework.stereotype.Component;

import com.technicalchallenge.dto.DailySummaryDTO;
import com.technicalchallenge.model.DailySummary;

@Component
public class DailySummaryMapper {

    public DailySummaryDTO toDto(DailySummary dailySummary) {
        if (dailySummary == null) {
            return null;
        }

        DailySummaryDTO dto = new DailySummaryDTO();
        dto.setSummaryDate(dailySummary.getSummaryDate());
        dto.setTodaysTradeCount(dailySummary.getTodaysTradeCount());
        dto.setTodaysNotional(dailySummary.getTodaysNotional());
        dto.setBookLevelTradeCount(dailySummary.getBookActivitySummary().getTradeCount());
        dto.setBookLevelNotional(dailySummary.getBookActivitySummary().getTotalNotional());

        return dto;
    }

    public DailySummary toEntity(DailySummaryDTO dto) {
        if (dto == null) {
            return null;
        }

        DailySummary dailySummary = new DailySummary();
        dailySummary.setSummaryDate(dto.getSummaryDate());
        dailySummary.setTodaysTradeCount(dto.getTodaysTradeCount());
        dailySummary.setTodaysNotional(dto.getTodaysNotional());
        dailySummary.getBookActivitySummary().setTotalNotional(dto.getBookLevelNotional());
        dailySummary.getBookActivitySummary().setTradeCount(dto.getBookLevelTradeCount());

        return dailySummary;
    }

}
