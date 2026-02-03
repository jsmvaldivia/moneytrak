package dev.juanvaldivia.moneytrak.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests verifying old /v1/expenses endpoints have been removed
 * as part of the migration from Expense to Transaction model (User Story 5).
 *
 * These tests verify that the old ExpenseController endpoints return 404 Not Found
 * after the migration is complete.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExpenseEndpointRemovalTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void oldExpenseEndpoint_GET_shouldReturn404() throws Exception {
        mockMvc.perform(get("/v1/expenses"))
            .andExpect(status().isNotFound());
    }

    @Test
    void oldExpenseEndpoint_POST_shouldReturn404() throws Exception {
        String requestBody = """
            {
                "description": "Test expense",
                "amount": 100.00,
                "currency": "USD",
                "date": "2025-01-22T10:00:00Z"
            }
            """;

        mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound());
    }

    @Test
    void oldExpenseEndpoint_GET_byId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/v1/expenses/123e4567-e89b-12d3-a456-426614174000"))
            .andExpect(status().isNotFound());
    }

    @Test
    void oldExpenseEndpoint_PUT_shouldReturn404() throws Exception {
        String requestBody = """
            {
                "description": "Updated expense",
                "amount": 200.00,
                "version": 0
            }
            """;

        mockMvc.perform(put("/v1/expenses/123e4567-e89b-12d3-a456-426614174000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound());
    }

    @Test
    void oldExpenseEndpoint_DELETE_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/v1/expenses/123e4567-e89b-12d3-a456-426614174000"))
            .andExpect(status().isNotFound());
    }
}
