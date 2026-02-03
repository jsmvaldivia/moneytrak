package dev.juanvaldivia.moneytrak.transactions;

import dev.juanvaldivia.moneytrak.categories.Category;
import dev.juanvaldivia.moneytrak.categories.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TransactionController covering User Stories 2, 3, and 4.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // ========================================================================
    // US2: Link Transactions to Categories (T029-T035)
    // ========================================================================

    // T029: Creating transaction with category assignment
    @Test
    void createTransaction_withCategoryId_shouldLinkToCategory() throws Exception {
        Category foodCategory = categoryRepository.findByNameIgnoreCase("Food & Drinks").orElseThrow();

        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Lunch at restaurant",
                        "amount": 23.70,
                        "currency": "EUR",
                        "date": "2026-01-15T12:00:00Z",
                        "transactionType": "EXPENSE",
                        "transactionStability": "VARIABLE",
                        "categoryId": "%s"
                    }
                    """.formatted(foodCategory.getId())))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.categoryId").value(foodCategory.getId().toString()))
            .andExpect(jsonPath("$.categoryName").value("Food & Drinks"))
            .andExpect(jsonPath("$.amount").value(23.70));
    }

    // T030: Updating transaction category
    @Test
    void updateTransaction_withNewCategoryId_shouldUpdateCategoryLink() throws Exception {
        Category tollsCategory = categoryRepository.findByNameIgnoreCase("Tolls").orElseThrow();
        Category gasCategory = categoryRepository.findByNameIgnoreCase("Gas").orElseThrow();

        // Create transaction with Tolls category
        String response = mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Highway toll",
                        "amount": 5.50,
                        "currency": "EUR",
                        "date": "2026-01-10T08:00:00Z",
                        "transactionType": "EXPENSE",
                        "transactionStability": "VARIABLE",
                        "categoryId": "%s"
                    }
                    """.formatted(tollsCategory.getId())))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String txId = extractId(response);

        // Update to Gas category
        mockMvc.perform(put("/v1/transactions/{id}", txId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "categoryId": "%s",
                        "version": 0
                    }
                    """.formatted(gasCategory.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.categoryId").value(gasCategory.getId().toString()))
            .andExpect(jsonPath("$.categoryName").value("Gas"));
    }

    // T031: Retrieving transactions with category information
    @Test
    void getTransactions_shouldReturnCategoryInfo() throws Exception {
        Category bankCategory = categoryRepository.findByNameIgnoreCase("Bank").orElseThrow();

        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Wire transfer fee",
                        "amount": 3.00,
                        "currency": "EUR",
                        "date": "2026-01-05T09:00:00Z",
                        "transactionType": "EXPENSE",
                        "categoryId": "%s"
                    }
                    """.formatted(bankCategory.getId())))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/transactions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].categoryId").exists())
            .andExpect(jsonPath("$[0].categoryName").exists());
    }

    // T032: Filtering transactions by category (200 OK with results)
    @Test
    void filterByCategory_withMatchingTransactions_shouldReturn200WithResults() throws Exception {
        Category supermarket = categoryRepository.findByNameIgnoreCase("Supermarket").orElseThrow();

        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Weekly groceries",
                        "amount": 65.40,
                        "currency": "EUR",
                        "date": "2026-01-18T10:00:00Z",
                        "transactionType": "EXPENSE",
                        "categoryId": "%s"
                    }
                    """.formatted(supermarket.getId())))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/transactions?categoryId=" + supermarket.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].categoryName").value("Supermarket"));
    }

    // T033: Filtering by category with no transactions (200 OK empty array)
    @Test
    void filterByCategory_withNoTransactions_shouldReturn200EmptyArray() throws Exception {
        Category sport = categoryRepository.findByNameIgnoreCase("Sport").orElseThrow();

        mockMvc.perform(get("/v1/transactions?categoryId=" + sport.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    // T034: Invalid category ID reference (404 Not Found)
    @Test
    void filterByCategory_withNonExistentId_shouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/v1/transactions?categoryId=" + nonExistentId))
            .andExpect(status().isNotFound());
    }

    // T035: Missing category defaults to "Others"
    @Test
    void createTransaction_withoutCategoryId_shouldDefaultToOthers() throws Exception {
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Uncategorized purchase",
                        "amount": 10.00,
                        "currency": "EUR",
                        "date": "2026-01-20T14:00:00Z",
                        "transactionType": "EXPENSE"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.categoryName").value("Others"));
    }

    // ========================================================================
    // US3: Classify Transaction Types (T047-T052)
    // ========================================================================

    // T047: Creating EXPENSE transaction with positive amount
    @Test
    void createTransaction_asExpense_shouldStoreWithPositiveAmount() throws Exception {
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Office rent",
                        "amount": 30.75,
                        "currency": "EUR",
                        "date": "2026-01-12T00:00:00Z",
                        "transactionType": "EXPENSE"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.transactionType").value("EXPENSE"))
            .andExpect(jsonPath("$.amount").value(30.75));
    }

    // T048: Creating INCOME transaction with positive amount
    @Test
    void createTransaction_asIncome_shouldStoreWithPositiveAmount() throws Exception {
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Freelance payment",
                        "amount": 500.00,
                        "currency": "EUR",
                        "date": "2026-01-15T00:00:00Z",
                        "transactionType": "INCOME"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.transactionType").value("INCOME"))
            .andExpect(jsonPath("$.amount").value(500.00));
    }

    // T049: Rejecting negative amount (validation error)
    @Test
    void createTransaction_withNegativeAmount_shouldReturn400() throws Exception {
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Invalid expense",
                        "amount": -30.75,
                        "currency": "EUR",
                        "date": "2026-01-12T00:00:00Z",
                        "transactionType": "EXPENSE"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    // T050: Calculating expense summary (EXPENSE only)
    @Test
    void expenseSummary_shouldCalculateOnlyExpenses() throws Exception {
        // Create an EXPENSE
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Groceries",
                        "amount": 50.00,
                        "currency": "EUR",
                        "date": "2026-01-10T00:00:00Z",
                        "transactionType": "EXPENSE"
                    }
                    """))
            .andExpect(status().isCreated());

        // Create an INCOME (should not be in expense total)
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Salary",
                        "amount": 3000.00,
                        "currency": "EUR",
                        "date": "2026-01-01T00:00:00Z",
                        "transactionType": "INCOME"
                    }
                    """))
            .andExpect(status().isCreated());

        // Expense total should only include EXPENSE transactions
        mockMvc.perform(get("/v1/transactions/summary/expenses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(50.00));
    }

    // T051: Calculating income summary (INCOME only)
    @Test
    void incomeSummary_shouldCalculateOnlyIncome() throws Exception {
        // Create an INCOME
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Bonus",
                        "amount": 200.00,
                        "currency": "EUR",
                        "date": "2026-01-05T00:00:00Z",
                        "transactionType": "INCOME"
                    }
                    """))
            .andExpect(status().isCreated());

        // Create an EXPENSE (should not be in income total)
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Coffee",
                        "amount": 4.50,
                        "currency": "EUR",
                        "date": "2026-01-05T10:00:00Z",
                        "transactionType": "EXPENSE"
                    }
                    """))
            .andExpect(status().isCreated());

        // Income total should only include INCOME transactions
        mockMvc.perform(get("/v1/transactions/summary/income"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(200.00));
    }

    // T052: Updating transaction type from EXPENSE to INCOME
    @Test
    void updateTransaction_changeTypeToIncome_shouldPreserveAmount() throws Exception {
        String response = mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Refund received",
                        "amount": 25.00,
                        "currency": "EUR",
                        "date": "2026-01-18T00:00:00Z",
                        "transactionType": "EXPENSE"
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String txId = extractId(response);

        mockMvc.perform(put("/v1/transactions/{id}", txId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "transactionType": "INCOME",
                        "version": 0
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactionType").value("INCOME"))
            .andExpect(jsonPath("$.amount").value(25.00));
    }

    // ========================================================================
    // US4: Classify Transaction Stability (T062-T067)
    // ========================================================================

    // T062: Creating EXPENSE transaction with FIXED stability
    @Test
    void createTransaction_expenseFixed_shouldStoreStability() throws Exception {
        Category subscriptions = categoryRepository.findByNameIgnoreCase("Subscriptions").orElseThrow();

        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Netflix subscription",
                        "amount": 12.99,
                        "currency": "EUR",
                        "date": "2026-01-01T00:00:00Z",
                        "transactionType": "EXPENSE",
                        "transactionStability": "FIXED",
                        "categoryId": "%s"
                    }
                    """.formatted(subscriptions.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.transactionType").value("EXPENSE"))
            .andExpect(jsonPath("$.transactionStability").value("FIXED"));
    }

    // T063: Creating EXPENSE transaction with VARIABLE stability
    @Test
    void createTransaction_expenseVariable_shouldStoreStability() throws Exception {
        Category gas = categoryRepository.findByNameIgnoreCase("Gas").orElseThrow();

        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Gas refill",
                        "amount": 55.00,
                        "currency": "EUR",
                        "date": "2026-01-14T09:00:00Z",
                        "transactionType": "EXPENSE",
                        "transactionStability": "VARIABLE",
                        "categoryId": "%s"
                    }
                    """.formatted(gas.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.transactionType").value("EXPENSE"))
            .andExpect(jsonPath("$.transactionStability").value("VARIABLE"));
    }

    // T064: Creating INCOME transaction with FIXED stability
    @Test
    void createTransaction_incomeFixed_shouldStoreStability() throws Exception {
        Category transfers = categoryRepository.findByNameIgnoreCase("Transfers").orElseThrow();

        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Monthly salary",
                        "amount": 3000.00,
                        "currency": "EUR",
                        "date": "2026-01-01T00:00:00Z",
                        "transactionType": "INCOME",
                        "transactionStability": "FIXED",
                        "categoryId": "%s"
                    }
                    """.formatted(transfers.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.transactionType").value("INCOME"))
            .andExpect(jsonPath("$.transactionStability").value("FIXED"));
    }

    // T065: Default stability being VARIABLE when not specified
    @Test
    void createTransaction_withoutStability_shouldDefaultToVariable() throws Exception {
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "One-time purchase",
                        "amount": 99.99,
                        "currency": "EUR",
                        "date": "2026-01-20T16:00:00Z",
                        "transactionType": "EXPENSE"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.transactionStability").value("VARIABLE"));
    }

    // T066: Filtering transactions by FIXED stability
    @Test
    void filterByStability_fixed_shouldReturnOnlyFixedTransactions() throws Exception {
        // Create FIXED transaction
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Rent payment",
                        "amount": 800.00,
                        "currency": "EUR",
                        "date": "2026-01-01T00:00:00Z",
                        "transactionType": "EXPENSE",
                        "transactionStability": "FIXED"
                    }
                    """))
            .andExpect(status().isCreated());

        // Create VARIABLE transaction
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Random purchase",
                        "amount": 15.00,
                        "currency": "EUR",
                        "date": "2026-01-15T10:00:00Z",
                        "transactionType": "EXPENSE",
                        "transactionStability": "VARIABLE"
                    }
                    """))
            .andExpect(status().isCreated());

        // Filter by FIXED
        mockMvc.perform(get("/v1/transactions?stability=FIXED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].transactionStability").value("FIXED"))
            .andExpect(jsonPath("$[0].description").value("Rent payment"));
    }

    // T067: Updating transaction stability from FIXED to VARIABLE
    @Test
    void updateTransaction_changeStability_shouldUpdate() throws Exception {
        String response = mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Insurance",
                        "amount": 120.00,
                        "currency": "EUR",
                        "date": "2026-01-01T00:00:00Z",
                        "transactionType": "EXPENSE",
                        "transactionStability": "FIXED"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.transactionStability").value("FIXED"))
            .andReturn().getResponse().getContentAsString();

        String txId = extractId(response);

        mockMvc.perform(put("/v1/transactions/{id}", txId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "transactionStability": "VARIABLE",
                        "version": 0
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactionStability").value("VARIABLE"));
    }

    // ========================================================================
    // US5: Old endpoints return 404 (T081)
    // ========================================================================

    // T081: Old /v1/expenses endpoints return 404
    @Test
    void oldExpensesEndpoint_shouldReturn404() throws Exception {
        mockMvc.perform(get("/v1/expenses"))
            .andExpect(status().isNotFound());
    }

    // ========================================================================
    // Helper
    // ========================================================================

    private String extractId(String jsonResponse) {
        int idStart = jsonResponse.indexOf("\"id\":\"") + 6;
        int idEnd = jsonResponse.indexOf("\"", idStart);
        return jsonResponse.substring(idStart, idEnd);
    }
}
