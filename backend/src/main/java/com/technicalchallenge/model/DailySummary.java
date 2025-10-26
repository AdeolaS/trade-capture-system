package com.technicalchallenge.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_summary")
public class DailySummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate summaryDate;
    // Today's trade count
    private int todaysTradeCount;
    // Today's notional
    private BigDecimal todaysNotional;
    
    // Book-level activity summaries
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "book_activity_summary_id", referencedColumnName = "id")
    private BookActivitySummary bookActivitySummary;
}
