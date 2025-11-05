package com.technicalchallenge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.Trade;
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

    @Test
    void testGetTradesByBook_Success() {
        when(applicationUserRepository.findByLoginId("user123")).thenReturn(Optional.of(activeUser));
        when(bookRepository.findByBookName("EQUITY-DESK")).thenReturn(Optional.of(activeBook));
        when(tradeRepository.findByTraderUser_IdAndBook_Id(99L, 15L)).thenReturn(List.of(trade));

        List<Trade> result = tradeDashboardService.getTradesByBook("EQUITY-DESK", "user123");

        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).getTradeId());
        verify(tradeRepository).findByTraderUser_IdAndBook_Id(99L, 15L);
    }

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

   
}
