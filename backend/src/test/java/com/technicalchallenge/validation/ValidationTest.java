package com.technicalchallenge.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.CostCenter;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.model.Desk;
import com.technicalchallenge.model.SubDesk;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.model.TradeLeg;
import com.technicalchallenge.model.TradeStatus;
import com.technicalchallenge.model.TradeSubType;
import com.technicalchallenge.model.TradeType;
import com.technicalchallenge.model.ValidationResult;
import com.technicalchallenge.repository.ApplicationUserRepository;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.CounterpartyRepository;
import com.technicalchallenge.repository.TradeStatusRepository;

@ExtendWith(MockitoExtension.class)
public class ValidationTest {

    @Mock
    private TradeStatusRepository tradeStatusRepository;
    @Mock
    private ApplicationUserRepository applicationUserRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CounterpartyRepository counterpartyRepository;

    private LocalDate earliestTradeDate;
    private LocalDate latestTradeDate;

    private TradeDTO tradeDTO;
    private ValidationResult result;

    private TradeLegDTO leg1;
    private TradeLegDTO leg2;

    private Trade trade;
    private Book book;
    private Counterparty counterparty;
    private TradeStatus tradeStatus;
    private ApplicationUser traderUser;
    private TradeType tradeType;
    private TradeSubType tradeSubType;


    @BeforeEach
    void setUp(){
        earliestTradeDate = LocalDate.now().minusMonths(10);
        latestTradeDate = LocalDate.now().plusMonths(2);

        tradeDTO = new TradeDTO();

        leg1 = new TradeLegDTO();
        leg1.setLegType("Floating");
        leg1.setPayReceiveFlag("Pay");
        leg1.setCurrencyId(1000L);
        leg1.setScheduleId(1000L);
        leg1.setHolidayCalendarId(1000L);
        leg1.setPaymentBdcId(1000L);
        leg1.setFixingBdcId(1000L);
        leg1.setIndexId(1000L);
        leg1.setNotional(BigDecimal.valueOf(1_000_000));

        leg2 = new TradeLegDTO();
        leg2.setLegType("Fixed");
        leg2.setPayReceiveFlag("Receive");
        leg2.setCurrencyId(1000L);
        leg2.setScheduleId(1000L);
        leg2.setHolidayCalendarId(1000L);
        leg2.setPaymentBdcId(1000L);
        leg2.setFixingBdcId(1000L);
        leg2.setRate(0.05);
        leg2.setNotional(BigDecimal.valueOf(1_000_000));

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

        trade = new Trade();
        trade.setId(1L);
        trade.setTradeId(100001L);
        trade.setBook(book);
        trade.setCounterparty(counterparty);
        trade.setTradeStatus(tradeStatus);
        trade.setTraderUser(traderUser);
        trade.setTradeType(tradeType);
        trade.setTradeSubType(tradeSubType);
    }

    @Test
    void testValidateReferenceData_AllValid() {
        
        result = Validation.validateReferenceData(trade);

        assertTrue(result.isValid(), "Expected trade to be valid");
        assertTrue(result.getValidationErrors().isEmpty(), "Expected no validation errors");
    }

    @Test
    void testValidateReferenceData_MissingBook() {
        trade.setBook(null);

        result = Validation.validateReferenceData(trade);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().contains("book")
                        && e.getErrorMessage().contains("Book not found or not set")));
    }

    @Test
    void testValidateReferenceData_InactiveBook() {
        book.setActive(false);
        trade.setBook(book);

        result = Validation.validateReferenceData(trade);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("book") &&
                               e.getErrorMessage().contains("Book must be active")));
    }

    @Test
    void testValidateReferenceData_MissingCostCenter() {
        book.setCostCenter(null);
        trade.setBook(book);

        result = Validation.validateReferenceData(trade);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("costCenter") &&
                               e.getErrorMessage().contains("Book has no associated cost center")));
    }

    @Test
    void testValidateReferenceData_MissingSubDesk() {
        book.getCostCenter().setSubDesk(null);

        result = Validation.validateReferenceData(trade);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("subDesk") &&
                               e.getErrorMessage().contains("Cost center has no associated subdesk")));
    }

    @Test
    void testValidateReferenceData_MissingDesk() {
        book.getCostCenter().getSubDesk().setDesk(null);

        result = Validation.validateReferenceData(trade);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("desk") &&
                               e.getErrorMessage().contains("Subdesk has no associated desk")));
    }

    @Test
    void testValidateReferenceData_InactiveCounterparty() {
        counterparty.setActive(false);

        result = Validation.validateReferenceData(trade);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("counterparty") &&
                               e.getErrorMessage().contains("Counterparty must be active")));
    }

    @Test
    void testValidateReferenceData_MissingCounterparty() {
        trade.setCounterparty(null);

        result = Validation.validateReferenceData(trade);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("counterparty") &&
                               e.getErrorMessage().contains("Counterparty not found or not set")));
    }

    @Test
    void testValidateReferenceData_InactiveTraderUser() {
        traderUser.setActive(false);

        result = Validation.validateReferenceData(trade);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("traderUser") &&
                               e.getErrorMessage().contains("Trader User must be active")));
    }

    @Test
    void testValidateReferenceData_MissingTraderUser() {
        trade.setTraderUser(null);

        result = Validation.validateReferenceData(trade);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("traderUser") &&
                               e.getErrorMessage().contains("Trader User not found or not set")));
    }

    @Test
    void testValidateReferenceData_MissingTradeStatus() {
        trade.setTradeStatus(null);

        result = Validation.validateReferenceData(trade);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("tradeStatus") &&
                               e.getErrorMessage().contains("Trade status not found or not set")));
    }

    @Test
    void testValidateReferenceData_MissingTradeType() {
        trade.setTradeType(null);

        result = Validation.validateReferenceData(trade);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("tradeType") &&
                               e.getErrorMessage().contains("Trade type not found or not set")));
    }

    @Test
    void testValidateReferenceData_MissingTradeSubType() {
        trade.setTradeSubType(null);

        result = Validation.validateReferenceData(trade);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("tradeSubType") &&
                               e.getErrorMessage().contains("Trade sub-type not found or not set")));
    }

    @Test
    void testValidateTradeLegConsistency_AllValid_NoErrors() {
        List<TradeLegDTO> legs = Arrays.asList(leg1, leg2);
        result = Validation.validateTradeLegConsistency(legs);

        assertTrue(result.isValid(), "Expected no validation errors for valid legs");
        assertTrue(result.getValidationErrors().isEmpty());
    }

    @Test
    void testValidateTradeLegConsistency_NullLegs_ShouldReturnError() {
        result = Validation.validateTradeLegConsistency(null);

        assertFalse(result.isValid(), "Validation should fail when legs are null");
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("tradeLegs") &&
                        e.getErrorMessage().contains("Trade must have exactly two legs")));
    }

    @Test
    void testValidateTradeLegConsistency_OnlyOneLeg_ShouldReturnError() {
        result = Validation.validateTradeLegConsistency(Collections.singletonList(leg1));

        assertFalse(result.isValid(), "Validation should fail when only one leg is provided");
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("tradeLegs") &&
                        e.getErrorMessage().contains("Trade must have exactly two legs")));
    }

    @Test
    void testValidateTradeLegConsistency_SamePayReceiveFlag_ShouldReturnError() {
        leg2.setPayReceiveFlag("Pay"); // Both legs are "Pay"
        result = Validation.validateTradeLegConsistency(Arrays.asList(leg1, leg2));

        assertFalse(result.isValid(), "Validation should fail when both legs have the same Pay/Receive flag");
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("payReceiveFlag") &&
                        e.getErrorMessage().contains("Legs must have opposite pay/receive flags (one PAY, one RECEIVE)")));
    }

    @Test
    void testValidateTradeLegConsistency_MissingPayReceiveFlag_ShouldReturnError() {
        leg1.setPayReceiveFlag(null);
        result = Validation.validateTradeLegConsistency(Arrays.asList(leg1, leg2));

        assertFalse(result.isValid(), "Validation should fail when pay/receive flag is missing");
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("payReceiveFlag") &&
                        e.getErrorMessage().contains("Both legs must specify a pay/receive flag")));
    }

    @Test
    void testValidateTradeLegConsistency_BlankLegType_ShouldReturnError() {
        leg1.setLegType("");
        result = Validation.validateTradeLegConsistency(Arrays.asList(leg1, leg2));

        assertFalse(result.isValid(), "Validation should fail when leg type is blank");
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().contains("leg[1].legType") &&
                        e.getErrorMessage().contains("Leg type not set")));
    }

    @Test
    void testValidateTradeLegConsistency_NullCurrency_ShouldReturnError() {
        leg1.setCurrency(null);
        leg1.setCurrencyId(null);
        result = Validation.validateTradeLegConsistency(Arrays.asList(leg1, leg2));

        assertFalse(result.isValid(), "Validation should fail when currency is not set");
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().contains("leg[1].currency") &&
                        e.getErrorMessage().contains("Currency not set")));
    }

    @Test
    void testValidateTradeLegConsistency_NegativeNotional_ShouldReturnError() {
        leg1.setNotional(BigDecimal.valueOf(-500000));
        result = Validation.validateTradeLegConsistency(Arrays.asList(leg1, leg2));

        assertFalse(result.isValid(), "Validation should fail when notional is negative");
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().contains("leg[1].notional") &&
                        e.getErrorMessage().contains("Leg must have a positive notional")));
    }

    @Test
    void testValidateTradeLegConsistency_FixedLegZeroRate_ShouldReturnError() {
        leg2.setRate(0.0);
        result = Validation.validateTradeLegConsistency(Arrays.asList(leg1, leg2));

        assertFalse(result.isValid(), "Validation should fail when fixed leg rate is zero");
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().contains("leg[2].rate") &&
                        e.getErrorMessage().contains("Fixed leg must have a rate greater than zero")));
    }

    @Test
    void testValidateTradeLegConsistency_FloatingLegMissingIndex_ShouldReturnError() {
        leg1.setIndexName(null);
        leg1.setIndexId(null);
        result = Validation.validateTradeLegConsistency(Arrays.asList(leg1, leg2));

        assertFalse(result.isValid(), "Validation should fail when floating leg index is missing");
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().contains("leg[1].index") &&
                        e.getErrorMessage().contains("Floating legs must have an index specified")));
    }

    @Test
    void testValidateTradeLegConsistency_MissingHolidayCalendar_ShouldReturnError() {
        leg1.setHolidayCalendar(null);
        leg1.setHolidayCalendarId(null);
        result = Validation.validateTradeLegConsistency(Arrays.asList(leg1, leg2));

        assertFalse(result.isValid(), "Validation should fail when holiday calendar is missing");
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().contains("leg[1].holidayCalendar") &&
                        e.getErrorMessage().contains("Holiday calendar not set")));
    }

    @Test
    void testValidateTradeLegConsistency_MissingPaymentBusinessDayConvention_ShouldReturnError() {
        leg1.setPaymentBdcId(null);
        leg1.setPaymentBusinessDayConvention(null);
        result = Validation.validateTradeLegConsistency(Arrays.asList(leg1, leg2));

        assertFalse(result.isValid(), "Validation should fail when Payment Business day convention is missing");
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().contains("leg[1].businessDayConvention") &&
                        e.getErrorMessage().contains("Payment Business day convention not set")));
    }

    @Test
    void testValidateTradeLegConsistency_MissingFixingBusinessDayConvention_ShouldReturnError() {
        leg1.setFixingBdcId(null);
        leg1.setFixingBusinessDayConvention(null);
        result = Validation.validateTradeLegConsistency(Arrays.asList(leg1, leg2));

        assertFalse(result.isValid(), "Validation should fail when Fixing Business day convention is missing");
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().contains("leg[1].businessDayConvention") &&
                        e.getErrorMessage().contains("Fixing Business day convention not set")));
    }

    @Test
    void testValidateTradeLegConsistency_MissingSchedule_ShouldReturnError() {
        leg1.setScheduleId(null);
        leg1.setCalculationPeriodSchedule(null);
        result = Validation.validateTradeLegConsistency(Arrays.asList(leg1, leg2));

        assertFalse(result.isValid(), "Validation should fail when schedule is missing");
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().contains("leg[1].schedule") &&
                        e.getErrorMessage().contains("Schedule not set")));
    }

    @Test
    void testValidateTradeLegConsistency_BothLegTypesBlank_ShouldReturnTwoErrors() {
        leg1.setLegType("");
        leg2.setLegType("");
        result = Validation.validateTradeLegConsistency(Arrays.asList(leg1, leg2));

        assertFalse(result.isValid(), "Validation should fail when both leg types are blank");
        assertEquals(2, result.getValidationErrors().size(), "Expected two field-level errors");
        assertTrue(result.getValidationErrors().stream()
                .allMatch(e -> e.getFieldName().contains("leg") && e.getErrorMessage().contains("Leg type not set")));
    }

    @Test
    void testValidateTradeLegConsistency_NullNotional_ShouldReturnError() {
        leg1.setNotional(null);
        result = Validation.validateTradeLegConsistency(Arrays.asList(leg1, leg2));

        assertFalse(result.isValid(), "Validation should fail when one leg has null notional");
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().contains("leg[1].notional") &&
                        e.getErrorMessage().contains("Leg must have a positive notional")));
    }

    @Test
    void testValidateTradeLegConsistency_PayReceiveCaseInsensitive_ShouldPass() {
        leg1.setPayReceiveFlag("pay");
        leg2.setPayReceiveFlag("RECEIVE");

        result = Validation.validateTradeLegConsistency(Arrays.asList(leg1, leg2));

        assertTrue(result.isValid(), "Validation should pass when Pay/Receive flags differ only by case");
    }



    @ParameterizedTest
    @CsvSource({"0,1,2", "4,7,9", "100,200,300", "250,250,300"})
    void testValidateTradeBusinessRules_AllValid_NoErrors(int addToDate, int addToStart, int addToMaturity) {
        tradeDTO.setTradeDate(LocalDate.now().plusDays(addToDate));
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(addToStart));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusMonths(addToMaturity));

        result = Validation.validateTradeBusinessRules(tradeDTO);

        assertTrue(result.isValid(), "Expected no validation errors for valid trade");
        assertTrue(result.getValidationErrors().isEmpty(), "Validation errors list should be empty");
    }

    @ParameterizedTest
    @CsvSource({"40,1,2", "140,10,9", "200,20,300", "31,25,300"})
    void testValidateTradeBusinessRules_TradeDateTooOld_ShouldReturnError(int addToDate, int addToStart, int addToMaturity) {
        tradeDTO.setTradeDate(LocalDate.now().minusDays(40));
        tradeDTO.setTradeStartDate(LocalDate.now().minusDays(10));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusDays(10));

        result = Validation.validateTradeBusinessRules(tradeDTO);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("tradeDate") &&
                                e.getErrorMessage().contains("must not be more than 30 days before today's date")));
    }

    @Test
    void testValidateTradeBusinessRules_TradeDateNull_ShouldReturnError() {
        tradeDTO.setTradeDate(null);
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(2));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusMonths(1));

        result = Validation.validateTradeBusinessRules(tradeDTO);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("tradeDate") &&
                                e.getErrorMessage().contains("Trade date is required")));
    }

    @ParameterizedTest
    @CsvSource({"40,1,200", "140,10,900", "200,20,3000", "31,25,3000"})
    void testValidateTradeBusinessRules_StartDateBeforeTradeDate_ShouldReturnError(int addToDate, int addToStart, int addToMaturity) {
        tradeDTO.setTradeDate(LocalDate.now().plusDays(addToDate));
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(addToStart));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusDays(addToMaturity));

        result = Validation.validateTradeBusinessRules(tradeDTO);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("tradeStartDate")
                        && e.getErrorMessage().contains("Start date cannot be before trade date")));
    }

    @Test
    void testValidateTradeBusinessRules_StartDateNull_ShouldReturnError() {
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(null);
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusMonths(1));

        result = Validation.validateTradeBusinessRules(tradeDTO);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("tradeStartDate")
                        && e.getErrorMessage().contains("Trade start date is required")));
    }

    @ParameterizedTest
    @CsvSource({"40,100,20", "140,200,10", "200,201,30", "31,250,30"})
    void testValidateTradeBusinessRules_MaturityBeforeStart_ShouldReturnError(int addToDate, int addToStart, int addToMaturity) {
        tradeDTO.setTradeDate(LocalDate.now().plusDays(addToDate));
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(addToStart));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusDays(addToMaturity));

        result = Validation.validateTradeBusinessRules(tradeDTO);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("tradeMaturityDate")
                        && e.getErrorMessage().contains("Maturity date cannot be before start date or trade date")));
    }

    @ParameterizedTest
    @CsvSource({"40,100,60", "140,200,180", "200,1200,300", "31,2500,560"})
    void testValidateTradeBusinessRules_MaturityBeforeTradeDate_ShouldReturnError(int addToDate, int addToStart, int addToMaturity) {
        tradeDTO.setTradeDate(LocalDate.now().plusDays(addToDate));
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(addToStart));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusDays(addToMaturity));

        result = Validation.validateTradeBusinessRules(tradeDTO);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("tradeMaturityDate")
                        && e.getErrorMessage().contains("Maturity date cannot be before start date or trade date")));
    }

    @Test
    void testValidateTradeBusinessRules_MaturityNull_ShouldReturnError() {
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(3));
        tradeDTO.setTradeMaturityDate(null);

        result = Validation.validateTradeBusinessRules(tradeDTO);

        assertFalse(result.isValid());
        assertTrue(result.getValidationErrors().stream()
                .anyMatch(e -> e.getFieldName().equals("tradeMaturityDate")
                        && e.getErrorMessage().contains("Trade maturity date is required")));
    }

    @Test
    void testValidateTradeBusinessRules_AllDatesNull_ShouldReturnMultipleErrors() {
        tradeDTO.setTradeDate(null);
        tradeDTO.setTradeStartDate(null);
        tradeDTO.setTradeMaturityDate(null);

        result = Validation.validateTradeBusinessRules(tradeDTO);

        assertFalse(result.isValid());
        assertEquals(3, result.getValidationErrors().size(), "Expected 3 missing field errors");
    }

    @Test
    void testValidateSearchParameters_AllValid_NoException() {
        when(tradeStatusRepository.existsById(1L)).thenReturn(true);
        when(applicationUserRepository.existsById(2L)).thenReturn(true);
        when(bookRepository.existsById(3L)).thenReturn(true);
        when(counterpartyRepository.existsById(4L)).thenReturn(true);

        assertDoesNotThrow(() -> Validation.validateSearchParameters(
                earliestTradeDate, latestTradeDate, 1L, 2L, 3L, 4L,
                tradeStatusRepository, applicationUserRepository, bookRepository, counterpartyRepository
        ));
    }

    @Test
    void testValidateSearchParameters_ShouldThrow_WhenEarlierDateIsLater() {

        // Make earliest date later than latest date
        earliestTradeDate = LocalDate.now().plusMonths(10);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                Validation.validateSearchParameters(earliestTradeDate, latestTradeDate, null, null, null, null,
                tradeStatusRepository, applicationUserRepository, bookRepository, counterpartyRepository
        ));

        assertTrue(exception.getMessage().contains("Earliest date must be before latest date"));
    }

    @Test
    void testValidateSearchParameters_InvalidTradeStatus_ThrowsException() {
        when(tradeStatusRepository.existsById(99L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                Validation.validateSearchParameters(
                        null, null, 99L, null, null, null,
                        tradeStatusRepository, applicationUserRepository, bookRepository, counterpartyRepository
                )
        );

        assertTrue(exception.getMessage().contains("Trade status ID does not exist in the database"));
    }

    @Test
    void testValidateSearchParameters_InvalidTrader_ThrowsException() {
        when(applicationUserRepository.existsById(77L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                Validation.validateSearchParameters(
                        earliestTradeDate, latestTradeDate, null, 77L, null, null,
                        tradeStatusRepository, applicationUserRepository, bookRepository, counterpartyRepository
                )
        );

        assertTrue(exception.getMessage().contains("Trader user ID does not exist in the database"));
    }

    @Test
    void testValidateSearchParameters_InvalidBook_ThrowsException() {
        when(bookRepository.existsById(55L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                Validation.validateSearchParameters(
                        earliestTradeDate, latestTradeDate, null, null, 55L, null,
                        tradeStatusRepository, applicationUserRepository, bookRepository, counterpartyRepository
                )
        );

        assertTrue(exception.getMessage().contains("Book ID does not exist in the database"));
    }

    @Test
    void testValidateSearchParameters_InvalidCounterparty_ThrowsException() {
        when(counterpartyRepository.existsById(44L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                Validation.validateSearchParameters(
                        earliestTradeDate, latestTradeDate, null, null, null, 44L,
                        tradeStatusRepository, applicationUserRepository, bookRepository, counterpartyRepository
                )
        );

        assertTrue(exception.getMessage().contains("Counterparty ID does not exist in the database"));
    }

    @Test
    void testValidateSearchParameters_MultipleInvalidConditions_ThrowsCombinedMessage() {
        earliestTradeDate = LocalDate.now().plusMonths(10); // invalid order
        when(tradeStatusRepository.existsById(99L)).thenReturn(false);
        when(applicationUserRepository.existsById(77L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                Validation.validateSearchParameters(
                        earliestTradeDate, latestTradeDate, 99L, 77L, null, null,
                        tradeStatusRepository, applicationUserRepository, bookRepository, counterpartyRepository
                )
        );

        String message = exception.getMessage();
        assertTrue(message.contains("Earliest date must be before latest date"));
        assertTrue(message.contains("Trade status ID does not exist"));
        assertTrue(message.contains("Trader user ID does not exist"));
    }

    @Test
    void testValidateSearchParameters_AllNull_NoException() {
        assertDoesNotThrow(() -> Validation.validateSearchParameters(
                null, null, null, null, null, null,
                tradeStatusRepository, applicationUserRepository, bookRepository, counterpartyRepository
        ));
    }

    @Test
    void testValidatePaginationParams_ValidParams() {
        // Should not throw any exception
        assertDoesNotThrow(() -> Validation.validatePaginationParams(0, 1));
    }

    @Test
    void testValidatePaginationParams_AllInvalidParams_CombinedMessage() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            Validation.validatePaginationParams(-1, 0)
        );

        assertTrue(exception.getMessage().contains("Requested Page number must be non-negative"));
        assertTrue(exception.getMessage().contains("Page size must be more than zero"));
    }

    @Test
    void testValidatePaginationParams_InvalidPageNumber_ErrorMessage() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            Validation.validatePaginationParams(-1, 2)
        );

        assertTrue(exception.getMessage().contains("Requested Page number must be non-negative"));
    }

    @Test
    void testValidatePaginationParams_InvalidPageSize_ErrorMessage() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            Validation.validatePaginationParams(5, 0)
        );

        assertTrue(exception.getMessage().contains("Page size must be more than zero"));
    }

}
