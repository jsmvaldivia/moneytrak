package dev.juanvaldivia.moneytrak.security;

import dev.juanvaldivia.moneytrak.categories.CategoryRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive security integration tests covering all 5 user stories.
 * Uses httpBasic() post-processor with real configured test users
 * from application-test.yaml (not @WithMockUser).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    private static final String VALID_TRANSACTION_JSON = """
            {
                "description": "Test transaction",
                "amount": 10.00,
                "currency": "EUR",
                "date": "2026-01-15T12:00:00Z",
                "type": "EXPENSE"
            }
            """;

    // ========================================================================
    // US1: Protect All Endpoints with Authentication (P1)
    // ========================================================================

    @Nested
    class US1_Authentication {

        @Test
        void unauthenticated_getTransactions_returns401() throws Exception {
            mockMvc.perform(get("/v1/transactions"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"));
        }

        @Test
        void invalidCredentials_getTransactions_returns401() throws Exception {
            mockMvc.perform(get("/v1/transactions")
                            .with(httpBasic("wrong", "wrong")))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void validCredentials_getTransactions_returns200() throws Exception {
            mockMvc.perform(get("/v1/transactions")
                            .with(httpBasic("admin", "admin")))
                    .andExpect(status().isOk());
        }

        @Test
        void malformedAuthHeader_getTransactions_returns401() throws Exception {
            mockMvc.perform(get("/v1/transactions")
                            .header("Authorization", "Basic not-valid-base64!!!"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void unauthenticated_postTransaction_returns401() throws Exception {
            mockMvc.perform(post("/v1/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_TRANSACTION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void unauthenticated_getCategories_returns401() throws Exception {
            mockMvc.perform(get("/v1/categories"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ========================================================================
    // US2: APP Role Read-Only Access (P2)
    // ========================================================================

    @Nested
    class US2_AppRole {

        @Test
        void appRole_getTransactions_returns200() throws Exception {
            mockMvc.perform(get("/v1/transactions")
                            .with(httpBasic("app-client", "app-client")))
                    .andExpect(status().isOk());
        }

        @Test
        void appRole_getCategories_returns200() throws Exception {
            mockMvc.perform(get("/v1/categories")
                            .with(httpBasic("app-client", "app-client")))
                    .andExpect(status().isOk());
        }

        @Test
        void appRole_getTransactionSummaryExpenses_returns200() throws Exception {
            mockMvc.perform(get("/v1/transactions/summary/expenses")
                            .with(httpBasic("app-client", "app-client")))
                    .andExpect(status().isOk());
        }

        @Test
        void appRole_getTransactionSummaryIncome_returns200() throws Exception {
            mockMvc.perform(get("/v1/transactions/summary/income")
                            .with(httpBasic("app-client", "app-client")))
                    .andExpect(status().isOk());
        }

        @Test
        void appRole_postTransaction_returns403() throws Exception {
            mockMvc.perform(post("/v1/transactions")
                            .with(httpBasic("app-client", "app-client"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_TRANSACTION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"));
        }

        @Test
        void appRole_putTransaction_returns403() throws Exception {
            mockMvc.perform(put("/v1/transactions/{id}", UUID.randomUUID())
                            .with(httpBasic("app-client", "app-client"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"version\": 0}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void appRole_deleteTransaction_returns403() throws Exception {
            mockMvc.perform(delete("/v1/transactions/{id}", UUID.randomUUID())
                            .with(httpBasic("app-client", "app-client")))
                    .andExpect(status().isForbidden());
        }

        @Test
        void appRole_postCategory_returns403() throws Exception {
            mockMvc.perform(post("/v1/categories")
                            .with(httpBasic("app-client", "app-client"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"TestCategory\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void appRole_putCategory_returns403() throws Exception {
            mockMvc.perform(put("/v1/categories/{id}", UUID.randomUUID())
                            .with(httpBasic("app-client", "app-client"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"Updated\", \"version\": 0}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void appRole_deleteCategory_returns403() throws Exception {
            mockMvc.perform(delete("/v1/categories/{id}", UUID.randomUUID())
                            .with(httpBasic("app-client", "app-client")))
                    .andExpect(status().isForbidden());
        }
    }

    // ========================================================================
    // US3: BACKOFFICE Role Full CRUD (P2)
    // ========================================================================

    @Nested
    class US3_BackofficeRole {

        @Test
        void backofficeRole_postTransaction_returns201() throws Exception {
            mockMvc.perform(post("/v1/transactions")
                            .with(httpBasic("backoffice", "backoffice"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_TRANSACTION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"));
        }

        @Test
        void backofficeRole_getTransactions_returns200() throws Exception {
            mockMvc.perform(get("/v1/transactions")
                            .with(httpBasic("backoffice", "backoffice")))
                    .andExpect(status().isOk());
        }

        @Test
        void backofficeRole_putTransaction_returns200() throws Exception {
            // Create transaction first
            String response = mockMvc.perform(post("/v1/transactions")
                            .with(httpBasic("backoffice", "backoffice"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_TRANSACTION_JSON))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String txId = extractId(response);

            // Update it
            mockMvc.perform(put("/v1/transactions/{id}", txId)
                            .with(httpBasic("backoffice", "backoffice"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"description\": \"Updated\", \"version\": 0}"))
                    .andExpect(status().isOk());
        }

        @Test
        void backofficeRole_deleteTransaction_returns204() throws Exception {
            // Create transaction first
            String response = mockMvc.perform(post("/v1/transactions")
                            .with(httpBasic("backoffice", "backoffice"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_TRANSACTION_JSON))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String txId = extractId(response);

            // Delete it
            mockMvc.perform(delete("/v1/transactions/{id}", txId)
                            .with(httpBasic("backoffice", "backoffice")))
                    .andExpect(status().isNoContent());
        }

        @Test
        void backofficeRole_postCategory_returns201() throws Exception {
            mockMvc.perform(post("/v1/categories")
                            .with(httpBasic("backoffice", "backoffice"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"BackofficeTestCategory\"}"))
                    .andExpect(status().isCreated());
        }

        @Test
        void backofficeRole_getCategories_returns200() throws Exception {
            mockMvc.perform(get("/v1/categories")
                            .with(httpBasic("backoffice", "backoffice")))
                    .andExpect(status().isOk());
        }

        @Test
        void backofficeRole_getTransactionSummary_returns200() throws Exception {
            mockMvc.perform(get("/v1/transactions/summary/expenses")
                            .with(httpBasic("backoffice", "backoffice")))
                    .andExpect(status().isOk());
        }

        @Test
        void backofficeRole_actuatorInfo_returns403() throws Exception {
            mockMvc.perform(get("/actuator/info")
                            .with(httpBasic("backoffice", "backoffice")))
                    .andExpect(status().isForbidden());
        }
    }

    // ========================================================================
    // US4: ADMIN Role Full System Access (P3)
    // ========================================================================

    @Nested
    class US4_AdminRole {

        @Test
        void adminRole_actuatorInfo_returns200() throws Exception {
            mockMvc.perform(get("/actuator/info")
                            .with(httpBasic("admin", "admin")))
                    .andExpect(status().isOk());
        }

        @Test
        void adminRole_postTransaction_returns201() throws Exception {
            mockMvc.perform(post("/v1/transactions")
                            .with(httpBasic("admin", "admin"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_TRANSACTION_JSON))
                    .andExpect(status().isCreated());
        }

        @Test
        void adminRole_getTransactions_returns200() throws Exception {
            mockMvc.perform(get("/v1/transactions")
                            .with(httpBasic("admin", "admin")))
                    .andExpect(status().isOk());
        }

        @Test
        void appRole_actuatorInfo_returns403() throws Exception {
            mockMvc.perform(get("/actuator/info")
                            .with(httpBasic("app-client", "app-client")))
                    .andExpect(status().isForbidden());
        }
    }

    // ========================================================================
    // US5: Health Check Publicly Accessible (P3)
    // ========================================================================

    @Nested
    class US5_HealthCheck {

        @Test
        void healthEndpoint_noAuth_returns200() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        void actuatorInfo_noAuth_returns401() throws Exception {
            mockMvc.perform(get("/actuator/info"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void apiEndpoint_noAuth_returns401() throws Exception {
            mockMvc.perform(get("/v1/transactions"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ========================================================================
    // FR-014: Failed Authentication Logging
    // ========================================================================

    @Nested
    class FailedAuthLogging {

        @Test
        void failedAuth_returns401WithCorrectFormat() throws Exception {
            mockMvc.perform(get("/v1/transactions")
                            .with(httpBasic("unknown-user", "bad-password")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").value("Authentication required. Provide valid credentials."))
                    .andExpect(jsonPath("$.details").isEmpty())
                    .andExpect(header().string("WWW-Authenticate", "Basic realm=\"MoneyTrak API\""));
        }
    }

    // ========================================================================
    // Portfolio Readings Security Tests
    // ========================================================================

    @Nested
    class PortfolioReadingsSecurity {

        private static final String VALID_ACCOUNT_JSON = """
                {
                    "name": "Test Bank",
                    "type": "BANK",
                    "currency": "USD"
                }
                """;

        // AUTH-001: No auth to /v1/accounts → 401
        @Test
        void unauthenticated_getAccounts_returns401() throws Exception {
            mockMvc.perform(get("/v1/accounts"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"));
        }

        // AUTH-002: No auth to /v1/readings → 401
        @Test
        void unauthenticated_getLatestReadings_returns401() throws Exception {
            mockMvc.perform(get("/v1/readings/latest"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        // AUTH-003: APP role GET /v1/accounts → 200
        @Test
        void appRole_getAccounts_returns200() throws Exception {
            mockMvc.perform(get("/v1/accounts")
                            .with(httpBasic("app-client", "app-client")))
                    .andExpect(status().isOk());
        }

        // AUTH-004: APP role GET /v1/readings/latest → 200
        @Test
        void appRole_getLatestReadings_returns200() throws Exception {
            mockMvc.perform(get("/v1/readings/latest")
                            .with(httpBasic("app-client", "app-client")))
                    .andExpect(status().isOk());
        }

        // AUTH-005: APP role POST /v1/accounts → 403
        @Test
        void appRole_createAccount_returns403() throws Exception {
            mockMvc.perform(post("/v1/accounts")
                            .with(httpBasic("app-client", "app-client"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_ACCOUNT_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"));
        }

        // AUTH-006: BACKOFFICE role full CRUD accounts → all succeed
        @Test
        void backofficeRole_fullCrudAccounts_allSucceed() throws Exception {
            // Create
            String createResponse = mockMvc.perform(post("/v1/accounts")
                            .with(httpBasic("backoffice", "backoffice"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_ACCOUNT_JSON))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String accountId = extractId(createResponse);

            // Read
            mockMvc.perform(get("/v1/accounts/{id}", accountId)
                            .with(httpBasic("backoffice", "backoffice")))
                    .andExpect(status().isOk());

            // Update
            mockMvc.perform(put("/v1/accounts/{id}", accountId)
                            .with(httpBasic("backoffice", "backoffice"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Updated\",\"version\":0}"))
                    .andExpect(status().isOk());

            // Delete
            mockMvc.perform(delete("/v1/accounts/{id}", accountId)
                            .with(httpBasic("backoffice", "backoffice")))
                    .andExpect(status().isNoContent());
        }

        // AUTH-007: ADMIN role full CRUD readings → all succeed
        @Test
        void adminRole_fullCrudReadings_allSucceed() throws Exception {
            // First create an account
            String accountResponse = mockMvc.perform(post("/v1/accounts")
                            .with(httpBasic("admin", "admin"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_ACCOUNT_JSON))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String accountId = extractId(accountResponse);

            // Create reading
            String readingJson = """
                    {
                        "accountId": "%s",
                        "amount": 15000.50,
                        "readingDate": "2026-02-28T10:00:00Z"
                    }
                    """.formatted(accountId);

            String createResponse = mockMvc.perform(post("/v1/readings")
                            .with(httpBasic("admin", "admin"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(readingJson))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String readingId = extractId(createResponse);

            // Read
            mockMvc.perform(get("/v1/readings/{id}", readingId)
                            .with(httpBasic("admin", "admin")))
                    .andExpect(status().isOk());

            // Update
            mockMvc.perform(put("/v1/readings/{id}", readingId)
                            .with(httpBasic("admin", "admin"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\":20000,\"version\":0}"))
                    .andExpect(status().isOk());

            // Delete
            mockMvc.perform(delete("/v1/readings/{id}", readingId)
                            .with(httpBasic("admin", "admin")))
                    .andExpect(status().isNoContent());
        }
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
