package com.technicalchallenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.mapper.TradeMapper;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TradeController.class)
public class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeService tradeService;

    @MockBean
    private TradeMapper tradeMapper;

    private ObjectMapper objectMapper;
    private TradeDTO tradeDTO;
    private Trade trade;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = "1000";
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create a sample TradeDTO for testing
        tradeDTO = new TradeDTO();
        tradeDTO.setTradeId(1001L);
        tradeDTO.setVersion(1);
        tradeDTO.setTradeDate(LocalDate.now()); // Fixed: LocalDate instead of LocalDateTime
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(2)); // Fixed: correct method name
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(5)); // Fixed: correct method name
        tradeDTO.setTradeStatus("LIVE");
        tradeDTO.setBookName("TestBook");
        tradeDTO.setCounterpartyName("TestCounterparty");
        tradeDTO.setTraderUserName("TestTrader");
        tradeDTO.setInputterUserName("TestInputter");
        tradeDTO.setUtiCode("UTI123456789");

        // Create a sample Trade entity for testing
        trade = new Trade();
        trade.setId(1L);
        trade.setTradeId(1001L);
        trade.setVersion(1);
        trade.setTradeDate(LocalDate.now()); // Fixed: LocalDate instead of LocalDateTime
        trade.setTradeStartDate(LocalDate.now().plusDays(2)); // Fixed: correct method name
        trade.setTradeMaturityDate(LocalDate.now().plusYears(5)); // Fixed: correct method name

        // Set up default mappings
        when(tradeMapper.toDto(any(Trade.class))).thenReturn(tradeDTO);
        when(tradeMapper.toEntity(any(TradeDTO.class))).thenReturn(trade);
    }

    @Test
    void testGetAllTrades_Success() throws Exception {
        // Given
        List<Trade> trades = List.of(trade); // Fixed: use List.of instead of Arrays.asList for single item

        when(tradeService.getAllTrades()).thenReturn(trades);
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(true);

        // When/Then
        mockMvc.perform(get("/api/trades")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tradeId", is(1001)))
                .andExpect(jsonPath("$[0].bookName", is("TestBook")))
                .andExpect(jsonPath("$[0].counterpartyName", is("TestCounterparty")));

        verify(tradeService).getAllTrades();
    }

    @Test
    void testGetAllTrades_ForbiddenUserId() throws Exception {
        String forbiddenId = "403";
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(false);
        
        mockMvc.perform(get("/api/trades")
                        .param("userId", String.valueOf(forbiddenId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User 403 is not authorized to VIEW trades."));

        verify(tradeService, times(0)).getAllTrades();
    }

    @Test
    void testSearchTrades_Success() throws Exception {
        // Given
        List<Trade> trades = List.of(trade);

        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(true);
        when(tradeService.searchTrades(
                any(), any(), any(), any(), any(), any()))
                .thenReturn(trades);

        // When/Then
        mockMvc.perform(get("/api/trades/search")
                        .param("userId", userId)
                        .param("earliestTradeDate", LocalDate.now().minusDays(1).toString())
                        .param("latestTradeDate", LocalDate.now().plusDays(1).toString())
                        .param("tradeStatusId", "1")
                        .param("traderId", "2")
                        .param("bookId", "3")
                        .param("counterpartyId", "4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tradeId", is(1001)))
                .andExpect(jsonPath("$[0].bookName", is("TestBook")))
                .andExpect(jsonPath("$[0].counterpartyName", is("TestCounterparty")));

        verify(tradeService).validateUserPrivileges(eq(userId), eq("VIEW"));
        verify(tradeService).searchTrades(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testSearchTrades_Forbidden() throws Exception {
        // Given
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(false);

        // When/Then
        mockMvc.perform(get("/api/trades/search")
                        .param("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User " + userId + " is not authorized to VIEW trades."));

        verify(tradeService).validateUserPrivileges(eq(userId), eq("VIEW"));
    }

    @Test
    void testSearchTrades_BadRequest() throws Exception {
        // Given
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(true);
        when(tradeService.searchTrades(
                any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Bad Request"));

        // When/Then
        mockMvc.perform(get("/api/trades/search")
                        .param("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error fetching trades: Bad Request"));

        verify(tradeService).validateUserPrivileges(eq(userId), eq("VIEW"));
        verify(tradeService).searchTrades(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testGetTradesWithRSQL_Success() throws Exception {

        // Given
        String query = "book.name==Testbook";
        when(tradeService.getTradesWithRSQL(query)).thenReturn(List.of(trade));
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(true);

        // When/Then
        mockMvc.perform(get("/api/trades/rsql")
                        .param("query", query)
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tradeId", is(1001)))
                .andExpect(jsonPath("$[0].bookName", is("TestBook")));

        verify(tradeService).getTradesWithRSQL(query);
    }

    @Test
    void testGetTradesWithRSQL_ForbiddenUserId() throws Exception {
        String forbiddenId = "403";
        String query = "book.name==Testbook";
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(false);
        
        mockMvc.perform(get("/api/trades/rsql")
                        .param("query", query)
                        .param("userId", String.valueOf(forbiddenId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User 403 is not authorized to VIEW trades."));

        verify(tradeService, times(0)).getTradesWithRSQL(query);
    }

    @Test
    void testGetTradesWithRSQL_NoValue() throws Exception {
        String query = "counterparty.name=";
        
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(true);
        when(tradeService.getTradesWithRSQL(query))
                .thenThrow(new IllegalArgumentException("cz.jirutka.rsql.parser.TokenMgrError: Lexical error at line 1, column 19.  Encountered: <EOF> after : \"\""));

        mockMvc.perform(get("/api/trades/rsql")
                        .param("query", query)
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid query: cz.jirutka.rsql.parser.TokenMgrError: Lexical error at line 1, column 19.  Encountered: <EOF> after : \"\""));

        verify(tradeService).getTradesWithRSQL(query);
    }

    @Test
    void testGetTradesWithRSQL_UnknownField() throws Exception {
        String query = "counterparty.unknownField==X";
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(true);
        when(tradeService.getTradesWithRSQL(query))
                .thenThrow(new RuntimeException("Error building predicate for property: counterparty.unknownField — org.hibernate.query.SemanticException: Could not resolve attribute 'unknownField' of 'com.technicalchallenge.model.Counterparty'"));

        mockMvc.perform(get("/api/trades/rsql")
                        .param("userId", String.valueOf(userId))
                        .param("query", query)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error fetching trades: Error building predicate for property: counterparty.unknownField — org.hibernate.query.SemanticException: Could not resolve attribute 'unknownField' of 'com.technicalchallenge.model.Counterparty'"));

        verify(tradeService).getTradesWithRSQL(query);
    }

    @Test
    void testGetTradesWithRSQL_EmptyQuery() throws Exception {
        String query = "";
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(true);
        when(tradeService.getTradesWithRSQL(query))
                .thenThrow(new IllegalArgumentException("cz.jirutka.rsql.parser.ParseException: Encountered \"<EOF>\" at line 0, column 0.\r\n" + //
                                        "Was expecting one of:\r\n" + //
                                        "    <UNRESERVED_STR> ...\r\n" + //
                                        "    \"(\" ..."));

        mockMvc.perform(get("/api/trades/rsql")
                        .param("query", query)
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid query: cz.jirutka.rsql.parser.ParseException: Encountered \"<EOF>\" at line 0, column 0.\r\n" + //
                                        "Was expecting one of:\r\n" + //
                                        "    <UNRESERVED_STR> ...\r\n" + //
                                        "    \"(\" ..."));

        verify(tradeService).getTradesWithRSQL(query);
    }

    @Test
    void testGetTradesWithRSQL_TooManyEquals() throws Exception {
        String query = "tradeDate===2025-01-01";
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(true);
        when(tradeService.getTradesWithRSQL(query))
                .thenThrow(new IllegalArgumentException("cz.jirutka.rsql.parser.TokenMgrError: Lexical error at line 1, column 13.  Encountered: \"2\" (50), after : \"=\""));

        mockMvc.perform(get("/api/trades/rsql")
                        .param("query", query)
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid query: cz.jirutka.rsql.parser.TokenMgrError: Lexical error at line 1, column 13.  Encountered: \"2\" (50), after : \"=\""));

        verify(tradeService).getTradesWithRSQL(query);
    }

    @Test
    void testGetTradesWithRSQL_UnexpectedError() throws Exception {
        String query = "book.name==TestBook";
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(true);
        when(tradeService.getTradesWithRSQL(query))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/trades/rsql")
                        .param("userId", String.valueOf(userId))
                        .param("query", query)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error fetching trades: Database connection failed"));

        verify(tradeService).getTradesWithRSQL(query);
    }

    @Test
    void testPaginateTrades_Success() throws Exception {
        // Given
        int pageNum = 0;
        int pageSize = 3;

        List<Trade> trades = List.of(trade);
        Page<Trade> tradePage = new PageImpl<>(trades, PageRequest.of(pageNum, pageSize), trades.size());

        when(tradeService.paginateTrades(pageNum, pageSize)).thenReturn(tradePage);
        when(tradeMapper.toDto(any(Trade.class))).thenReturn(tradeDTO);
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(true);

        // When / Then
        mockMvc.perform(get("/api/trades/filter")
                        .param("pageNum", String.valueOf(pageNum))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Validate pagination info
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].tradeId", is(1001)))
                .andExpect(jsonPath("$.content[0].bookName", is("TestBook")))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andExpect(jsonPath("$.number", is(pageNum)));

        verify(tradeService).paginateTrades(pageNum, pageSize);
    }

    @Test
    void testPaginateTrades_ForbiddenUserId() throws Exception {
        // Given
        String forbiddenId = "403";
        int pageNum = -1;
        int pageSize = 3;

        when(tradeService.validateUserPrivileges(eq(forbiddenId), eq("VIEW"))).thenReturn(false);

        // When / Then
        mockMvc.perform(get("/api/trades/filter")
                        .param("pageNum", String.valueOf(pageNum))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("userId", String.valueOf(forbiddenId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User 403 is not authorized to VIEW trades."));

        verify(tradeService, times(0)).paginateTrades(pageNum, pageSize);
    }

    @Test
    void testPaginateTrades_NegativePageNumber() throws Exception {
        // Given
        int pageNum = -1;
        int pageSize = 3;

        // Simulate that the service throws an Exception
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(true);
        when(tradeService.paginateTrades(pageNum, pageSize))
                .thenThrow(new RuntimeException("\n Requested Page number must be non-negative"));

        // When / Then
        mockMvc.perform(get("/api/trades/filter")
                        .param("pageNum", String.valueOf(pageNum))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Pagination Error: \n Requested Page number must be non-negative"));

        verify(tradeService).paginateTrades(pageNum, pageSize);
    }

    @Test
    void testPaginateTrades_NegativePageSize() throws Exception {
        // Given
        int pageNum = 1;
        int pageSize = -3;

        // Simulate that the service throws an Exception
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(true);
        when(tradeService.paginateTrades(pageNum, pageSize))
                .thenThrow(new RuntimeException("\n Page size must be more than zero"));

        // When / Then
        mockMvc.perform(get("/api/trades/filter")
                        .param("pageNum", String.valueOf(pageNum))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Pagination Error: \n Page size must be more than zero"));

        verify(tradeService).paginateTrades(pageNum, pageSize);
    }

    @Test
    void testGetTradeById() throws Exception {
        // Given
        when(tradeService.getTradeById(1001L)).thenReturn(Optional.of(trade));
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(true);

        // When/Then
        mockMvc.perform(get("/api/trades/1001")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeId", is(1001)))
                .andExpect(jsonPath("$.bookName", is("TestBook")))
                .andExpect(jsonPath("$.counterpartyName", is("TestCounterparty")));

        verify(tradeService).getTradeById(1001L);
    }

    @Test
    void testGetTradeByIdNotFound() throws Exception {
        // Given
        when(tradeService.getTradeById(9999L)).thenReturn(Optional.empty());
        when(tradeService.validateUserPrivileges(eq(userId), eq("VIEW"))).thenReturn(true);

        // When/Then
        mockMvc.perform(get("/api/trades/9999")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(tradeService).getTradeById(9999L);
    }

    @Test
    void testCreateTrade() throws Exception {
        // Given
        when(tradeService.validateUserPrivileges(eq(userId), eq("CREATE"))).thenReturn(true);
        when(tradeService.saveTrade(any(Trade.class), any(TradeDTO.class))).thenReturn(trade);
        doNothing().when(tradeService).populateReferenceDataByName(any(Trade.class), any(TradeDTO.class));

        // When/Then
        mockMvc.perform(post("/api/trades")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tradeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tradeId", is(1001)));

        verify(tradeService).validateUserPrivileges(eq(userId), eq("CREATE"));
        verify(tradeService).saveTrade(any(Trade.class), any(TradeDTO.class));
        verify(tradeService).populateReferenceDataByName(any(Trade.class), any(TradeDTO.class));
    }

    @Test
    void testCreateTradeValidationFailure_MissingTradeDate() throws Exception {
        // Given
        TradeDTO invalidDTO = new TradeDTO();
        invalidDTO.setBookName("TestBook");
        invalidDTO.setCounterpartyName("TestCounterparty");
        // Trade date is purposely missing
        when(tradeService.validateUserPrivileges(eq(userId), eq("CREATE"))).thenReturn(true);

        doThrow(new RuntimeException("Trade date is required"))
            .when(tradeMapper).toEntity(any(TradeDTO.class));

        // When/Then
        mockMvc.perform(post("/api/trades")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error creating trade: Trade date is required"));

        verify(tradeService).validateUserPrivileges(eq(userId), eq("CREATE"));
        verify(tradeService, never()).saveTrade(any(Trade.class), any(TradeDTO.class));
    }

    @Test
    void testCreateTradeValidationFailure_MissingBook() throws Exception {
        // Given
        TradeDTO invalidDTO = new TradeDTO();
        invalidDTO.setTradeDate(LocalDate.now());
        invalidDTO.setCounterpartyName("TestCounterparty");
        when(tradeService.validateUserPrivileges(eq(userId), eq("CREATE"))).thenReturn(true);
        // Book name is purposely missing

        doThrow(new RuntimeException("Book and Counterparty are required"))
            .when(tradeService).populateReferenceDataByName(any(Trade.class),any(TradeDTO.class));


        // When/Then
        mockMvc.perform(post("/api/trades")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error creating trade: Book and Counterparty are required"));

        verify(tradeService).validateUserPrivileges(eq(userId), eq("CREATE"));
        verify(tradeService, never()).saveTrade(any(Trade.class), any(TradeDTO.class));
    }

    @Test
    void testUpdateTrade() throws Exception {
        // Given
        Long tradeId = 1001L;
        tradeDTO.setTradeId(tradeId);

        when(tradeService.amendTrade(eq(tradeId), any(TradeDTO.class))).thenReturn(trade);
        when(tradeMapper.toDto(any(Trade.class))).thenReturn(tradeDTO);
        
        when(tradeService.validateUserPrivileges(eq(userId), eq("AMEND"))).thenReturn(true);

        // When/Then
        mockMvc.perform(put("/api/trades/{id}", tradeId)
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tradeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeId", is(1001)));
        
        verify(tradeService).amendTrade(eq(tradeId), any(TradeDTO.class));
    }

    @Test
    void testUpdateTrade_ForbiddenId() throws Exception {
        // Given
        Long tradeId = 1001L;
        tradeDTO.setTradeId(tradeId);
        String forbiddenId = "403";

        when(tradeService.amendTrade(eq(tradeId), any(TradeDTO.class))).thenReturn(trade);
        when(tradeMapper.toDto(any(Trade.class))).thenReturn(tradeDTO);
        when(tradeService.validateUserPrivileges(eq(forbiddenId), eq("AMEND"))).thenReturn(false);

        // When/Then
        mockMvc.perform(put("/api/trades/{id}", tradeId)
                        .param("userId", String.valueOf(forbiddenId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tradeDTO)))
                .andExpect(status().isForbidden());
        
        verify(tradeService, times(0)).amendTrade(eq(tradeId), any(TradeDTO.class));
    }


    @Test
    void testUpdateTradeIdMismatch() throws Exception {
        // Given
        Long pathId = 1001L;
        tradeDTO.setTradeId(2002L); // Different from path ID
        when(tradeService.validateUserPrivileges(eq(userId), eq("AMEND"))).thenReturn(true);

        // When/Then
        mockMvc.perform(put("/api/trades/{id}", pathId)
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tradeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error updating trade: Trade ID in path must match Trade ID in request body"));

        verify(tradeService, never()).saveTrade(any(Trade.class), any(TradeDTO.class));
    }

    @Test
    void testDeleteTrade() throws Exception {
        // Given
        doNothing().when(tradeService).deleteTrade(1001L);
        when(tradeService.validateUserPrivileges(eq(userId), eq("CANCEL"))).thenReturn(true);

        // When/Then
        mockMvc.perform(delete("/api/trades/1001")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(tradeService).deleteTrade(1001L);
    }

    @Test
    void testDeleteTrade_ForbiddenUserId() throws Exception {
        // Given
        
        String forbiddenId = "403";
        doNothing().when(tradeService).deleteTrade(1001L);
        when(tradeService.validateUserPrivileges(eq(forbiddenId), eq("CANCEL"))).thenReturn(false);

        // When/Then
        mockMvc.perform(delete("/api/trades/1001")
                        .param("userId", String.valueOf(forbiddenId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User 403 is not authorized to CANCEL trades."));

        verify(tradeService, times(0)).deleteTrade(1001L);
    }

    @Test
    void testCreateTradeWithValidationErrors() throws Exception {
        // Given
        TradeDTO invalidDTO = new TradeDTO();
        invalidDTO.setTradeDate(LocalDate.now()); // Fixed: LocalDate instead of LocalDateTime
        // Missing required fields to trigger validation errors

        // When/Then
        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(tradeService, never()).createTrade(any(TradeDTO.class));
    }
}
