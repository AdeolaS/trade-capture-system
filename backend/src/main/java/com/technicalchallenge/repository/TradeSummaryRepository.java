package com.technicalchallenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.technicalchallenge.model.TradeSummary;

public interface TradeSummaryRepository extends JpaRepository<TradeSummary, Long> {
    
}
