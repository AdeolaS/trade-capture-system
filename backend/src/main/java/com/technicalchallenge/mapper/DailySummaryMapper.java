package com.technicalchallenge.mapper;

import java.util.HashMap;

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

        // Mapps the maps while avoiding null pointer exceptions
        dto.setTradesByBook(dailySummary.getTradesByBook() != null ? new HashMap<>(dailySummary.getTradesByBook()) : new HashMap<>());

        dto.setNotionalByBook(dailySummary.getNotionalByBook() != null ? new HashMap<>(dailySummary.getNotionalByBook()) : new HashMap<>());

        dto.setPreviousDayNotional(dailySummary.getPreviousDayNotional());
        dto.setPreviousDayTradeCount(dailySummary.getPreviousDayTradeCount());
        dto.setNotionalChangePercentage(dailySummary.getNotionalChangePercentage());
        dto.setTradeCountChangePercentage(dailySummary.getTradeCountChangePercentage());

        dto.setAvgNotional30Days(dailySummary.getAvgNotional30Days());
        dto.setAvgTradeCount30Days(dailySummary.getAvgTradeCount30Days());
        dto.setNotionalChange30Days(dailySummary.getNotionalChange30Days());
        dto.setTradeCountChange30Days(dailySummary.getTradeCountChange30Days());

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

        dailySummary.setTradesByBook(dto.getTradesByBook() != null ? new HashMap<>(dto.getTradesByBook()) : new HashMap<>());

        dailySummary.setNotionalByBook(dto.getNotionalByBook() != null ? new HashMap<>(dto.getNotionalByBook()) : new HashMap<>());

        dailySummary.setPreviousDayNotional(dto.getPreviousDayNotional());
        dailySummary.setPreviousDayTradeCount(dto.getPreviousDayTradeCount());
        dailySummary.setNotionalChangePercentage(dto.getNotionalChangePercentage());
        dailySummary.setTradeCountChangePercentage(dto.getTradeCountChangePercentage());

        dailySummary.setAvgNotional30Days(dto.getAvgNotional30Days());
        dailySummary.setAvgTradeCount30Days(dto.getAvgTradeCount30Days());
        dailySummary.setNotionalChange30Days(dto.getNotionalChange30Days());
        dailySummary.setTradeCountChange30Days(dto.getTradeCountChange30Days());

        return dailySummary;
    }

}
