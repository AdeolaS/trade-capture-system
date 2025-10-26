package com.technicalchallenge.service;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.Cashflow;
import com.technicalchallenge.model.CostCenter;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.model.Desk;
import com.technicalchallenge.model.Index;
import com.technicalchallenge.model.LegType;
import com.technicalchallenge.model.PayRec;
import com.technicalchallenge.model.Schedule;
import com.technicalchallenge.model.SubDesk;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.model.TradeLeg;
import com.technicalchallenge.model.TradeStatus;
import com.technicalchallenge.model.TradeSubType;
import com.technicalchallenge.model.TradeType;
import com.technicalchallenge.repository.ApplicationUserRepository;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.BusinessDayConventionRepository;
import com.technicalchallenge.repository.CashflowRepository;
import com.technicalchallenge.repository.CounterpartyRepository;
import com.technicalchallenge.repository.CurrencyRepository;
import com.technicalchallenge.repository.HolidayCalendarRepository;
import com.technicalchallenge.repository.IndexRepository;
import com.technicalchallenge.repository.LegTypeRepository;
import com.technicalchallenge.repository.PayRecRepository;
import com.technicalchallenge.repository.ScheduleRepository;
import com.technicalchallenge.repository.TradeLegRepository;
import com.technicalchallenge.repository.TradeRepository;
import com.technicalchallenge.repository.TradeStatusRepository;
import com.technicalchallenge.repository.TradeSubTypeRepository;
import com.technicalchallenge.repository.TradeTypeRepository;

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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
    private LegTypeRepository legTypeRepository;

    @Mock
    private IndexRepository indexRepository;

    @Mock
    private PayRecRepository payRecRepository;

    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @Mock
    private AdditionalInfoService additionalInfoService;

    @Mock
    private TradeTypeRepository tradeTypeRepository;

    @Mock
    private TradeSubTypeRepository tradeSubTypeRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private HolidayCalendarRepository holidayCalendarRepository;

    @Mock
    private BusinessDayConventionRepository businessDayConventionRepository;

    @InjectMocks
    private TradeService tradeService;

    private TradeDTO tradeDTO;
    private Trade trade;
    private Book book;
    private Counterparty counterparty;
    private TradeStatus tradeStatus;
    private TradeLeg tradeLeg;
    private ApplicationUser traderUser;
    private TradeType tradeType;
    private TradeSubType tradeSubType;

    @BeforeEach
    void setUp() {
        // Set up test data
        // === DTO setup ===
        tradeDTO = new TradeDTO();
        tradeDTO.setTradeId(100001L);
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(2));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1).plusDays(2));

        // --- Create legs ---
        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(1_000_000));
        leg1.setRate(0.05);
        leg1.setLegType("Floating");
        leg1.setPayReceiveFlag("Pay");
        leg1.setIndexId(1000L);
        leg1.setCurrencyId(1000L);
        leg1.setScheduleId(1000L);
        leg1.setHolidayCalendarId(1000L);
        leg1.setPaymentBdcId(1000L);
        leg1.setFixingBdcId(1000L);

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(1_000_000));
        leg2.setRate(0.01);
        leg2.setLegType("Fixed");
        leg2.setPayReceiveFlag("Receive");
        leg2.setCurrencyId(1000L);
        leg2.setScheduleId(1000L);
        leg2.setHolidayCalendarId(1000L);
        leg2.setPaymentBdcId(1000L);
        leg2.setFixingBdcId(1000L);

        tradeDTO.setTradeLegs(Arrays.asList(leg1, leg2));

        // === Reference entities ===
        Desk desk = new Desk();
        desk.setId(1000L);
        desk.setDeskName("FX");

        SubDesk subDesk = new SubDesk();
        subDesk.setId(1000L);
        subDesk.setSubdeskName("FX Spot");
        subDesk.setDesk(desk);

        CostCenter costCenter = new CostCenter();
        costCenter.setId(1000L);
        costCenter.setCostCenterName("London Trading");
        costCenter.setSubDesk(subDesk);

        book = new Book();
        book.setId(123L);
        book.setBookName("Test Book");
        book.setActive(true);
        book.setCostCenter(costCenter);

        counterparty = new Counterparty();
        counterparty.setId(345L);
        counterparty.setName("Test Counterparty");
        counterparty.setActive(true);

        tradeStatus = new TradeStatus();
        tradeStatus.setId(202L);
        tradeStatus.setTradeStatus("NEW");

        traderUser = new ApplicationUser();
        traderUser.setId(456L);
        traderUser.setActive(true);

        tradeType = new TradeType();
        tradeType.setId(1000L);
        tradeType.setTradeType("Swap");

        tradeSubType = new TradeSubType();
        tradeSubType.setId(1003L);
        tradeSubType.setTradeSubType("IR Swap");

        // === Trade ===
        trade = new Trade();
        trade.setId(1L);
        trade.setTradeId(100001L);
        trade.setBook(book);
        trade.setCounterparty(counterparty);
        trade.setTradeStatus(tradeStatus);
        trade.setTraderUser(traderUser);
        trade.setTradeType(tradeType);
        trade.setTradeSubType(tradeSubType);

        tradeLeg = new TradeLeg();
        tradeLeg.setLegId(303L);

        // === Link IDs to DTO ===
        tradeDTO.setBookId(book.getId());
        tradeDTO.setCounterpartyId(counterparty.getId());
        tradeDTO.setTraderUserId(traderUser.getId());
        tradeDTO.setTradeTypeId(tradeType.getId());
        tradeDTO.setTradeSubTypeId(tradeSubType.getId());
    }

    @Test
    void testCreateTrade_Success() {
        // Given
        when(bookRepository.findById(123L)).thenReturn(Optional.of(book));
        when(counterpartyRepository.findById(345L)).thenReturn(Optional.of(counterparty));
        when(tradeStatusRepository.findByTradeStatus("NEW")).thenReturn(Optional.of(tradeStatus));
        
        when(applicationUserRepository.findById(anyLong())).thenReturn(Optional.of(traderUser));
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(tradeLeg);
        when(tradeTypeRepository.findById(1000L)).thenReturn(Optional.of(tradeType));
        when(tradeSubTypeRepository.findById(1003L)).thenReturn(Optional.of(tradeSubType));

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
        assertTrue(exception.getMessage().contains("TRADE VALIDATION FAILED: Start date cannot be before trade date"));
    }

    @Test
    void testCreateTrade_InvalidLegCount_ShouldFail() {
        // Given
        tradeDTO.setTradeLegs(Arrays.asList(new TradeLegDTO())); // Only 1 leg

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        assertTrue(exception.getMessage().contains("Trade must have exactly two legs"));
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
        tradeDTO.setTradeStatus("AMENDED");

        when(tradeRepository.findByTradeIdAndActiveTrue(100001L)).thenReturn(Optional.of(trade));
        when(tradeStatusRepository.findByTradeStatus("AMENDED")).thenReturn(Optional.of(tradeStatus));
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(counterpartyRepository.findById(anyLong())).thenReturn(Optional.of(counterparty));
        when(applicationUserRepository.findById(anyLong())).thenReturn(Optional.of(traderUser));
        when(tradeTypeRepository.findById(1000L)).thenReturn(Optional.of(tradeType));
        when(tradeSubTypeRepository.findById(1003L)).thenReturn(Optional.of(tradeSubType));

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
    @CsvSource({"0,2,4", "0,4,8", "1,0,24", "2,2,52"})
    void testCashflowGeneration_MonthlySchedule(int years, int months, int invocationsCount) {

        // Given
        Schedule schedule = new Schedule();
        schedule.setId(111L);
        schedule.setSchedule("1M");
        tradeLeg.setNotional(BigDecimal.valueOf(1000000));
        tradeLeg.setCalculationPeriodSchedule(schedule);

        tradeDTO.setTradeStartDate(LocalDate.now());
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(years).plusMonths(months));

        LegType legType = new LegType();
        Index index = new Index();
        PayRec payRec = new PayRec();

        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(counterpartyRepository.findById(anyLong())).thenReturn(Optional.of(counterparty));
        when(tradeStatusRepository.findByTradeStatus("NEW")).thenReturn(Optional.of(tradeStatus));
        when(applicationUserRepository.findById(anyLong())).thenReturn(Optional.of(traderUser));

        when(legTypeRepository.findByType(anyString())).thenReturn(Optional.of(legType));
        when(indexRepository.findById(anyLong())).thenReturn(Optional.of(index));
        when(payRecRepository.findByPayRec(anyString())).thenReturn(Optional.of(payRec));
        when(tradeTypeRepository.findById(1000L)).thenReturn(Optional.of(tradeType));
        when(tradeSubTypeRepository.findById(1003L)).thenReturn(Optional.of(tradeSubType));

        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(tradeLeg);
        // When
        tradeService.createTrade(tradeDTO);
        
        // Then verify cashflowRepository.save has been called the correct number of times
        // There are two trade legs and a cashflow is generated for each for evey month
        verify(cashflowRepository, times(invocationsCount)).save(any(Cashflow.class));
    }
}
