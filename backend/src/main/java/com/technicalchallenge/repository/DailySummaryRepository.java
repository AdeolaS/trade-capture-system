package com.technicalchallenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.technicalchallenge.model.DailySummary;

public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {
    
}
