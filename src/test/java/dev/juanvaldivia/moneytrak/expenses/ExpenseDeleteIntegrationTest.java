package dev.juanvaldivia.moneytrak.expenses;

import tools.jackson.databind.ObjectMapper;
import dev.juanvaldivia.moneytrak.expenses.dto.ExpenseCreationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExpenseDeleteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExpenseRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    private UUID createExpense(String description, BigDecimal amount, String currency, ZonedDateTime date) throws Exception {
        ExpenseCreationDto dto = new ExpenseCreationDto(description, amount, currency, date);

        MvcResult result = mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String idString = objectMapper.readTree(responseBody).get("id").asText();
        return UUID.fromString(idString);
    }

    @Test
    void shouldDeleteExpenseByIdAndReturnNoContent() throws Exception {
        UUID expenseId = createExpense("Coffee", new BigDecimal("4.50"), "USD", ZonedDateTime.now());

        mockMvc.perform(delete("/v1/expenses/{id}", expenseId))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$").doesNotExist());  // No body
    }

    @Test
    void shouldNotBeAbleToRetrieveDeletedExpense() throws Exception {
        UUID expenseId = createExpense("Coffee", new BigDecimal("4.50"), "USD", ZonedDateTime.now());

        // Delete the expense
        mockMvc.perform(delete("/v1/expenses/{id}", expenseId))
            .andExpect(status().isNoContent());

        // Attempt to retrieve it
        mockMvc.perform(get("/v1/expenses/{id}", expenseId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("NotFound"))
            .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    void shouldNotIncludeDeletedExpenseInList() throws Exception {
        UUID expense1 = createExpense("Expense 1", new BigDecimal("10.00"), "USD", ZonedDateTime.now());
        UUID expense2 = createExpense("Expense 2", new BigDecimal("20.00"), "USD", ZonedDateTime.now());
        UUID expense3 = createExpense("Expense 3", new BigDecimal("30.00"), "USD", ZonedDateTime.now());

        // Verify all three exist
        mockMvc.perform(get("/v1/expenses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3));

        // Delete expense2
        mockMvc.perform(delete("/v1/expenses/{id}", expense2))
            .andExpect(status().isNoContent());

        // Verify only two remain
        mockMvc.perform(get("/v1/expenses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[*].id").value(not(hasItem(expense2.toString()))));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentExpense() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete("/v1/expenses/{id}", nonExistentId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("NotFound"))
            .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    void shouldReturn404WhenDeletingAlreadyDeletedExpense() throws Exception {
        UUID expenseId = createExpense("Coffee", new BigDecimal("4.50"), "USD", ZonedDateTime.now());

        // First delete succeeds
        mockMvc.perform(delete("/v1/expenses/{id}", expenseId))
            .andExpect(status().isNoContent());

        // Second delete returns 404
        mockMvc.perform(delete("/v1/expenses/{id}", expenseId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("NotFound"));
    }

    @Test
    void shouldBeAbleToCreateNewExpenseWithSameDescriptionAfterDeletion() throws Exception {
        // Create and delete an expense
        UUID firstId = createExpense("Coffee", new BigDecimal("4.50"), "USD", ZonedDateTime.now());

        mockMvc.perform(delete("/v1/expenses/{id}", firstId))
            .andExpect(status().isNoContent());

        // Create a new expense with the same description
        UUID secondId = createExpense("Coffee", new BigDecimal("4.50"), "USD", ZonedDateTime.now());

        // Verify new expense exists and has different ID
        mockMvc.perform(get("/v1/expenses/{id}", secondId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(secondId.toString()))
            .andExpect(jsonPath("$.description").value("Coffee"));

        assert !firstId.equals(secondId) : "New expense should have different ID";
    }
}
