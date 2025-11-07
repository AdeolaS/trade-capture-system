package com.technicalchallenge.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technicalchallenge.dto.DailySummaryDTO;
import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeSummaryDTO;
import com.technicalchallenge.mapper.TradeMapper;
import com.technicalchallenge.service.TradeDashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dashboard")
@Validated
@Tag(name = "Dashboard", description = "Trade dashboard management system.")
public class TradeDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(TradeDashboardController.class);

    @Autowired
    private TradeDashboardService tradeDashboardService;
    @Autowired
    private TradeMapper tradeMapper;

    @GetMapping("/my-trades")
    @Operation(summary = "Get personal trades",
               description = "Retrieves a list of Trader's personal trades")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved Trader's personal trades",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<TradeDTO>> getPersonalTrades(@RequestParam String userId) {
        logger.info("Fetching trades for user");

        List<TradeDTO> listOfTradeDTOs = tradeDashboardService.getPersonalTrades(userId)
            .stream()
            .map(tradeMapper::toDto)
            .toList();
        
        return ResponseEntity.ok().body(listOfTradeDTOs);
    }
    
    @GetMapping("/book/{id}/trades")
    @Operation(summary = "Get Book-level trade aggregation",
               description = "Retrieves a list of Trader's personal trades by book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved Trader's  trades",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<TradeDTO>> getTradesByBook(@PathVariable String id, @RequestParam String userId) {
        logger.info("Fetching trades by book for user");

        // List<TradeDTO> listOfTradeDTOs = tradeDashboardService.getPersonalTrades(userId)
        List<TradeDTO> listOfTradeDTOs = tradeDashboardService.getTradesByBook(id, userId)
            .stream()
            .map(tradeMapper::toDto)
            .toList();

        return ResponseEntity.ok(listOfTradeDTOs);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get trade portfolio summaries",
               description = "Retrieves a list trade portfolio information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved trade portfolio summaries",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TradeSummaryDTO> getTradeSummary(@RequestParam String userId) {
        return ResponseEntity.ok(tradeDashboardService.getTradeSummaryForUser(userId));
    }

    @GetMapping("/daily-summary")
    @Operation(summary = "Get daily trading statistics",
               description = "Retrieves a list of daily summary statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved daily summary statistics",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DailySummaryDTO> getDailySummary(@RequestParam String userId) {
        return ResponseEntity.ok(tradeDashboardService.getDailySummaryForUser(userId));
    }
}
