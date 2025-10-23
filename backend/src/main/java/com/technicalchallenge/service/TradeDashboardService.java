// package com.technicalchallenge.service;

// import java.time.LocalDate;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.technicalchallenge.dto.TradeDTO;
// import com.technicalchallenge.dto.TradeSummaryDTO;
// import com.technicalchallenge.model.Trade;
// import com.technicalchallenge.model.TradeSummary;
// import com.technicalchallenge.repository.TradeRepository;

// @Service
// public class TradeDashboardService {
//     private static final Logger logger = LoggerFactory.getLogger(TradeService.class);

//     @Autowired
//     private TradeRepository tradeRepository;

//     public List<Trade> getPersonalTrades(Long traderLoginId) {
//         logger.info("Retrieving of user's trades");
//         return tradeRepository.findByTraderUser_Id(traderLoginId);
//     }

//     public List<Trade> getTradesByBook(Long bookId, Long userId) {
//         logger.info("Retrieving of user's trades from specified book");
//         return tradeRepository.findByTraderUser_IdAndBook_Id(bookId, userId);
//     }

//     public TradeSummaryDTO getTradeSummaryForUser(Long userId) {

//         TradeSummary tradeSummary = buildTradeSummary(userId);

//     }

//     private TradeSummary buildTradeSummary(Long userID) {

//         List<Trade> listOfUsersTrades = getPersonalTrades(userID);

//         TradeSummary tradeSummary = new TradeSummary();
//         tradeSummary.setTraderId(userID);
//         tradeSummary.setSummaryDate(LocalDate.now());

//         if (listOfUsersTrades == null || listOfUsersTrades.isEmpty()) {
//             return tradeSummary; // empty summary
//         }


//         return tradeSummary;
//     }
// }
