package com.technicalchallenge.service;

import java.math.BigDecimal;
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
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.model.TradeStatus;
import com.technicalchallenge.model.TradeSummary;
import com.technicalchallenge.model.TradeType;
import com.technicalchallenge.repository.CounterpartyRepository;
import com.technicalchallenge.repository.TradeRepository;
import com.technicalchallenge.repository.TradeStatusRepository;
import com.technicalchallenge.repository.TradeTypeRepository;

@Service
public class TradeDashboardService {
    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private TradeStatusRepository tradeStatusRepository;

    @Autowired
    private TradeTypeRepository tradeTypeRepository;

    @Autowired
    private CounterpartyRepository counterpartyRepository;

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
        // === Count trades by status (case-insensitive) ===
        // Convert trade objects into a stream
        Map<String, Long> tradeCountByStatus = listOfUsersTrades.stream()
                // Extract TradeStatus Object from each Trade object
                .map(Trade::getTradeStatus)
                // Filters out any objects that are null
                .filter(Objects::nonNull)
                // Extraacts the TradeStatus string from the TradeStatus Object
                .map(TradeStatus::getTradeStatus)
                .filter(Objects::nonNull)
                // Normalise all strings to uppper case for consistency
                .map(String::toUpperCase)
                // Groups statuses by name and counts the occurences of each one
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        List<TradeStatus> allStatuses = tradeStatusRepository.findAll();

        for (TradeStatus status : allStatuses) {
            String statusName = status.getTradeStatus().toUpperCase();
            // Adds any statuses present in the repo that weren't counted. Gives them count of 0
            tradeCountByStatus.putIfAbsent(statusName, 0L);
        }
        tradeSummary.setTradeCountByStatus(tradeCountByStatus);

        // === Count trades by Trade Type (case-insensitive) ===
        // Convert trade objects into a stream
        Map<String, Long> tradeCountByTradeType = listOfUsersTrades.stream()
                .map(Trade::getTradeType)
                .filter(Objects::nonNull)
                .map(TradeType::getTradeType)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        
        List<TradeType> allTypes = tradeTypeRepository.findAll();
        
        for (TradeType type : allTypes) {
            String typeName = type.getTradeType().toUpperCase();
            // Adds any statuses present in the repo that weren't counted. Gives them count of 0
            tradeCountByTradeType.putIfAbsent(typeName, 0L);
        }
        tradeSummary.setTradeCountByTradeType(tradeCountByTradeType);

        // === Count trades by Trade Type (case-insensitive) ===
        // Convert trade objects into a stream
        Map<String, Long> tradeCountByCounterparty = listOfUsersTrades.stream()
                .map(Trade::getCounterparty)
                .filter(Objects::nonNull)
                .map(Counterparty::getName)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        
        List<Counterparty> allCounterparties = counterpartyRepository.findAll();
        
        for (Counterparty counterparty : allCounterparties) {
            String counterpartyName = counterparty.getName().toUpperCase();
            // Adds any statuses present in the repo that weren't counted. Gives them count of 0
            tradeCountByCounterparty.putIfAbsent(counterpartyName, 0L);
        }
        tradeSummary.setTradeCountByCounterparty(tradeCountByCounterparty);

        Map<String, BigDecimal> totalNotionalByCurrency = listOfUsersTrades.stream()
                // Get all non-null tradelegs
                .flatMap(trade -> trade.getTradeLegs().stream())
                .filter(Objects::nonNull)
                .filter(leg -> leg.getCurrency() != null && leg.getNotional() != null)
                .collect(Collectors.groupingBy(
                    leg -> leg.getCurrency().getCurrency().toUpperCase(),
                    Collectors.reducing(BigDecimal.ZERO, leg -> leg.getNotional(), BigDecimal::add)
            ));
        tradeSummary.setTotalNotionalByCurrency(totalNotionalByCurrency);

        // Calculate risk exposure
        Map<String, BigDecimal> riskExposure = listOfUsersTrades.stream()
            .filter(trade -> trade.getBook() != null)
            .collect(Collectors.groupingBy(
                trade -> trade.getBook().getBookName().toUpperCase(),
                Collectors.mapping(
                    trade -> trade.getTradeLegs().stream()
                        .filter(Objects::nonNull)
                        .map(leg -> {
                            BigDecimal notional = leg.getNotional() == null ? BigDecimal.ZERO : leg.getNotional();
                            String payRec = leg.getPayReceiveFlag() != null ? leg.getPayReceiveFlag().getPayRec() : "PAY";
                            return payRec.equalsIgnoreCase("RECEIVE") ? notional : notional.negate();
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));
        tradeSummary.setRiskExposure(riskExposure);

        return tradeSummary;
    }
}
