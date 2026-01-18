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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExpenseControllerIntegrationTest {

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
    void shouldCreateExpenseWithValidData() throws Exception {
        ExpenseCreationDto dto = new ExpenseCreationDto(
            "Lunch at restaurant",
            new BigDecimal("25.50"),
            "USD",
            ZonedDateTime.now()
        );

        mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.description").value("Lunch at restaurant"))
            .andExpect(jsonPath("$.amount").value(25.50))
            .andExpect(jsonPath("$.currency").value("USD"))
            .andExpect(jsonPath("$.version").value(0))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldRejectExpenseWithNullDescription() throws Exception {
        ExpenseCreationDto dto = new ExpenseCreationDto(
            null,
            new BigDecimal("25.50"),
            "USD",
            ZonedDateTime.now()
        );

        mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("ValidationError"))
            .andExpect(jsonPath("$.message").value("Invalid expense data"))
            .andExpect(jsonPath("$.details").isArray())
            .andExpect(jsonPath("$.details[*].field").value(hasItem("description")));
    }

    @Test
    void shouldRejectExpenseWithBlankDescription() throws Exception {
        ExpenseCreationDto dto = new ExpenseCreationDto(
            "   ",
            new BigDecimal("25.50"),
            "USD",
            ZonedDateTime.now()
        );

        mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("ValidationError"));
    }

    @Test
    void shouldRejectExpenseWithNullAmount() throws Exception {
        ExpenseCreationDto dto = new ExpenseCreationDto(
            "Lunch",
            null,
            "USD",
            ZonedDateTime.now()
        );

        mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details[*].field").value(hasItem("amount")));
    }

    @Test
    void shouldRejectExpenseWithNegativeAmount() throws Exception {
        ExpenseCreationDto dto = new ExpenseCreationDto(
            "Lunch",
            new BigDecimal("-10.00"),
            "USD",
            ZonedDateTime.now()
        );

        mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details[*].field").value(hasItem("amount")));
    }

    @Test
    void shouldRejectExpenseWithInvalidCurrency() throws Exception {
        ExpenseCreationDto dto = new ExpenseCreationDto(
            "Lunch",
            new BigDecimal("25.50"),
            "INVALID",
            ZonedDateTime.now()
        );

        mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details[*].field").value(hasItem("currency")));
    }

    @Test
    void shouldRejectExpenseWithFutureDate() throws Exception {
        ExpenseCreationDto dto = new ExpenseCreationDto(
            "Lunch",
            new BigDecimal("25.50"),
            "USD",
            ZonedDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details[*].field").value(hasItem("date")));
    }

    @Test
    void shouldAcceptExpenseWithMaximumAmount() throws Exception {
        ExpenseCreationDto dto = new ExpenseCreationDto(
            "Large purchase",
            new BigDecimal("999999999.99"),
            "USD",
            ZonedDateTime.now()
        );

        mockMvc.perform(post("/v1/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(999999999.99));
    }

    @Test
    void shouldAcceptExpenseWithDifferentCurrencies() throws Exception {
        String[] currencies = {"USD", "EUR", "GBP", "JPY"};

        for (String currency : currencies) {
            ExpenseCreationDto dto = new ExpenseCreationDto(
                "Test expense",
                new BigDecimal("100.00"),
                currency,
                ZonedDateTime.now()
            );

            mockMvc.perform(post("/v1/expenses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value(currency));
        }
    }
}
