package com.technicalchallenge.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
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

    // The trader this summary belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trader_id", referencedColumnName = "id")
    private ApplicationUser traderUser;

    private LocalDate summaryDate;
    // Today's trade count
    private int todaysTradeCount;
    // Today's notional
    private BigDecimal todaysNotional;
    
    // === Book-level summaries persisted as element collections ===
    @ElementCollection
    @CollectionTable(name = "daily_summary_trades_by_book", joinColumns = @JoinColumn(name = "daily_summary_id"))
    @MapKeyColumn(name = "book_name")
    @Column(name = "trade_count")
    private Map<String, Long> tradesByBook = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "daily_summary_notional_by_book", joinColumns = @JoinColumn(name = "daily_summary_id"))
    @MapKeyColumn(name = "book_name")
    @Column(name = "total_notional", precision = 38, scale = 2)
    private Map<String, BigDecimal> notionalByBook = new HashMap<>();

    // === Historical comparison ===
    private BigDecimal previousDayNotional;
    private int previousDayTradeCount;
    private BigDecimal notionalChangePercentage;
    private BigDecimal tradeCountChangePercentage;

    private BigDecimal avgTradeCount30Days;
    private BigDecimal avgNotional30Days;
    private BigDecimal notionalChange30Days;
    private BigDecimal tradeCountChange30Days;
}
