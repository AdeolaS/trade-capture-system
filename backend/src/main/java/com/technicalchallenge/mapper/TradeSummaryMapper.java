package com.technicalchallenge.mapper;

import com.technicalchallenge.dto.TradeSummaryDTO;
import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.model.TradeSummary;
import org.springframework.stereotype.Component;

@Component
public class TradeSummaryMapper {

    public TradeSummaryDTO toDto(TradeSummary tradeSummary) {
        if (tradeSummary == null) {
            return null;
        }

        TradeSummaryDTO dto = new TradeSummaryDTO();

        dto.setSummaryTimeStamp(tradeSummary.getSummaryTimeStamp());
        dto.setSummaryDateStamp(tradeSummary.getSummaryDateStamp());
        dto.setTradeCountByStatus(tradeSummary.getTradeCountByStatus());
        dto.setTotalNotionalByCurrency(tradeSummary.getTotalNotionalByCurrency());
        dto.setTradeCountByTradeType(tradeSummary.getTradeCountByTradeType());
        dto.setTradeCountByCounterparty(tradeSummary.getTradeCountByCounterparty());
        dto.setRiskExposure(tradeSummary.getRiskExposure());

        return dto;
    }

    public TradeSummary toEntity(TradeSummaryDTO dto, Long traderUserId) {
        if (dto == null) {
            return null;
        }

        TradeSummary tradeSummary = new TradeSummary();

        ApplicationUser trader = new ApplicationUser();
        trader.setId(traderUserId);
        tradeSummary.setTraderUser(trader);

        tradeSummary.setSummaryTimeStamp(dto.getSummaryTimeStamp());
        tradeSummary.setSummaryDateStamp(dto.getSummaryDateStamp());
        tradeSummary.setTradeCountByStatus(dto.getTradeCountByStatus());
        tradeSummary.setTotalNotionalByCurrency(dto.getTotalNotionalByCurrency());
        tradeSummary.setTradeCountByTradeType(dto.getTradeCountByTradeType());
        tradeSummary.setTradeCountByCounterparty(dto.getTradeCountByCounterparty());
        tradeSummary.setRiskExposure(dto.getRiskExposure());

        return tradeSummary;
    }
}
