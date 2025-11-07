package com.technicalchallenge.controller;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.mapper.TradeMapper;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.service.TradeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/trades")
@Validated
@Tag(name = "Trades", description = "Trade management operations including booking, searching, and lifecycle management")
public class TradeController {
    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);

    @Autowired
    private TradeService tradeService;
    @Autowired
    private TradeMapper tradeMapper;

    @GetMapping("/rsql")
    @Operation(summary = "Get trades using RSQL query",
               description = "Retrieves a list of trades, using RSQL, in the system by criteria such as counterparty, book, trader, status, date ranges")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved trades",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Request not authorised")
    })
    public ResponseEntity<?> getTradesWithRSQL(
                @Parameter(description = "Id of user seeking to perform action", required = true)
                @RequestParam String userId,
                @Parameter(description = "RSQL Query", required = true)
                @RequestParam String query) {

        if (!tradeService.validateUserPrivileges(userId, "VIEW")) {
            return ResponseEntity.status(403).body("User " + userId + " is not authorized to VIEW trades.");
        }
        logger.info("Fetching specified trades: {}", query);

        try {
            List<TradeDTO> listOfTradeDTOs = tradeService.getTradesWithRSQL(query)
            .stream()
            .map(tradeMapper::toDto)
            .toList();
            return ResponseEntity.ok().body(listOfTradeDTOs);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid RSQL query: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid query: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching trades: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching trades: " + e.getMessage());
        }   
    }

    @GetMapping("/filter")
    @Operation(summary = "Paginate trades",
               description = "Retrieves a list of paginated trades")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated trades",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Request not authorised")
    })
    public ResponseEntity<?> paginateTrades(
            @Parameter(description = "Id of user seeking to perform action", required = true)
            @RequestParam String userId,
            @Parameter(description = "Requested page number. Default value is 0 (which is the first page)", required = false)
            @RequestParam(defaultValue = "0") int pageNum,
            @Parameter(description = "Requested number of trades on page. Default value is 3", required = false)
            @RequestParam(defaultValue = "3") int pageSize) {

        if (!tradeService.validateUserPrivileges(userId, "VIEW")) {
            return ResponseEntity.status(403).body("User " + userId + " is not authorized to VIEW trades.");
        }
        logger.info("Fetching paginated trades: Page Number - {}, Page Size - {}", pageNum, pageSize);
        try {
            Page<Trade> pageOfTrades = tradeService.paginateTrades(pageNum, pageSize);
            Page<TradeDTO> pageOfTradeDTOs = pageOfTrades.map(tradeMapper::toDto);
            return ResponseEntity.ok().body(pageOfTradeDTOs);
        } catch (Exception e) {
            logger.error("Pagination Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Pagination Error: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search trades",
               description = "Retrieves a list of trades in the system by criteria such as counterparty, book, trader, status, date ranges")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved trades",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error"),
        @ApiResponse(responseCode = "400", description = "Invalid search criteria"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Request not authorised")
    })
    public ResponseEntity<?> searchTrades(
            @Parameter(description = "Id of user seeking to perform action", required = true)
            @RequestParam String userId,
            @Parameter(description = "Earliest possible date of the trades being searched", required = false)
            @RequestParam (required = false) LocalDate earliestTradeDate,
            @Parameter(description = "Lastest possible date of the trades being searched", required = false) 
            @RequestParam (required = false) LocalDate latestTradeDate, 
            @Parameter(description = "Id of the trade status", required = false)
            @RequestParam (required = false) Long tradeStatusId, 
            @Parameter(description = "Id of the trader who's trades are being searched for", required = false)
            @RequestParam (required = false) Long traderId, 
            @Parameter(description = "Id of book", required = false)
            @RequestParam (required = false) Long bookId, 
            @Parameter(description = "Id of counterparty", required = false)
            @RequestParam (required = false) Long counterpartyId) {

        if (!tradeService.validateUserPrivileges(userId, "VIEW")) {
            return ResponseEntity.status(403).body("User " + userId + " is not authorized to VIEW trades.");
        }

        logger.info("Fetching trades with specified properties: {}, {}, {}, {}, {}, {}", 
            earliestTradeDate, latestTradeDate, tradeStatusId, traderId, bookId, counterpartyId);
        
        try {
            List<TradeDTO> listOfTradeDTOs = tradeService.searchTrades(earliestTradeDate, latestTradeDate, tradeStatusId, traderId, bookId, counterpartyId)
                    .stream()
                    .map(tradeMapper::toDto)
                    .toList();
            return ResponseEntity.ok().body(listOfTradeDTOs);
        } catch (Exception e) {
            logger.error("Error fetching trades: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching trades: " + e.getMessage());
        }        
    }

    @GetMapping
    @Operation(summary = "Get all trades",
               description = "Retrieves a list of all trades in the system. Returns comprehensive trade information including legs and cashflows.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all trades",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Request not authorised")
    })
    public ResponseEntity<?> getAllTrades(
            @Parameter(description = "Id of user seeking to perform action", required = true)
            @RequestParam String userId) {

        if (!tradeService.validateUserPrivileges(userId, "VIEW")) {
            return ResponseEntity.status(403).body("User " + userId + " is not authorized to VIEW trades.");
        }
        logger.info("Fetching all trades");
        List<TradeDTO> listOfTradeDTOs = tradeService.getAllTrades().stream()
                                    .map(tradeMapper::toDto)
                                    .toList();
        
        return ResponseEntity.ok().body(listOfTradeDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trade by ID",
               description = "Retrieves a specific trade by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade found and returned successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "404", description = "Trade not found"),
        @ApiResponse(responseCode = "400", description = "Invalid trade ID format"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Request not authorised")
    })
    public ResponseEntity<?> getTradeById(
            @Parameter(description = "Id of user seeking to perform action", required = true)
            @RequestParam String userId,
            @Parameter(description = "Unique identifier of the trade", required = true)
            @PathVariable(name = "id") Long id) {

        if (!tradeService.validateUserPrivileges(userId, "VIEW")) {
            return ResponseEntity.status(403).body("User " + userId + " is not authorized to VIEW trades.");
        }
        logger.debug("Fetching trade by id: {}", id);
        return tradeService.getTradeById(id)
                .map(tradeMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new trade",
               description = "Creates a new trade with the provided details. Automatically generates cashflows and validates business rules.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Trade created successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid trade data or business rule violation"),
        @ApiResponse(responseCode = "500", description = "Internal server error during trade creation"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Request not authorised")
    })
    public ResponseEntity<?> createTrade(
            @Parameter(description = "Id of user seeking to perform action", required = true)
            @RequestParam String userId,
            @Parameter(description = "Trade details for creation", required = true)
            @Valid @RequestBody TradeDTO tradeDTO) {

        if (!tradeService.validateUserPrivileges(userId, "CREATE")) {
            return ResponseEntity.status(403).body("User " + userId + " is not authorized to CREATE trades.");
        }

        logger.info("Creating new trade: {}", tradeDTO);
        
        try {
            Trade trade = tradeMapper.toEntity(tradeDTO);
            tradeService.populateReferenceDataByName(trade, tradeDTO);
            Trade savedTrade = tradeService.saveTrade(trade, tradeDTO);
            TradeDTO responseDTO = tradeMapper.toDto(savedTrade);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (Exception e) {
            logger.error("Error creating trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating trade: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update existing trade",
               description = "Updates an existing trade with new information. Subject to business rule validation and user privileges.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade updated successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "404", description = "Trade not found"),
        @ApiResponse(responseCode = "400", description = "Invalid trade data or business rule violation"),
        @ApiResponse(responseCode = "403", description = "Insufficient privileges to update trade")
    })
    public ResponseEntity<?> updateTrade(
            @Parameter(description = "Id of user seeking to perform action", required = true)
            @RequestParam String userId,
            @Parameter(description = "Unique identifier of the trade to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated trade details", required = true)
            @Valid @RequestBody TradeDTO tradeDTO) {

        if (!tradeService.validateUserPrivileges(userId, "AMEND", tradeDTO)) {
            return ResponseEntity.status(403).body("User " + userId + " is not authorized to AMEND trades.");
        }
        logger.info("Updating trade with id: {}", id);
        try {
            if (!tradeDTO.getTradeId().equals(id)) {
                throw new RuntimeException("Trade ID in path must match Trade ID in request body");
            }
            tradeDTO.setTradeId(id); // Ensure the ID matches
            Trade amendedTrade = tradeService.amendTrade(id, tradeDTO);
            TradeDTO responseDTO = tradeMapper.toDto(amendedTrade);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error updating trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating trade: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete trade",
               description = "Deletes an existing trade. This is a soft delete that changes the trade status.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Trade deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Trade not found"),
        @ApiResponse(responseCode = "400", description = "Trade cannot be deleted in current status"),
        @ApiResponse(responseCode = "403", description = "Insufficient privileges to delete trade")
    })
    public ResponseEntity<?> deleteTrade(
            @Parameter(description = "Id of user seeking to perform action", required = true)
            @RequestParam String userId,
            @Parameter(description = "Unique identifier of the trade to delete", required = true)
            @PathVariable Long id) {

        if (!tradeService.validateUserPrivileges(userId, "CANCEL")) {
            return ResponseEntity.status(403).body("User " + userId + " is not authorized to CANCEL trades.");
        }
        logger.info("Deleting trade with id: {}", id);
        try {
            tradeService.deleteTrade(id);
            return ResponseEntity.noContent().header("Message","Trade cancelled successfully").build();
        } catch (Exception e) {
            logger.error("Error deleting trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error deleting trade: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/terminate")
    @Operation(summary = "Terminate trade",
               description = "Terminates an existing trade before its natural maturity date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade terminated successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "404", description = "Trade not found"),
        @ApiResponse(responseCode = "400", description = "Trade cannot be terminated in current status"),
        @ApiResponse(responseCode = "403", description = "Insufficient privileges to terminate trade")
    })
    public ResponseEntity<?> terminateTrade(
            @Parameter(description = "Id of user seeking to perform action", required = true)
            @RequestParam String userId,
            @Parameter(description = "Unique identifier of the trade to terminate", required = true)
            @PathVariable Long id) {

        if (!tradeService.validateUserPrivileges(userId, "TERMINATE")) {
            return ResponseEntity.status(403).body("User " + userId + " is not authorized to TERMINATE trades.");
        }
        logger.info("Terminating trade with id: {}", id);
        try {
            Trade terminatedTrade = tradeService.terminateTrade(id);
            TradeDTO responseDTO = tradeMapper.toDto(terminatedTrade);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error terminating trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error terminating trade: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel trade",
               description = "Cancels an existing trade by changing its status to cancelled")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade cancelled successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "404", description = "Trade not found"),
        @ApiResponse(responseCode = "400", description = "Trade cannot be cancelled in current status"),
        @ApiResponse(responseCode = "403", description = "Insufficient privileges to cancel trade")
    })
    public ResponseEntity<?> cancelTrade(
            @Parameter(description = "Id of user seeking to perform action", required = true)
            @RequestParam String userId,
            @Parameter(description = "Unique identifier of the trade to cancel", required = true)
            @PathVariable Long id) {

        if (!tradeService.validateUserPrivileges(userId, "CANCEL")) {
            return ResponseEntity.status(403).body("User " + userId + " is not authorized to CANCEL trades.");
        }
        logger.info("Cancelling trade with id: {}", id);
        try {
            Trade cancelledTrade = tradeService.cancelTrade(id);
            TradeDTO responseDTO = tradeMapper.toDto(cancelledTrade);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            logger.error("Error cancelling trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error cancelling trade: " + e.getMessage());
        }
    }
}
