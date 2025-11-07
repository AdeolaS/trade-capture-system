package com.technicalchallenge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.technicalchallenge.dto.TradeSummaryDTO;
import com.technicalchallenge.mapper.TradeSummaryMapper;
import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.model.Currency;
import com.technicalchallenge.model.PayRec;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.model.TradeLeg;
import com.technicalchallenge.model.TradeStatus;
import com.technicalchallenge.model.TradeSummary;
import com.technicalchallenge.model.TradeType;
import com.technicalchallenge.repository.ApplicationUserRepository;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.CounterpartyRepository;
import com.technicalchallenge.repository.DailySummaryRepository;
import com.technicalchallenge.repository.TradeRepository;
import com.technicalchallenge.repository.TradeStatusRepository;
import com.technicalchallenge.repository.TradeSummaryRepository;
import com.technicalchallenge.repository.TradeTypeRepository;

@ExtendWith(MockitoExtension.class)
public class TradeDashboardServiceTest {
    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeStatusRepository tradeStatusRepository;

    @Mock
    private TradeTypeRepository tradeTypeRepository;

    @Mock
    private CounterpartyRepository counterpartyRepository;

    @Mock
    private TradeSummaryRepository tradeSummaryRepository;

    @Mock
    private DailySummaryRepository dailySummaryRepository;

    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private TradeSummaryMapper tradeSummaryMapper;

    @InjectMocks
    private TradeDashboardService tradeDashboardService;

    private Trade trade;
    private Book activeBook;

    private Trade trade2;
    private Book inactiveBook;

    private ApplicationUser activeUser;
    private ApplicationUser inactiveUser;

    @BeforeEach
    void setUp() {
        activeUser = new ApplicationUser();
        activeUser.setId(15L);
        activeUser.setLoginId("user123");
        activeUser.setActive(true);

        inactiveUser = new ApplicationUser();
        inactiveUser.setId(16L);
        inactiveUser.setLoginId("inactiveUser");
        inactiveUser.setActive(false);

        activeBook = new Book();
        activeBook.setId(1L);
        activeBook.setBookName("EQUITY-DESK");
        activeBook.setActive(true);

        trade = new Trade();
        trade.setTradeId(101L);
        trade.setBook(activeBook);

        inactiveBook = new Book();
        inactiveBook.setId(1L);
        inactiveBook.setBookName("RATES-BOOK");
        inactiveBook.setActive(false);

        trade2 = new Trade();
        trade2.setTradeId(101L);
        trade2.setBook(activeBook);
    }

    @Test
    void testGetPersonalTrades_Success() {
        when(applicationUserRepository.findByLoginId("user123")).thenReturn(Optional.of(activeUser));
        when(tradeRepository.findByTraderUser_Id(15L)).thenReturn(List.of(trade, trade2));

        List<Trade> result = tradeDashboardService.getPersonalTrades("user123");

        assertEquals(2, result.size());
        verify(tradeRepository).findByTraderUser_Id(15L);
    }

    @Test
    void testGetPersonalTrades_UserNotFound() {
        when(applicationUserRepository.findByLoginId("missingUser")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tradeDashboardService.getPersonalTrades("missingUser"));

        assertEquals("User not found with login ID: missingUser", exception.getMessage());
        verify(tradeRepository, never()).findByTraderUser_Id(anyLong());
    }

    @Test
    void testGetPersonalTrades_UserInactive() {
        when(applicationUserRepository.findByLoginId("inactiveUser")).thenReturn(Optional.of(inactiveUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tradeDashboardService.getPersonalTrades("inactiveUser"));

        assertEquals("User is inactive: inactiveUser", exception.getMessage());
        verify(tradeRepository, never()).findByTraderUser_Id(anyLong());
    }

    // @Test
    // void testGetTradesByBook_Success() {
    //     when(applicationUserRepository.findByLoginId("user123")).thenReturn(Optional.of(activeUser));
    //     when(bookRepository.findByBookName("EQUITY-DESK")).thenReturn(Optional.of(activeBook));
    //     when(tradeRepository.findByTraderUser_IdAndBook_Id(99L, 15L)).thenReturn(List.of(trade));

    //     List<Trade> result = tradeDashboardService.getTradesByBook("EQUITY-DESK", "user123");

    //     assertEquals(1, result.size());
    //     assertEquals(101L, result.get(0).getTradeId());
    //     verify(tradeRepository).findByTraderUser_IdAndBook_Id(99L, 15L);
    // }

    @Test
    void testGetTradesByBook_UserNotFound() {
        when(applicationUserRepository.findByLoginId("ghostUser")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tradeDashboardService.getTradesByBook("EQUITY-DESK", "ghostUser"));

        assertEquals("User not found with login ID: ghostUser", exception.getMessage());
        verify(tradeRepository, never()).findByTraderUser_IdAndBook_Id(anyLong(), anyLong());
    }

    @Test
    void testGetTradesByBook_UserInactive() {
        when(applicationUserRepository.findByLoginId("inactiveUser")).thenReturn(Optional.of(inactiveUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tradeDashboardService.getTradesByBook("EQUITY-DESK", "inactiveUser"));

        assertEquals("User is inactive: inactiveUser", exception.getMessage());
        verify(tradeRepository, never()).findByTraderUser_IdAndBook_Id(anyLong(), anyLong());
    }

    @Test
    void testGetTradesByBook_BookNotFound() {
        when(applicationUserRepository.findByLoginId("user123")).thenReturn(Optional.of(activeUser));
        when(bookRepository.findByBookName("MISSING-BOOK")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tradeDashboardService.getTradesByBook("MISSING-BOOK", "user123"));

        assertEquals("Book not found: MISSING-BOOK", exception.getMessage());
        verify(tradeRepository, never()).findByTraderUser_IdAndBook_Id(anyLong(), anyLong());
    }

    @Test
    void testGetTradesByBook_BookInactive() {
        when(applicationUserRepository.findByLoginId("user123")).thenReturn(Optional.of(activeUser));
        when(bookRepository.findByBookName("RATES-BOOK")).thenReturn(Optional.of(inactiveBook));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tradeDashboardService.getTradesByBook("RATES-BOOK", "user123"));

        assertEquals("Book is inactive: RATES-BOOK", exception.getMessage());
        verify(tradeRepository, never()).findByTraderUser_IdAndBook_Id(anyLong(), anyLong());
    }

    @Test
    void testGetTradeSummaryForUser_Success() {
        // given
        when(applicationUserRepository.findByLoginId("user123"))
                .thenReturn(Optional.of(activeUser));

        TradeStatus status = new TradeStatus();
        status.setTradeStatus("NEW");

        TradeType type = new TradeType();
        type.setTradeType("SPOT");

        Counterparty cp = new Counterparty();
        cp.setName("ABC_BANK");

        // TradeLeg with notional + currency
        TradeLeg leg = new TradeLeg();
        Currency currency = new Currency();
        currency.setCurrency("USD");
        leg.setCurrency(currency);
        leg.setNotional(BigDecimal.valueOf(1000));
        PayRec flag = new PayRec();
        flag.setPayRec("RECEIVE");
        leg.setPayReceiveFlag(flag);

        trade.setTradeStatus(status);
        trade.setTradeType(type);
        trade.setCounterparty(cp);
        trade.setTradeLegs(List.of(leg));

        when(tradeRepository.findByTraderUser_Id(activeUser.getId()))
                .thenReturn(List.of(trade));

        when(tradeStatusRepository.findAll()).thenReturn(List.of(status));
        when(tradeTypeRepository.findAll()).thenReturn(List.of(type));
        when(counterpartyRepository.findAll()).thenReturn(List.of(cp));

        when(tradeSummaryRepository.save(any(TradeSummary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TradeSummaryDTO dto = new TradeSummaryDTO();
        when(tradeSummaryMapper.toDto(any(TradeSummary.class))).thenReturn(dto);

        // when
        TradeSummaryDTO result = tradeDashboardService.getTradeSummaryForUser("user123");

        // then
        assertNotNull(result);
        verify(tradeSummaryRepository).save(any(TradeSummary.class));
        verify(tradeSummaryMapper).toDto(any(TradeSummary.class));
    }

    @Test
    // Test that an empty trade list doesn’t crash the method and still produces a summary.
    void testGetTradeSummaryForUser_NoTrades() {
        when(applicationUserRepository.findByLoginId("user123"))
                .thenReturn(Optional.of(activeUser));

        when(tradeRepository.findByTraderUser_Id(activeUser.getId()))
                .thenReturn(Collections.emptyList());

        when(tradeSummaryMapper.toDto(any(TradeSummary.class))).thenReturn(new TradeSummaryDTO());

        TradeSummaryDTO result = tradeDashboardService.getTradeSummaryForUser("user123");

        assertNotNull(result);
        verify(tradeRepository).findByTraderUser_Id(activeUser.getId());
    }

    @Test
    void testGetHistoricalTradeSummaries_UserNotFound() {
        when(applicationUserRepository.findByLoginId("nonexistentUser"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tradeDashboardService.getHistoricalTradeSummaries("nonexistentUser", LocalDate.now()));

        assertTrue(exception.getMessage().contains("User not found with login ID"));
    }

    @Test
    void testGetHistoricalTradeSummaries_UserInactive() {
        when(applicationUserRepository.findByLoginId("inactiveUser"))
                .thenReturn(Optional.of(inactiveUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tradeDashboardService.getHistoricalTradeSummaries("inactiveUser", LocalDate.now()));

        assertTrue(exception.getMessage().contains("User is inactive"));
    }

    @Test
    void testGetHistoricalTradeSummaries_Success() {
        when(applicationUserRepository.findByLoginId("user123"))
                .thenReturn(Optional.of(activeUser));

        TradeSummary summary1 = new TradeSummary();
        TradeSummary summary2 = new TradeSummary();

        TradeSummaryDTO dto1 = new TradeSummaryDTO();
        TradeSummaryDTO dto2 = new TradeSummaryDTO();

        when(tradeSummaryRepository.findByTraderUser_IdAndSummaryDateStamp(activeUser.getId(), LocalDate.now()))
                .thenReturn(List.of(summary1, summary2));

        when(tradeSummaryMapper.toDto(summary1)).thenReturn(dto1);
        when(tradeSummaryMapper.toDto(summary2)).thenReturn(dto2);

        List<TradeSummaryDTO> result =
                tradeDashboardService.getHistoricalTradeSummaries("user123", LocalDate.now());

        assertEquals(2, result.size());
        verify(tradeSummaryMapper, times(2)).toDto(any(TradeSummary.class));
    }

    @Test
    void testGetTradeSummaryForUser_ComputesCorrectValues() {
        // === Arrange ===
        when(applicationUserRepository.findByLoginId("user123"))
                .thenReturn(Optional.of(activeUser));

        // --- Trade setup ---
        TradeStatus status1 = new TradeStatus();
        status1.setTradeStatus("NEW");

        TradeStatus status2 = new TradeStatus();
        status2.setTradeStatus("CANCELLED");

        TradeType type1 = new TradeType();
        type1.setTradeType("SPOT");

        Counterparty cp1 = new Counterparty();
        cp1.setName("BankA");

        Counterparty cp2 = new Counterparty();
        cp2.setName("BankB");

        Currency usd = new Currency();
        usd.setCurrency("USD");

        // TradeLeg 1 — PAY 1000 USD
        TradeLeg leg1 = new TradeLeg();
        leg1.setCurrency(usd);
        leg1.setNotional(BigDecimal.valueOf(1000));
        PayRec payFlag = new PayRec();
        payFlag.setPayRec("PAY");
        leg1.setPayReceiveFlag(payFlag);

        // TradeLeg 2 — RECEIVE 500 USD
        TradeLeg leg2 = new TradeLeg();
        leg2.setCurrency(usd);
        leg2.setNotional(BigDecimal.valueOf(500));
        PayRec recFlag = new PayRec();
        recFlag.setPayRec("RECEIVE");
        leg2.setPayReceiveFlag(recFlag);

        // Trade #1 (NEW / SPOT / BankA)
        Trade trade1 = new Trade();
        trade1.setTradeStatus(status1);
        trade1.setTradeType(type1);
        trade1.setCounterparty(cp1);
        trade1.setBook(activeBook);
        trade1.setTradeLegs(List.of(leg1, leg2));

        // Trade #2 (CANCELLED / SPOT / BankB)
        Trade trade2 = new Trade();
        trade2.setTradeStatus(status2);
        trade2.setTradeType(type1);
        trade2.setCounterparty(cp2);
        trade2.setBook(activeBook);
        trade2.setTradeLegs(List.of(leg1)); // PAY leg only

        // Repositories return
        when(tradeRepository.findByTraderUser_Id(activeUser.getId()))
                .thenReturn(List.of(trade1, trade2));

        when(tradeStatusRepository.findAll()).thenReturn(List.of(status1, status2));
        when(tradeTypeRepository.findAll()).thenReturn(List.of(type1));
        when(counterpartyRepository.findAll()).thenReturn(List.of(cp1, cp2));

        // Mapper behavior — return DTO with content copied from TradeSummary
        when(tradeSummaryRepository.save(any(TradeSummary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tradeSummaryMapper.toDto(any(TradeSummary.class)))
                .thenAnswer(invocation -> {
                    TradeSummary summary = invocation.getArgument(0);
                    TradeSummaryDTO dto = new TradeSummaryDTO();
                    dto.setTradeCountByStatus(summary.getTradeCountByStatus());
                    dto.setTradeCountByTradeType(summary.getTradeCountByTradeType());
                    dto.setTradeCountByCounterparty(summary.getTradeCountByCounterparty());
                    dto.setTotalNotionalByCurrency(summary.getTotalNotionalByCurrency());
                    dto.setRiskExposure(summary.getRiskExposure());
                    return dto;
                });

        // === Act ===
        TradeSummaryDTO result = tradeDashboardService.getTradeSummaryForUser("user123");

        // === Assert ===
        assertNotNull(result);

        // ✅ Status counts: NEW=1, CANCELLED=1
        assertEquals(2, result.getTradeCountByStatus().size());
        assertEquals(1L, result.getTradeCountByStatus().get("NEW"));
        assertEquals(1L, result.getTradeCountByStatus().get("CANCELLED"));

        // ✅ Trade type counts: SPOT=2
        assertEquals(1, result.getTradeCountByTradeType().size());
        assertEquals(2L, result.getTradeCountByTradeType().get("SPOT"));

        // ✅ Counterparty counts: BankA=1, BankB=1
        assertEquals(2, result.getTradeCountByCounterparty().size());
        assertEquals(1L, result.getTradeCountByCounterparty().get("BANKA"));
        assertEquals(1L, result.getTradeCountByCounterparty().get("BANKB"));

        // ✅ Total notional by currency: USD = 1000 + 500 + 1000 = 2500
        assertEquals(BigDecimal.valueOf(2500),
                result.getTotalNotionalByCurrency().get("USD"));

        // ✅ Risk exposure by book:
        // Book "EQUITY-DESK" = RECEIVE(500) - PAY(1000 + 1000) = -1500
        BigDecimal exposure = result.getRiskExposure().get("EQUITY-DESK");
        assertEquals(BigDecimal.valueOf(-1500), exposure);
    }


   
}
