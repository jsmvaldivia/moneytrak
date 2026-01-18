package dev.juanvaldivia.moneytrak.expenses;

import tools.jackson.databind.ObjectMapper;
import dev.juanvaldivia.moneytrak.expenses.dto.ExpenseCreationDto;
import dev.juanvaldivia.moneytrak.expenses.dto.ExpenseUpdateDto;
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
class ExpenseUpdateIntegrationTest {

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
    void shouldUpdateExpenseDescription() throws Exception {
        UUID expenseId = createExpense("Coffe", new BigDecimal("4.50"), "USD", ZonedDateTime.now());

        ExpenseUpdateDto updateDto = new ExpenseUpdateDto(
            "Coffee",  // Fixed typo
            null,
            null,
            null,
            0  // version
        );

        mockMvc.perform(put("/v1/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expenseId.toString()))
            .andExpect(jsonPath("$.description").value("Coffee"))
            .andExpect(jsonPath("$.amount").value(4.50))
            .andExpect(jsonPath("$.version").value(1));  // Version incremented
    }

    @Test
    void shouldUpdateExpenseAmount() throws Exception {
        UUID expenseId = createExpense("Coffee", new BigDecimal("4.50"), "USD", ZonedDateTime.now());

        ExpenseUpdateDto updateDto = new ExpenseUpdateDto(
            null,
            new BigDecimal("5.00"),  // Corrected amount
            null,
            null,
            0
        );

        mockMvc.perform(put("/v1/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Coffee"))  // Unchanged
            .andExpect(jsonPath("$.amount").value(5.00))
            .andExpect(jsonPath("$.currency").value("USD"))  // Unchanged
            .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void shouldUpdateMultipleFieldsAtOnce() throws Exception {
        UUID expenseId = createExpense("Lunch", new BigDecimal("15.00"), "USD", ZonedDateTime.now());

        ExpenseUpdateDto updateDto = new ExpenseUpdateDto(
            "Dinner",
            new BigDecimal("25.00"),
            "EUR",
            ZonedDateTime.now().minusDays(1),
            0
        );

        mockMvc.perform(put("/v1/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Dinner"))
            .andExpect(jsonPath("$.amount").value(25.00))
            .andExpect(jsonPath("$.currency").value("EUR"))
            .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void shouldRejectUpdateWithInvalidData() throws Exception {
        UUID expenseId = createExpense("Coffee", new BigDecimal("4.50"), "USD", ZonedDateTime.now());

        ExpenseUpdateDto updateDto = new ExpenseUpdateDto(
            null,
            new BigDecimal("-10.00"),  // Negative amount
            null,
            null,
            0
        );

        mockMvc.perform(put("/v1/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("ValidationError"))
            .andExpect(jsonPath("$.details[*].field").value(hasItem("amount")));
    }

    @Test
    void shouldRejectUpdateWithInvalidCurrency() throws Exception {
        UUID expenseId = createExpense("Coffee", new BigDecimal("4.50"), "USD", ZonedDateTime.now());

        ExpenseUpdateDto updateDto = new ExpenseUpdateDto(
            null,
            null,
            "INVALID",
            null,
            0
        );

        mockMvc.perform(put("/v1/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details[*].field").value(hasItem("currency")));
    }

    @Test
    void shouldRejectUpdateWithFutureDate() throws Exception {
        UUID expenseId = createExpense("Coffee", new BigDecimal("4.50"), "USD", ZonedDateTime.now());

        ExpenseUpdateDto updateDto = new ExpenseUpdateDto(
            null,
            null,
            null,
            ZonedDateTime.now().plusDays(1),  // Future date
            0
        );

        mockMvc.perform(put("/v1/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details[*].field").value(hasItem("date")));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentExpense() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        ExpenseUpdateDto updateDto = new ExpenseUpdateDto(
            "Updated",
            new BigDecimal("10.00"),
            "USD",
            ZonedDateTime.now(),
            0
        );

        mockMvc.perform(put("/v1/expenses/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("NotFound"));
    }

    @Test
    void shouldRejectConcurrentUpdateWithStaleVersion() throws Exception {
        UUID expenseId = createExpense("Coffee", new BigDecimal("4.50"), "USD", ZonedDateTime.now());

        // First update succeeds (version 0 -> 1)
        ExpenseUpdateDto firstUpdate = new ExpenseUpdateDto(
            "Updated Coffee",
            null,
            null,
            null,
            0
        );

        mockMvc.perform(put("/v1/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUpdate)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").value(1));

        // Second update with stale version should fail
        ExpenseUpdateDto secondUpdate = new ExpenseUpdateDto(
            "Another update",
            null,
            null,
            null,
            0  // Stale version
        );

        mockMvc.perform(put("/v1/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondUpdate)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").value(containsString("Version mismatch")));
    }

    @Test
    void shouldSucceedConcurrentUpdateWithCorrectVersion() throws Exception {
        UUID expenseId = createExpense("Coffee", new BigDecimal("4.50"), "USD", ZonedDateTime.now());

        // First update (version 0 -> 1)
        ExpenseUpdateDto firstUpdate = new ExpenseUpdateDto(
            "Updated Coffee",
            null,
            null,
            null,
            0
        );

        mockMvc.perform(put("/v1/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUpdate)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").value(1));

        // Second update with correct version should succeed (version 1 -> 2)
        ExpenseUpdateDto secondUpdate = new ExpenseUpdateDto(
            "Another update",
            null,
            null,
            null,
            1  // Correct current version
        );

        mockMvc.perform(put("/v1/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondUpdate)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Another update"))
            .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void shouldUpdateTimestampButPreserveCreatedAt() throws Exception {
        // Create expense and capture createdAt
        UUID expenseId = createExpense("Coffee", new BigDecimal("4.50"), "USD", ZonedDateTime.now());

        MvcResult getResult = mockMvc.perform(get("/v1/expenses/{id}", expenseId))
            .andExpect(status().isOk())
            .andReturn();

        String originalResponse = getResult.getResponse().getContentAsString();
        String originalCreatedAt = objectMapper.readTree(originalResponse).get("createdAt").asText();
        String originalUpdatedAt = objectMapper.readTree(originalResponse).get("updatedAt").asText();

        // Small delay to ensure updatedAt changes
        Thread.sleep(10);

        // Update the expense
        ExpenseUpdateDto updateDto = new ExpenseUpdateDto(
            "Updated Coffee",
            null,
            null,
            null,
            0
        );

        MvcResult updateResult = mockMvc.perform(put("/v1/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andReturn();

        String updatedResponse = updateResult.getResponse().getContentAsString();
        String newCreatedAt = objectMapper.readTree(updatedResponse).get("createdAt").asText();
        String newUpdatedAt = objectMapper.readTree(updatedResponse).get("updatedAt").asText();

        // Verify createdAt unchanged, updatedAt changed
        assert originalCreatedAt.equals(newCreatedAt) : "createdAt should not change on update";
        assert !originalUpdatedAt.equals(newUpdatedAt) : "updatedAt should change on update";
    }
}
