package com.technicalchallenge.service;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.Cashflow;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.model.Schedule;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.model.TradeLeg;
import com.technicalchallenge.model.TradeStatus;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.CashflowRepository;
import com.technicalchallenge.repository.CounterpartyRepository;
import com.technicalchallenge.repository.TradeLegRepository;
import com.technicalchallenge.repository.TradeRepository;
import com.technicalchallenge.repository.TradeStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeLegRepository tradeLegRepository;

    @Mock
    private CashflowRepository cashflowRepository;

    @Mock
    private TradeStatusRepository tradeStatusRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CounterpartyRepository counterpartyRepository;

    @Mock
    private AdditionalInfoService additionalInfoService;

    @InjectMocks
    private TradeService tradeService;

    private TradeDTO tradeDTO;
    private Trade trade;
    private Book book;
    private Counterparty counterparty;
    private TradeStatus tradeStatus;
    private TradeLeg tradeLeg;


    @BeforeEach
    void setUp() {
        // Set up test data
        tradeDTO = new TradeDTO();
        tradeDTO.setTradeId(100001L);
        tradeDTO.setTradeDate(LocalDate.of(2025, 1, 15));
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 17));
        tradeDTO.setTradeMaturityDate(LocalDate.of(2026, 1, 17));

        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(1000000));
        leg1.setRate(0.05);

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(1000000));
        leg2.setRate(0.0);

        tradeDTO.setTradeLegs(Arrays.asList(leg1, leg2));

        book = new Book();
        book.setId(123L);
        book.setBookName("Test Book");

        counterparty = new Counterparty();
        counterparty.setId(345L);
        counterparty.setName("Test Counterparty");

        tradeDTO.setBookId(book.getId());
        tradeDTO.setCounterpartyId(counterparty.getId());

        trade = new Trade();
        trade.setId(1L);
        trade.setTradeId(100001L);

        tradeStatus = new TradeStatus();
        tradeStatus.setId(202L);
        tradeStatus.setTradeStatus("NEW");

        tradeLeg = new TradeLeg();
        tradeLeg.setLegId(303L);
    }

    @Test
    void testCreateTrade_Success() {
        // Given
        when(bookRepository.findById(123L)).thenReturn(Optional.of(book));
        when(counterpartyRepository.findById(345L)).thenReturn(Optional.of(counterparty));
        when(tradeStatusRepository.findByTradeStatus("NEW")).thenReturn(Optional.of(tradeStatus));

        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(tradeLeg);
        
        // When
        Trade result = tradeService.createTrade(tradeDTO);

        // Then
        assertNotNull(result);
        assertEquals(100001L, result.getTradeId());
        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    void testCreateTrade_InvalidDates_ShouldFail() {
        // Given
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 10)); // Before trade date

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        // Test that error with correct message is thrown
        assertEquals("Start date cannot be before trade date", exception.getMessage());
    }

    @Test
    void testCreateTrade_InvalidLegCount_ShouldFail() {
        // Given
        tradeDTO.setTradeLegs(Arrays.asList(new TradeLegDTO())); // Only 1 leg

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        assertTrue(exception.getMessage().contains("exactly 2 legs"));
    }

    @Test
    void testGetTradeById_Found() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(100001L)).thenReturn(Optional.of(trade));

        // When
        Optional<Trade> result = tradeService.getTradeById(100001L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(100001L, result.get().getTradeId());
    }

    @Test
    void testGetTradeById_NotFound() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        // When
        Optional<Trade> result = tradeService.getTradeById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testAmendTrade_Success() {
        // Given
        trade.setVersion(2);

        when(tradeRepository.findByTradeIdAndActiveTrue(100001L)).thenReturn(Optional.of(trade));
        when(tradeStatusRepository.findByTradeStatus("AMENDED")).thenReturn(Optional.of(new com.technicalchallenge.model.TradeStatus()));
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(tradeLeg);

        // When
        Trade result = tradeService.amendTrade(100001L, tradeDTO);

        // Then
        assertNotNull(result);
        verify(tradeRepository, times(2)).save(any(Trade.class)); // Save old and new
    }

    @Test
    void testAmendTrade_TradeNotFound() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.amendTrade(999L, tradeDTO);
        });

        assertTrue(exception.getMessage().contains("Trade not found"));
    }

    @ParameterizedTest
    // Input values for test. 
    @CsvSource({"2025,3,4", "2025,5,8", "2026,1,24", "2027,3,52"})
    void testCashflowGeneration_MonthlySchedule(int maturityYear, int maturityMonth, int invocationsCount) {

        // Given
        Schedule schedule = new Schedule();
        schedule.setId(111L);
        schedule.setSchedule("1M");
        tradeLeg.setNotional(BigDecimal.valueOf(1000000));
        tradeLeg.setCalculationPeriodSchedule(schedule);

        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 17));
        tradeDTO.setTradeMaturityDate(LocalDate.of(maturityYear, maturityMonth, 17));

        when(bookRepository.findById(123L)).thenReturn(Optional.of(book));
        when(counterpartyRepository.findById(345L)).thenReturn(Optional.of(counterparty));
        when(tradeStatusRepository.findByTradeStatus("NEW")).thenReturn(Optional.of(tradeStatus));

        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(tradeLeg);
        
        // When
        tradeService.createTrade(tradeDTO);
        
        // Then verify cashflowRepository.save has been called the correct number of times
        // There are two trade legs and a cashflow is generated for each for evey month
        verify(cashflowRepository, times(invocationsCount)).save(any(Cashflow.class));
    }
}
