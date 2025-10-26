package com.technicalchallenge.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.technicalchallenge.dto.TradeSummaryDTO;
import com.technicalchallenge.mapper.TradeSummaryMapper;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.model.TradeStatus;
import com.technicalchallenge.model.TradeSummary;
import com.technicalchallenge.repository.TradeRepository;

@Service
public class TradeDashboardService {
    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);

    @Autowired
    private TradeRepository tradeRepository;

    public List<Trade> getPersonalTrades(Long traderLoginId) {
        logger.info("Retrieving of user's trades");
        return tradeRepository.findByTraderUser_Id(traderLoginId);
    }

    public List<Trade> getTradesByBook(Long bookId, Long userId) {
        logger.info("Retrieving of user's trades from specified book");
        return tradeRepository.findByTraderUser_IdAndBook_Id(bookId, userId);
    }

    public TradeSummaryDTO getTradeSummaryForUser(Long userId) {

        TradeSummary tradeSummary = buildTradeSummary(userId);
        TradeSummaryMapper tradeSummaryMapper = new TradeSummaryMapper();
        TradeSummaryDTO tradeSummaryDTO = tradeSummaryMapper.toDto(tradeSummary);

        return tradeSummaryDTO;

    }

    private TradeSummary buildTradeSummary(Long userID) {

        List<Trade> listOfUsersTrades = getPersonalTrades(userID);

        TradeSummary tradeSummary = new TradeSummary();
        //tradeSummary.getTraderUser().setId(userID);
        tradeSummary.setSummaryTimeStamp(LocalDateTime.now());

        // If the user has no trades, return an empy trade summary
        if (listOfUsersTrades == null || listOfUsersTrades.isEmpty()) {
            return tradeSummary;
        }
        // Count trades by status (case-insensitive)
        Map<String, Long> tradeCountByStatus = listOfUsersTrades.stream()
                .map(Trade::getTradeStatus)
                .filter(Objects::nonNull)
                .map(TradeStatus::getTradeStatus)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        // Ensure all expected statuses exist (even with 0)
        for (String expected : List.of("NEW", "AMENDED", "TERMINATED", "CANCELLED", "LIVE", "DEAD")) {
            tradeCountByStatus.putIfAbsent(expected, 0L);
        }

        tradeSummary.setTradeCountByStatus(tradeCountByStatus);

        return tradeSummary;
    }
}
