package com.technicalchallenge.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.technicalchallenge.model.Trade;
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

}
