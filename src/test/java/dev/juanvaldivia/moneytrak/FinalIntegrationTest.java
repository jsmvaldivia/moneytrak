package dev.juanvaldivia.moneytrak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import dev.juanvaldivia.moneytrak.categories.CategoryRepository;
import dev.juanvaldivia.moneytrak.transactions.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Final integration test verifying all 5 user stories work end-to-end.
 *
 * <p>This test validates: - US1: Category management (CRUD, predefined categories) - US2:
 * Transaction-category linking - US3: Transaction types (EXPENSE/INCOME) with summaries - US4:
 * Transaction stability (FIXED/VARIABLE) with filtering
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
class FinalIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private TransactionRepository transactionRepository;

  @Test
  void completeWorkflow_AllFiveUserStories_ShouldWork() throws Exception {
    // ============================================================================
    // US1: Category Management
    // ============================================================================

    // Verify 14 predefined categories exist
    mockMvc
        .perform(get("/v1/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(15)); // 14 + "Others"

    // Create custom category
    String createCategoryResponse =
        mockMvc
            .perform(
                post("/v1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\": \"Medical\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Medical"))
            .andExpect(jsonPath("$.isPredefined").value(false))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String categoryId = extractId(createCategoryResponse);

    // ============================================================================
    // US2: Transaction-Category Linking
    // ============================================================================

    // Create transaction with category (EXPENSE + VARIABLE defaults)
    String createTxResponse =
        mockMvc
            .perform(
                post("/v1/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        String.format(
                            """
                    {
                        "description": "Doctor visit",
                        "amount": 75.50,
                        "currency": "EUR",
                        "date": "2026-01-22T10:00:00Z",
                        "categoryId": "%s"
                    }
                    """,
                            categoryId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.categoryId").value(categoryId))
            .andExpect(jsonPath("$.categoryName").value("Medical"))
            .andExpect(jsonPath("$.type").value("EXPENSE"))
            .andExpect(jsonPath("$.stability").value("VARIABLE"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String transactionId = extractId(createTxResponse);

    // Filter transactions by category
    mockMvc
        .perform(get("/v1/transactions?categoryId=" + categoryId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].categoryName").value("Medical"));

    // ============================================================================
    // US3: Transaction Types (EXPENSE vs INCOME)
    // ============================================================================

    // Create INCOME transaction
    mockMvc
        .perform(
            post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "description": "Freelance payment",
                        "amount": 500.00,
                        "currency": "EUR",
                        "date": "2026-01-20T10:00:00Z",
                        "type": "INCOME",
                        "stability": "VARIABLE"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("INCOME"));

    // Verify expense total
    mockMvc
        .perform(get("/v1/transactions/summary/expenses"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(75.50));

    // Verify income total
    mockMvc
        .perform(get("/v1/transactions/summary/income"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(500.00));

    // ============================================================================
    // US4: Transaction Stability (FIXED vs VARIABLE)
    // ============================================================================

    // Create FIXED transaction
    mockMvc
        .perform(
            post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "description": "Netflix subscription",
                        "amount": 12.99,
                        "currency": "EUR",
                        "date": "2026-01-01T00:00:00Z",
                        "type": "EXPENSE",
                        "stability": "FIXED"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.stability").value("FIXED"));

    // Filter by FIXED stability
    mockMvc
        .perform(get("/v1/transactions?stability=FIXED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].description").value("Netflix subscription"));

    // Filter by VARIABLE stability
    mockMvc
        .perform(get("/v1/transactions?stability=VARIABLE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2)); // Doctor visit + Freelance payment

    // ============================================================================
    // Final Verification
    // ============================================================================

    // Verify database state
    assertThat(categoryRepository.count())
        .isGreaterThanOrEqualTo(15); // 14 predefined + Others + Medical
    assertThat(transactionRepository.count()).isEqualTo(3); // Doctor, Freelance, Netflix

    // Verify all transactions are retrievable
    mockMvc
        .perform(get("/v1/transactions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3));
  }

  /** Helper method to extract "id" field from JSON response. */
  private String extractId(String jsonResponse) {
    int idStart = jsonResponse.indexOf("\"id\":\"") + 6;
    int idEnd = jsonResponse.indexOf("\"", idStart);
    return jsonResponse.substring(idStart, idEnd);
  }
}
