package com.technicalchallenge.repository;

import com.technicalchallenge.model.Trade;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long>, JpaSpecificationExecutor<Trade> {

    @Query("""
        SELECT t FROM Trade t WHERE 
            (:earliestTradeDate IS NULL OR t.tradeDate >= :earliestTradeDate)
            AND (:latestTradeDate IS NULL OR t.tradeDate <= :latestTradeDate)
            AND (:tradeStatusId IS NULL OR :tradeStatusId = t.tradeStatus.id)
            AND (:traderId IS NULL OR :traderId = t.traderUser.id)
            AND (:bookId IS NULL OR :bookId = t.book.id)
            AND (:counterpartyId IS NULL OR :counterpartyId = t.counterparty.id)
    """)
    public List<Trade> searchTradesUsingSearchCriteria(
        @Param ("earliestTradeDate") LocalDate earliestTradeDate,
        @Param ("latestTradeDate") LocalDate latestTradeDate,
        @Param ("tradeStatusId") Long tradeStatusId,
        @Param ("traderId") Long traderId,
        @Param ("bookId") Long bookId,
        @Param ("counterpartyId") Long counterpartyId
    );

    // Existing methods
    List<Trade> findByTradeId(Long tradeId);

    @Query("SELECT MAX(t.tradeId) FROM Trade t")
    Optional<Long> findMaxTradeId();

    @Query("SELECT MAX(t.version) FROM Trade t WHERE t.tradeId = :tradeId")
    Optional<Integer> findMaxVersionByTradeId(@Param("tradeId") Long tradeId);

    // NEW METHODS for service layer compatibility
    Optional<Trade> findByTradeIdAndActiveTrue(Long tradeId);

    List<Trade> findByActiveTrueOrderByTradeIdDesc();

    @Query("SELECT t FROM Trade t WHERE t.tradeId = :tradeId AND t.active = true ORDER BY t.version DESC")
    Optional<Trade> findLatestActiveVersionByTradeId(@Param("tradeId") Long tradeId);
}
