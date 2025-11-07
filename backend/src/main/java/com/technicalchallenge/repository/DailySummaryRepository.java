package com.technicalchallenge.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.technicalchallenge.model.DailySummary;

public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {
    Optional<DailySummary> findByTraderUser_IdAndSummaryDate(Long traderUserId, LocalDate summaryDateStamp);
}
