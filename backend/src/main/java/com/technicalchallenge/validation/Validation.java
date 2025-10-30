package com.technicalchallenge.validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.model.CostCenter;
import com.technicalchallenge.model.Desk;
import com.technicalchallenge.model.SubDesk;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.model.ValidationResult;
import com.technicalchallenge.repository.ApplicationUserRepository;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.CounterpartyRepository;
import com.technicalchallenge.repository.TradeStatusRepository;
import com.technicalchallenge.service.TradeService;

public class Validation {

    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);
    
    public static void validateSearchParameters(LocalDate earliestTradeDate, LocalDate latestTradeDate, Long tradeStatusId, Long traderId, Long bookId, Long counterpartyId,
                    TradeStatusRepository tradeStatusRepository, ApplicationUserRepository applicationUserRepository, BookRepository bookRepository, CounterpartyRepository counterpartyRepository) {
        logger.info("Validating search parameters");

        String errorMessage = "";
        if (latestTradeDate != null && earliestTradeDate != null && latestTradeDate.isBefore(earliestTradeDate)) {
            errorMessage += "\n Earliest date must be before latest date";
        }
        if (tradeStatusId != null && !tradeStatusRepository.existsById(tradeStatusId)) {
            errorMessage += "\n Trade status ID does not exist in the database";
        }
        if (traderId != null && !applicationUserRepository.existsById(traderId)) {
            errorMessage += "\n Trader user ID does not exist in the database";
        }
        if (bookId != null && !bookRepository.existsById(bookId)) {
            errorMessage += "\n Book ID does not exist in the database";
        }
        if (counterpartyId != null && !counterpartyRepository.existsById(counterpartyId)) {
            errorMessage += "\n Counterparty ID does not exist in the database";
        }
        if (!errorMessage.equals("")) {
            throw new RuntimeException(errorMessage);
        }
    }

    public static void validatePaginationParams(int pageNum, int pageSize) {
        logger.info("Validating search parameters");

        String errorMessage = "";
        if (pageNum < 0) {
            errorMessage += "\n Requested Page number must be non-negative";
        }
        if (pageSize <= 0) {
            errorMessage += "\n Page size must be more than zero";
        }
        if (!errorMessage.equals("")) {
            throw new RuntimeException(errorMessage);
        }
    }

    public static ValidationResult validateTradeBusinessRules(TradeDTO tradeDTO) {

        ValidationResult validationResult = new ValidationResult();

        LocalDate tradeDate = tradeDTO.getTradeDate();
        LocalDate startDate = tradeDTO.getTradeStartDate();
        LocalDate maturityDate = tradeDTO.getTradeMaturityDate();

        if (tradeDate != null) {
            if (tradeDate.isBefore(LocalDate.now().minusDays(30))){
                validationResult.addError("tradeDate", "Trade date must not be more than 30 days before today's date", "ERROR");
            }
        } else {
            validationResult.addError("tradeDate", "Trade date is required", "ERROR");
        }

        if (startDate != null) {
            if (tradeDate != null && startDate.isBefore(tradeDate)) {
                validationResult.addError("tradeStartDate", "Start date cannot be before trade date", "ERROR");
            }
        } else {
            validationResult.addError("tradeStartDate", "Trade start date is required", "ERROR");
        }

        if (maturityDate != null) {
            if ((startDate != null && !maturityDate.isAfter(startDate)) ||
                (tradeDate != null && maturityDate.isBefore(tradeDate))) {
                validationResult.addError("tradeMaturityDate", "Maturity date cannot be before start date or trade date", "ERROR");
            }
        } else {
            validationResult.addError("tradeMaturityDate", "Trade maturity date is required", "ERROR");
        }

        return validationResult;
    }

    public static ValidationResult validateTradeLegConsistency(List<TradeLegDTO> legs) {

        ValidationResult validationResult = new ValidationResult();
        // === Basic structure validation ===
        if (legs == null || legs.size() != 2) {
            validationResult.addError("tradeLegs", "Trade must have exactly two legs", "ERROR");
            return validationResult;
        }
        
        TradeLegDTO leg1 = legs.get(0);
        TradeLegDTO leg2 = legs.get(1);
        // === Pay/Receive consistency ===
        if (leg1.getPayReceiveFlag() != null && leg2.getPayReceiveFlag() != null) {
            if (leg1.getPayReceiveFlag().equalsIgnoreCase(leg2.getPayReceiveFlag())) {
                validationResult.addError("payReceiveFlag",
                        "Legs must have opposite pay/receive flags (one PAY, one RECEIVE)", "ERROR");
            }
        } else {
            validationResult.addError("payReceiveFlag", "Both legs must specify a pay/receive flag", "ERROR");
        }
        // === Iterate and validate each leg ===
        int legIndex = 1;
        for (TradeLegDTO leg : legs) {
            String errorPrefix = "leg[" + legIndex + "]";

            // === Leg Type ===
            if (leg.getLegType() == null || leg.getLegType().isBlank()) {
                validationResult.addError(errorPrefix + ".legType", "Leg type not set", "ERROR");
            }

            // === Pay/Receive ===
            if (leg.getPayReceiveFlag() == null || leg.getPayReceiveFlag().isBlank()) {
                validationResult.addError(errorPrefix + ".payReceiveFlag", "Pay/Receive flag not set", "ERROR");
            }

            // === Currency ===
            if (leg.getCurrencyId() == null && (leg.getCurrency() == null || leg.getCurrency().isBlank())) {
                validationResult.addError(errorPrefix + ".currency", "Currency not set", "ERROR");
            }

            // === Schedule ===
            if (leg.getScheduleId() == null && (leg.getCalculationPeriodSchedule() == null || leg.getCalculationPeriodSchedule().isBlank())) {
                validationResult.addError(errorPrefix + ".schedule", "Schedule not set", "ERROR");
            }

            // === Business Day Convention ===
            if (leg.getPaymentBdcId() == null
                    && (leg.getPaymentBusinessDayConvention() == null || leg.getPaymentBusinessDayConvention().isBlank())) {
                validationResult.addError(errorPrefix + ".businessDayConvention", "Payment Business day convention not set", "ERROR");
            }

            if (leg.getFixingBdcId() == null
                    && (leg.getFixingBusinessDayConvention() == null || leg.getFixingBusinessDayConvention().isBlank())) {
                validationResult.addError(errorPrefix + ".businessDayConvention", "Fixing Business day convention not set", "ERROR");
            }

            // === Holiday Calendar ===
            if (leg.getHolidayCalendarId() == null && (leg.getHolidayCalendar() == null || leg.getHolidayCalendar().isBlank())) {
                validationResult.addError(errorPrefix + ".holidayCalendar", "Holiday calendar not set", "ERROR");
            }

            // === Fixed Leg: Rate check ===
            if (leg.getLegType() != null && leg.getLegType().equalsIgnoreCase("Fixed")) {
                if (leg.getRate() == null) {
                    validationResult.addError(errorPrefix + ".rate", "Fixed leg must have a rate specified", "ERROR");
                } else if (leg.getRate() <= 0.0) {
                    validationResult.addError(errorPrefix + ".rate", "Fixed leg must have a rate greater than zero", "ERROR");
                }
            }

            // === Floating Leg: Index check ===
            if (leg.getLegType() != null && leg.getLegType().equalsIgnoreCase("Floating")) {
                if (leg.getIndexId() == null && (leg.getIndexName() == null || leg.getIndexName().isBlank())) {
                    validationResult.addError(errorPrefix + ".index", "Floating legs must have an index specified", "ERROR");
                }
            }

            // === Notional check ===
            if (leg.getNotional() == null || leg.getNotional().compareTo(BigDecimal.ZERO) <= 0) {
                validationResult.addError(errorPrefix + ".notional", "Leg must have a positive notional", "ERROR");
            }

            legIndex++;
        }
        return validationResult;
    }

    public static ValidationResult validateReferenceData(Trade trade) {

        ValidationResult validationResult = new ValidationResult();

        // === Book validation ===
        if (trade.getBook() == null) {
            validationResult.addError("book", "Book not found or not set", "ERROR");
        } else {
            if (!trade.getBook().isActive()) {
                validationResult.addError("book", "Book must be active", "ERROR");
            }
            // Validate cost center, subdesk, and desk hierarchy
            CostCenter costCenter = trade.getBook().getCostCenter();
            if (costCenter == null) {
                validationResult.addError("costCenter", "Book has no associated cost center", "ERROR");
            } else {
                SubDesk subDesk = costCenter.getSubDesk();
                if (subDesk == null) {
                    validationResult.addError("subDesk", "Cost center has no associated subdesk", "ERROR");
                } else {
                    Desk desk = subDesk.getDesk();
                    if (desk == null) {
                        validationResult.addError("desk", "Subdesk has no associated desk", "ERROR");
                    }
                }
            }
        }
        // === Counterparty validation ===
        if (trade.getCounterparty() == null) {
            validationResult.addError("counterparty", "Counterparty not found or not set", "ERROR");
        } else {
            if (!trade.getCounterparty().isActive()) {
                validationResult.addError("counterparty", "Counterparty must be active", "ERROR");
            }
        }
        // === Trader user validation ===
        if (trade.getTraderUser() == null) {
            validationResult.addError("traderUser", "Trader User not found or not set", "ERROR");
        } else {
            if (!trade.getTraderUser().isActive()) {
                validationResult.addError("traderUser", "Trader User must be active", "ERROR");
            }
        }
        // === Trade Status validation ===
        if (trade.getTradeStatus() == null) {
            validationResult.addError("tradeStatus", "Trade status not found or not set", "ERROR");
        }
        // === Trade Type validation ===
        if (trade.getTradeType() == null) {
            validationResult.addError("tradeType", "Trade type not found or not set", "ERROR");
        }
        // === Trade SubType validation ===
        if (trade.getTradeSubType() == null) {
            validationResult.addError("tradeSubType", "Trade sub-type not found or not set", "ERROR");
        }
        return validationResult;
    }
}
