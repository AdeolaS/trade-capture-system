package com.technicalchallenge.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

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
@Table(name = "trade_summary")
public class TradeSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // The trader this summary belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trader_id", referencedColumnName = "id")
    private ApplicationUser traderUser;


    // when this snapshot was taken
    private LocalDateTime summaryTimeStamp;


    // Total number of trades by status
    // Marks it as a simple collection (not a separate entity)
    @ElementCollection
    // Define the name and structure of the secondary table.
    @CollectionTable(name = "trade_summary_status_count",
                    joinColumns = @JoinColumn(name = "summary_id"))
    // Column storing the map keys
    @MapKeyColumn(name = "status")
    // Column storing the map values
    @Column(name = "count")
    private Map<String, Long> tradeCountByStatus;


    // Total notional amounts by currency
    @ElementCollection
    @CollectionTable(name = "trade_summary_notional_by_currency",
                    joinColumns = @JoinColumn(name = "summary_id"))
    @MapKeyColumn(name = "currency")
    @Column(name = "total_notional")
    private Map<String, BigDecimal> totalNotionalByCurrency;

    // Breakdown by trade type
    @ElementCollection
    @CollectionTable(name = "trade_summary_trade_type", joinColumns = @JoinColumn(name = "summary_id"))
    @MapKeyColumn(name = "trade_type")
    @Column(name = "count")
    private Map<String, Long> tradeCountByTradeType;

    // Breakdown by  counterparty 
    @ElementCollection
    @CollectionTable(name = "trade_summary_by_counterparty",
                    joinColumns = @JoinColumn(name = "summary_id"))
    @MapKeyColumn(name = "counterparty")
    @Column(name = "trade_count")
    private Map<String, Long> tradeCountByCounterparty;

    // Risk exposure summaries
    @ElementCollection
    @CollectionTable(name = "trade_summary_risk", joinColumns = @JoinColumn(name = "summary_id"))
    @MapKeyColumn(name = "book_name")
    @Column(name = "exposure")
    private Map<String, BigDecimal> riskExposure;
}
