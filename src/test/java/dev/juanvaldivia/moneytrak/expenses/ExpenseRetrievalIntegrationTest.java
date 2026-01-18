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
class ExpenseRetrievalIntegrationTest {

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

    @Test
    void shouldReturnEmptyListWhenNoExpenses() throws Exception {
        mockMvc.perform(get("/v1/expenses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturnAllExpensesInReverseChronologicalOrder() throws Exception {
        // Create three expenses with different dates
        ZonedDateTime baseDate = ZonedDateTime.now().minusDays(5);

        ExpenseCreationDto oldest = new ExpenseCreationDto(
            "Oldest expense",
            new BigDecimal("10.00"),
            "USD",
            baseDate
        );

        ExpenseCreationDto middle = new ExpenseCreationDto(
            "Middle expense",
            new BigDecimal("20.00"),
            "USD",
            baseDate.plusDays(2)
        );

        ExpenseCreationDto newest = new ExpenseCreationDto(
            "Newest expense",
            new BigDecimal("30.00"),
            "USD",
            baseDate.plusDays(4)
        );

        // Create them in non-chronological order to verify sorting
        mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(middle)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oldest)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newest)))
            .andExpect(status().isCreated());

        // Verify they're returned in reverse chronological order (newest first)
        mockMvc.perform(get("/v1/expenses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].description").value("Newest expense"))
            .andExpect(jsonPath("$[0].amount").value(30.00))
            .andExpect(jsonPath("$[1].description").value("Middle expense"))
            .andExpect(jsonPath("$[1].amount").value(20.00))
            .andExpect(jsonPath("$[2].description").value("Oldest expense"))
            .andExpect(jsonPath("$[2].amount").value(10.00));
    }

    @Test
    void shouldReturnSpecificExpenseById() throws Exception {
        // Create an expense
        ExpenseCreationDto dto = new ExpenseCreationDto(
            "Test expense",
            new BigDecimal("42.50"),
            "EUR",
            ZonedDateTime.now()
        );

        MvcResult createResult = mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        String idString = objectMapper.readTree(responseBody).get("id").asText();
        UUID expenseId = UUID.fromString(idString);

        // Retrieve it by ID
        mockMvc.perform(get("/v1/expenses/{id}", expenseId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expenseId.toString()))
            .andExpect(jsonPath("$.description").value("Test expense"))
            .andExpect(jsonPath("$.amount").value(42.50))
            .andExpect(jsonPath("$.currency").value("EUR"))
            .andExpect(jsonPath("$.version").value(0))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturn404WhenExpenseNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/v1/expenses/{id}", nonExistentId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("NotFound"))
            .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    void shouldReturnAllFieldsInListResponse() throws Exception {
        ExpenseCreationDto dto = new ExpenseCreationDto(
            "Complete expense",
            new BigDecimal("99.99"),
            "GBP",
            ZonedDateTime.now()
        );

        mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/expenses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").exists())
            .andExpect(jsonPath("$[0].description").value("Complete expense"))
            .andExpect(jsonPath("$[0].amount").value(99.99))
            .andExpect(jsonPath("$[0].currency").value("GBP"))
            .andExpect(jsonPath("$[0].date").exists())
            .andExpect(jsonPath("$[0].version").value(0))
            .andExpect(jsonPath("$[0].createdAt").exists())
            .andExpect(jsonPath("$[0].updatedAt").exists());
    }

    @Test
    void shouldHandleMultipleExpensesWithSameDateOrderConsistently() throws Exception {
        ZonedDateTime sameDate = ZonedDateTime.now().minusDays(1);

        for (int i = 1; i <= 3; i++) {
            ExpenseCreationDto dto = new ExpenseCreationDto(
                "Expense " + i,
                new BigDecimal("10.00"),
                "USD",
                sameDate
            );

            mockMvc.perform(post("/v1/expenses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/v1/expenses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3));
    }
}
