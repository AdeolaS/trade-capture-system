package com.technicalchallenge.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.technicalchallenge.dto.DailySummaryDTO;
import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeSummaryDTO;
import com.technicalchallenge.mapper.DailySummaryMapper;
import com.technicalchallenge.mapper.TradeSummaryMapper;
import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.BookActivitySummary;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.model.DailySummary;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.model.TradeStatus;
import com.technicalchallenge.model.TradeSummary;
import com.technicalchallenge.model.TradeType;
import com.technicalchallenge.repository.ApplicationUserRepository;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.CounterpartyRepository;
import com.technicalchallenge.repository.DailySummaryRepository;
import com.technicalchallenge.repository.TradeRepository;
import com.technicalchallenge.repository.TradeStatusRepository;
import com.technicalchallenge.repository.TradeSummaryRepository;
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
    private BookRepository bookRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    @Autowired
    private TradeSummaryRepository tradeSummaryRepository;

    @Autowired
    private DailySummaryRepository dailySummaryRepository;

    
    private TradeSummaryMapper tradeSummaryMapper = new TradeSummaryMapper();

    public List<Trade> getPersonalTrades(String traderLoginId) {
        logger.info("Retrieving of user's trades");
        // Fetch and validate user
        ApplicationUser user = applicationUserRepository.findByLoginId(traderLoginId)
            .orElseThrow(() -> {
                logger.warn("User not found: {}", traderLoginId);
                return new RuntimeException("User not found with login ID: " + traderLoginId);
            });

        if (!user.isActive()) {
            logger.warn("User '{}' is inactive", traderLoginId);
            throw new RuntimeException("User is inactive: " + traderLoginId);
        }
        Long traderUserId = user.getId();
        List<Trade> trades = tradeRepository.findByTraderUser_Id(traderUserId);

        return trades;
    }

    public List<Trade> getTradesByBook(String bookName, String traderLoginId) {
        logger.info("Retrieving trades for user '{}' from book '{}'", traderLoginId, bookName);

        // Fetch and validate user
        ApplicationUser user = applicationUserRepository.findByLoginId(traderLoginId)
            .orElseThrow(() -> {
                logger.warn("User not found: {}", traderLoginId);
                return new RuntimeException("User not found with login ID: " + traderLoginId);
            });

        if (!user.isActive()) {
            logger.warn("User '{}' is inactive", traderLoginId);
            throw new RuntimeException("User is inactive: " + traderLoginId);
        }

        // Fetch and validate book
        Book book = bookRepository.findByBookName(bookName)
            .orElseThrow(() -> {
                logger.warn("Book not found: {}", bookName);
                return new RuntimeException("Book not found: " + bookName);
            });

        if (!book.isActive()) {
            logger.warn("Book '{}' is inactive", bookName);
            throw new RuntimeException("Book is inactive: " + bookName);
        }

        // Retrieve trades
        Long traderUserId = user.getId();
        Long bookId = book.getId();

        List<Trade> trades = tradeRepository.findByTraderUser_IdAndBook_Id(bookId, traderUserId);
        logger.info("Retrieved {} trades for user '{}' from book '{}'", trades.size(), traderLoginId, bookName);

        return trades;
    }

    public TradeSummaryDTO getTradeSummaryForUser(String traderLoginId) {

        logger.info("Retrieving trade summary");

        TradeSummary tradeSummary = buildTradeSummary(traderLoginId);
        //TradeSummaryMapper tradeSummaryMapper = new TradeSummaryMapper();
        TradeSummaryDTO tradeSummaryDTO = tradeSummaryMapper.toDto(tradeSummary);

        return tradeSummaryDTO;
    }

    public List<TradeSummaryDTO> getHistoricalTradeSummaries(String traderLoginId, LocalDate date) {

        logger.info("Retrieving historical trade summaries for user '{}' from date '{}'", traderLoginId, date);

        // Fetch and validate user
        ApplicationUser user = applicationUserRepository.findByLoginId(traderLoginId)
            .orElseThrow(() -> {
                logger.warn("User not found: {}", traderLoginId);
                return new RuntimeException("User not found with login ID: " + traderLoginId);
            });

        if (!user.isActive()) {
            logger.warn("User '{}' is inactive", traderLoginId);
            throw new RuntimeException("User is inactive: " + traderLoginId);
        }

        List<TradeSummary> tradeSummaries = tradeSummaryRepository.findByTraderUser_IdAndSummaryDateStamp();
        List<TradeSummaryDTO> tradeSummaryDTOs = tradeSummaries
            .stream()
            .map(tradeSummaryMapper::toDto)
            .toList();
        return tradeSummaryDTOs;
    }

    private TradeSummary buildTradeSummary(String userID) {

        logger.info("Building Trade Summary");
        List<Trade> listOfUsersTrades = getPersonalTrades(userID);

        TradeSummary tradeSummary = new TradeSummary();
        tradeSummary.setSummaryTimeStamp(LocalTime.now());
        tradeSummary.setSummaryDateStamp(LocalDate.now());

        // If the user has no trades, return an empy trade summary
        if (listOfUsersTrades == null || listOfUsersTrades.isEmpty()) {
            logger.info("User has no trades. Returning Empty Trade Summary");
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

        tradeSummaryRepository.save(tradeSummary);
        return tradeSummary;
    }

    // public DailySummaryDTO getDailySummaryForUser(Long userId) {

    //     logger.info("Retrieving trade summary");
    //     DailySummary dailySummary = buildDailySummary(userId);
    //     DailySummaryMapper dailySummaryMapper = new DailySummaryMapper();
    //     DailySummaryDTO dailySummaryDTO = dailySummaryMapper.toDto(dailySummary);

    //     return dailySummaryDTO;
    // }

    

    // private DailySummary buildDailySummary(Long userId) {
    //     logger.info("Building Daily Summary");

    //     DailySummary dailySummary = new DailySummary();
    //     dailySummary.setSummaryDate(LocalDate.now());

    //     List<Trade> listOfUsersTrades = tradeRepository.findByTraderUser_Id(userId);
    //     int tradeCount = listOfUsersTrades.size();
    //     dailySummary.setTodaysTradeCount(tradeCount);

    //     // Total notional (sum across all legs)
    //     BigDecimal totalNotional = listOfUsersTrades.stream()
    //             .flatMap(trade -> trade.getTradeLegs().stream())
    //             .filter(Objects::nonNull)
    //             .map(leg -> leg.getNotional() == null ? BigDecimal.ZERO : leg.getNotional())
    //             .reduce(BigDecimal.ZERO, BigDecimal::add);
    //     dailySummary.setTodaysNotional(totalNotional);

    //     // // === Book-level summary ===
    //     // Map<String, BookActivitySummary> bookSummaries = listOfUsersTrades.stream()
    //     //         .filter(trade -> trade.getBook() != null)
    //     //         .collect(Collectors.groupingBy(
    //     //                 trade -> trade.getBook().getBookName(),
    //     //                 Collectors.collectingAndThen(
    //     //                         Collectors.toList(),
    //     //                         tradesInBook -> {
    //     //                             int count = tradesInBook.size();
    //     //                             BigDecimal bookNotional = tradesInBook.stream()
    //     //                                     .flatMap(t -> t.getTradeLegs().stream())
    //     //                                     .filter(Objects::nonNull)
    //     //                                     .map(leg -> leg.getNotional() == null ? BigDecimal.ZERO : leg.getNotional())
    //     //                                     .reduce(BigDecimal.ZERO, BigDecimal::add);

    //     //                             BookActivitySummary summary = new BookActivitySummary();
    //     //                             summary.setTradeCount(count);
    //     //                             summary.setTotalNotional(bookNotional);
    //     //                         }
    //     //                 )
    //     //         )); 
    //     dailySummary.setBookActivitySummary(new BookActivitySummary());

    //     dailySummaryRepository.save(dailySummary);
    //     return dailySummary;
    // }
}
