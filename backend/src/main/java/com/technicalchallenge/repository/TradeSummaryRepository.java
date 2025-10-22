package com.technicalchallenge.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.technicalchallenge.model.TradeSummary;

public interface TradeSummaryRepository extends JpaRepository<TradeSummary, Long> {
    List<TradeSummary> findBySummaryDate(LocalDate date);
}
