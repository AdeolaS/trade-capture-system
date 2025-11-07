package com.technicalchallenge.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.technicalchallenge.model.TradeSummary;

public interface TradeSummaryRepository extends JpaRepository<TradeSummary, Long> {
    // Find summaries for a trader on a specific date
    List<TradeSummary> findByTraderUser_IdAndSummaryDateStamp(Long traderUserId, LocalDate summaryDateStamp);

}
