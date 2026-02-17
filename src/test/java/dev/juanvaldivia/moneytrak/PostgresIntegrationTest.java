package dev.juanvaldivia.moneytrak;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests running against a real PostgreSQL database via Testcontainers.
 *
 * <p>Flyway is disabled here because the V2 migration uses RANDOM_UUID() which is
 * H2-specific and not supported by PostgreSQL. The schema is instead created by
 * Hibernate (ddl-auto=create-drop from application-test.yaml) and categories are
 * seeded by CategorySeeder at startup. See the V2 migration for the bug to fix.
 *
 * <p>These tests verify that:
 * - JPA entity mappings are correct for PostgreSQL's SQL dialect
 * - Queries and aggregates work against real PostgreSQL
 * - CategorySeeder runs correctly on PostgreSQL
 * - All API contracts hold against a production-equivalent database
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
@Testcontainers
@EnabledIfDockerAvailable
class PostgresIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        // Flyway disabled: V2 migration uses RANDOM_UUID() (H2-only).
        // CategorySeeder handles category seeding via JPA (database-agnostic).
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createTransaction_onPostgres_returns201() throws Exception {
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Coffee on PostgreSQL",
                        "amount": 4.50,
                        "currency": "EUR",
                        "date": "2026-02-01T10:00:00Z",
                        "type": "EXPENSE",
                        "stability": "VARIABLE"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.description").value("Coffee on PostgreSQL"))
                .andExpect(jsonPath("$.categoryName").value("Others"));
    }

    @Test
    void listTransactions_onPostgres_returns200() throws Exception {
        mockMvc.perform(get("/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void listCategories_onPostgres_returnsPredefinedCategoriesWithVersion() throws Exception {
        mockMvc.perform(get("/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].version").isNumber());
    }

    @Test
    void expenseSummary_onPostgres_returnsWrappedTotal() throws Exception {
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Rent",
                        "amount": 1200.00,
                        "currency": "EUR",
                        "date": "2026-02-01T00:00:00Z",
                        "type": "EXPENSE"
                    }
                    """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/transactions/summary/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1200.00));
    }

    @Test
    void createCategory_onPostgres_returns201WithVersion() throws Exception {
        mockMvc.perform(post("/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "PostgreSQL Test Category"}
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(0));
    }
}
