package dev.juanvaldivia.moneytrak.readings;

import dev.juanvaldivia.moneytrak.accounts.Account;
import dev.juanvaldivia.moneytrak.accounts.AccountRepository;
import dev.juanvaldivia.moneytrak.accounts.AccountType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Reading Controller.
 * Tests REST API endpoints for portfolio reading management with soft deletion support.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
class ReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReadingRepository readingRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EntityManager entityManager;

    // Test 1: Create with valid data → 201 with Location
    @Test
    void createReading_WithValidData_ShouldReturn201Created() throws Exception {
        // Given: Create an account
        Account account = accountRepository.save(Account.create("Test Bank", AccountType.BANK, "USD"));

        // When/Then: POST creates reading with Location header
        mockMvc.perform(post("/v1/readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"" + account.id() + "\",\"amount\":15000.50,\"readingDate\":\"2026-02-28T10:00:00Z\"}"))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.accountId").value(account.id().toString()))
            .andExpect(jsonPath("$.amount").value(15000.50))
            .andExpect(jsonPath("$.version").value(0));
    }

    // Test 2: Get by ID → 200 with account details embedded
    @Test
    void getReadingById_WhenExists_ShouldReturn200WithAccountDetails() throws Exception {
        // Given: Create account and reading
        Account account = accountRepository.save(Account.create("Test Broker", AccountType.BROKER, "EUR"));
        Reading reading = readingRepository.save(Reading.create(account, new BigDecimal("25000.75"), ZonedDateTime.now()));

        // When/Then: GET returns reading with embedded account details
        mockMvc.perform(get("/v1/readings/{id}", reading.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(reading.id().toString()))
            .andExpect(jsonPath("$.accountId").value(account.id().toString()))
            .andExpect(jsonPath("$.accountName").value("Test Broker"))
            .andExpect(jsonPath("$.accountType").value("BROKER"))
            .andExpect(jsonPath("$.accountCurrency").value("EUR"))
            .andExpect(jsonPath("$.amount").value(25000.75));
    }

    // Test 3: Future date → 400 validation
    @Test
    void createReading_WithFutureDate_ShouldReturn400BadRequest() throws Exception {
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));

        mockMvc.perform(post("/v1/readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"" + account.id() + "\",\"amount\":1000,\"readingDate\":\"2030-01-01T00:00:00Z\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("ValidationError"));
    }

    // Test 4: Non-existent accountId → 404
    @Test
    void createReading_WithNonExistentAccount_ShouldReturn404NotFound() throws Exception {
        mockMvc.perform(post("/v1/readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"00000000-0000-0000-0000-000000000000\",\"amount\":1000,\"readingDate\":\"2026-02-28T10:00:00Z\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    // Test 5: Update amount/date → 200
    @Test
    void updateReading_WithValidData_ShouldReturn200Ok() throws Exception {
        // Given: Create reading
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));
        Reading reading = readingRepository.save(Reading.create(account, new BigDecimal("1000"), ZonedDateTime.now()));

        // When/Then: PUT updates reading
        mockMvc.perform(put("/v1/readings/{id}", reading.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":2000.50,\"readingDate\":\"2026-02-27T10:00:00Z\",\"version\":0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.amount").value(2000.50))
            .andExpect(jsonPath("$.version").value(1));
    }

    // Test 6: Concurrent update → 409 Conflict
    @Test
    void updateReading_WithStaleVersion_ShouldReturn409Conflict() throws Exception {
        // Given: Create and update reading (version becomes 1)
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));
        Reading reading = readingRepository.save(Reading.create(account, new BigDecimal("1000"), ZonedDateTime.now()));
        reading.update(new BigDecimal("2000"), ZonedDateTime.now());
        readingRepository.save(reading);
        entityManager.flush(); // Ensure version is incremented

        // When/Then: PUT with stale version returns 409
        mockMvc.perform(put("/v1/readings/{id}", reading.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":3000,\"version\":0}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    // Test 7: Negative amount → accepted (FR-012A)
    @Test
    void createReading_WithNegativeAmount_ShouldBeAccepted() throws Exception {
        Account account = accountRepository.save(Account.create("Margin Account", AccountType.BROKER, "USD"));

        mockMvc.perform(post("/v1/readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"" + account.id() + "\",\"amount\":-1000.50,\"readingDate\":\"2026-02-28T10:00:00Z\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(-1000.50));
    }

    // Test 8: 8 decimal places → accepted (0.12345678 BTC)
    @Test
    void createReading_With8DecimalPlaces_ShouldPreserveAllDecimals() throws Exception {
        Account account = accountRepository.save(Account.create("Crypto Wallet", AccountType.CRYPTO, "BTC"));

        mockMvc.perform(post("/v1/readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"" + account.id() + "\",\"amount\":0.12345678,\"readingDate\":\"2026-02-28T10:00:00Z\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(0.12345678));
    }

    // Test 9: Latest with multiple dates → most recent only
    @Test
    void getLatestReadings_WithMultipleDates_ShouldReturnMostRecentOnly() throws Exception {
        // Given: Account with 3 readings at different times
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));
        readingRepository.save(Reading.create(account, new BigDecimal("1000"), ZonedDateTime.parse("2026-02-26T10:00:00Z")));
        readingRepository.save(Reading.create(account, new BigDecimal("2000"), ZonedDateTime.parse("2026-02-27T10:00:00Z")));
        Reading latest = readingRepository.save(Reading.create(account, new BigDecimal("3000"), ZonedDateTime.parse("2026-02-28T10:00:00Z")));

        // When/Then: Latest returns only the most recent
        mockMvc.perform(get("/v1/readings/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(latest.id().toString()))
            .andExpect(jsonPath("$[0].amount").value(3000));
    }

    // Test 10: Latest with 3 accounts → 3 readings (one per account)
    @Test
    void getLatestReadings_WithMultipleAccounts_ShouldReturnOnePerAccount() throws Exception {
        // Given: 3 accounts each with readings
        Account acc1 = accountRepository.save(Account.create("Account A", AccountType.BANK, "USD"));
        Account acc2 = accountRepository.save(Account.create("Account B", AccountType.BROKER, "EUR"));
        Account acc3 = accountRepository.save(Account.create("Account C", AccountType.CRYPTO, "BTC"));

        readingRepository.save(Reading.create(acc1, new BigDecimal("1000"), ZonedDateTime.now()));
        readingRepository.save(Reading.create(acc2, new BigDecimal("2000"), ZonedDateTime.now()));
        readingRepository.save(Reading.create(acc3, new BigDecimal("3000"), ZonedDateTime.now()));

        // When/Then: Latest returns 3 readings
        mockMvc.perform(get("/v1/readings/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));
    }

    // Test 11: Latest when no readings → 200 with []
    @Test
    void getLatestReadings_WhenNoReadings_ShouldReturn200WithEmptyArray() throws Exception {
        readingRepository.deleteAll();

        mockMvc.perform(get("/v1/readings/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    // Test 12: Latest excluding accounts with no readings
    @Test
    void getLatestReadings_ShouldExcludeAccountsWithNoReadings() throws Exception {
        // Given: 2 accounts, only 1 has readings
        Account withReading = accountRepository.save(Account.create("With Reading", AccountType.BANK, "USD"));
        Account withoutReading = accountRepository.save(Account.create("Without Reading", AccountType.BANK, "EUR"));

        readingRepository.save(Reading.create(withReading, new BigDecimal("1000"), ZonedDateTime.now()));

        // When/Then: Latest returns only 1 reading
        mockMvc.perform(get("/v1/readings/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].accountName").value("With Reading"));
    }

    // Test 13: Soft delete → 204, marked as deleted
    @Test
    void deleteReading_ShouldSoftDelete() throws Exception {
        // Given: Create reading
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));
        Reading reading = readingRepository.save(Reading.create(account, new BigDecimal("1000"), ZonedDateTime.now()));

        // When: DELETE
        mockMvc.perform(delete("/v1/readings/{id}", reading.id()))
            .andExpect(status().isNoContent());

        // Then: Reading is marked as deleted (GET returns 404)
        mockMvc.perform(get("/v1/readings/{id}", reading.id()))
            .andExpect(status().isNotFound());
    }

    // Test 14: Latest excludes soft-deleted
    @Test
    void getLatestReadings_ShouldExcludeSoftDeleted() throws Exception {
        // Given: Account with reading, then soft delete it
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));
        Reading reading = readingRepository.save(Reading.create(account, new BigDecimal("1000"), ZonedDateTime.now()));

        mockMvc.perform(delete("/v1/readings/{id}", reading.id()))
            .andExpect(status().isNoContent());

        // When/Then: Latest excludes soft-deleted reading
        mockMvc.perform(get("/v1/readings/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    // Test 15: Update soft-deleted → 404
    @Test
    void updateReading_WhenSoftDeleted_ShouldReturn404() throws Exception {
        // Given: Soft-deleted reading
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));
        Reading reading = readingRepository.save(Reading.create(account, new BigDecimal("1000"), ZonedDateTime.now()));

        mockMvc.perform(delete("/v1/readings/{id}", reading.id()))
            .andExpect(status().isNoContent());

        // When/Then: UPDATE returns 404
        mockMvc.perform(put("/v1/readings/{id}", reading.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":2000,\"version\":0}"))
            .andExpect(status().isNotFound());
    }

    // Test 16: Latest with only soft-deleted → excluded
    @Test
    void getLatestReadings_WithOnlySoftDeleted_ShouldExcludeAccount() throws Exception {
        // Given: Account with reading that gets soft-deleted
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));
        Reading reading = readingRepository.save(Reading.create(account, new BigDecimal("1000"), ZonedDateTime.now()));

        mockMvc.perform(delete("/v1/readings/{id}", reading.id()))
            .andExpect(status().isNoContent());

        // When/Then: Account not in latest
        mockMvc.perform(get("/v1/readings/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.accountId == '" + account.id() + "')]").doesNotExist());
    }

    // Test 17: Delete account with active readings → 409 with count
    @Test
    void deleteAccount_WithActiveReadings_ShouldReturn409Conflict() throws Exception {
        // Given: Account with active reading
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));
        readingRepository.save(Reading.create(account, new BigDecimal("1000"), ZonedDateTime.now()));

        // When/Then: DELETE account returns 409
        mockMvc.perform(delete("/v1/accounts/{id}", account.id()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value(containsString("1 active reading")));
    }

    // Test 18: Delete account with only soft-deleted → 204
    @Test
    void deleteAccount_WithOnlySoftDeletedReadings_ShouldReturn204() throws Exception {
        // Given: Account with soft-deleted reading
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));
        Reading reading = readingRepository.save(Reading.create(account, new BigDecimal("1000"), ZonedDateTime.now()));

        mockMvc.perform(delete("/v1/readings/{id}", reading.id()))
            .andExpect(status().isNoContent());

        // When/Then: DELETE account succeeds
        mockMvc.perform(delete("/v1/accounts/{id}", account.id()))
            .andExpect(status().isNoContent());
    }

    // Test 19: Delete account with mixed → 409 with active count only
    @Test
    void deleteAccount_WithMixedReadings_ShouldReturn409WithActiveCountOnly() throws Exception {
        // Given: Account with 2 active + 1 soft-deleted
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));
        Reading r1 = readingRepository.save(Reading.create(account, new BigDecimal("1000"), ZonedDateTime.parse("2026-02-26T10:00:00Z")));
        readingRepository.save(Reading.create(account, new BigDecimal("2000"), ZonedDateTime.parse("2026-02-27T10:00:00Z")));
        readingRepository.save(Reading.create(account, new BigDecimal("3000"), ZonedDateTime.parse("2026-02-28T10:00:00Z")));

        // Soft delete one
        mockMvc.perform(delete("/v1/readings/{id}", r1.id()))
            .andExpect(status().isNoContent());

        // When/Then: DELETE account returns 409 with count=2 (active only)
        mockMvc.perform(delete("/v1/accounts/{id}", account.id()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(containsString("2 active reading")));
    }

    // Test 20: Account history with 10 readings → DESC order
    @Test
    void getAccountReadingHistory_ShouldReturnDescendingOrder() throws Exception {
        // Given: Account with 10 readings
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));
        for (int i = 1; i <= 10; i++) {
            readingRepository.save(Reading.create(
                account,
                new BigDecimal(i * 1000),
                ZonedDateTime.parse("2026-02-" + String.format("%02d", i) + "T10:00:00Z")
            ));
        }

        // When/Then: History in DESC order (most recent first) - paginated response
        mockMvc.perform(get("/v1/accounts/{id}/readings", account.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(10)))
            .andExpect(jsonPath("$.content[0].amount").value(10000))
            .andExpect(jsonPath("$.content[9].amount").value(1000))
            .andExpect(jsonPath("$.totalElements").value(10))
            .andExpect(jsonPath("$.size").value(50)); // Default page size
    }

    // Test 21: Account history excludes soft-deleted
    @Test
    void getAccountReadingHistory_ShouldExcludeSoftDeleted() throws Exception {
        // Given: Account with 3 readings, 1 soft-deleted
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));
        Reading r1 = readingRepository.save(Reading.create(account, new BigDecimal("1000"), ZonedDateTime.now()));
        readingRepository.save(Reading.create(account, new BigDecimal("2000"), ZonedDateTime.now()));
        readingRepository.save(Reading.create(account, new BigDecimal("3000"), ZonedDateTime.now()));

        mockMvc.perform(delete("/v1/readings/{id}", r1.id()))
            .andExpect(status().isNoContent());

        // When/Then: History shows only 2 active - paginated response
        mockMvc.perform(get("/v1/accounts/{id}/readings", account.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.totalElements").value(2));
    }

    // Test 22: Account history when empty → 200 with empty page
    @Test
    void getAccountReadingHistory_WhenEmpty_ShouldReturn200WithEmptyArray() throws Exception {
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));

        mockMvc.perform(get("/v1/accounts/{id}/readings", account.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(0)))
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    // Test 23: Account history with non-existent account → 404
    @Test
    void getAccountReadingHistory_WithNonExistentAccount_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/v1/accounts/{id}/readings", "00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound());
    }

    // Test 24: Account history with duplicate dates → all returned
    @Test
    void getAccountReadingHistory_WithDuplicateDates_ShouldReturnAll() throws Exception {
        // Given: Account with 3 readings at same time
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));
        ZonedDateTime sameTime = ZonedDateTime.parse("2026-02-28T10:00:00Z");
        readingRepository.save(Reading.create(account, new BigDecimal("1000"), sameTime));
        readingRepository.save(Reading.create(account, new BigDecimal("2000"), sameTime));
        readingRepository.save(Reading.create(account, new BigDecimal("3000"), sameTime));

        // When/Then: All 3 returned - paginated response
        mockMvc.perform(get("/v1/accounts/{id}/readings", account.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(3)))
            .andExpect(jsonPath("$.totalElements").value(3));
    }

    // Test 25: Get soft-deleted reading → 404
    @Test
    void getReadingById_WhenSoftDeleted_ShouldReturn404() throws Exception {
        // Given: Soft-deleted reading
        Account account = accountRepository.save(Account.create("Test", AccountType.BANK, "USD"));
        Reading reading = readingRepository.save(Reading.create(account, new BigDecimal("1000"), ZonedDateTime.now()));

        mockMvc.perform(delete("/v1/readings/{id}", reading.id()))
            .andExpect(status().isNoContent());

        // When/Then: GET returns 404
        mockMvc.perform(get("/v1/readings/{id}", reading.id()))
            .andExpect(status().isNotFound());
    }

    // Test 26: Latest ordered by account name + ID
    @Test
    void getLatestReadings_ShouldBeOrderedByAccountNameAndId() throws Exception {
        // Given: 3 accounts with readings
        Account accZ = accountRepository.save(Account.create("Zebra", AccountType.BANK, "USD"));
        Account accA = accountRepository.save(Account.create("Apple", AccountType.BANK, "USD"));
        Account accM = accountRepository.save(Account.create("Microsoft", AccountType.BANK, "USD"));

        readingRepository.save(Reading.create(accZ, new BigDecimal("1000"), ZonedDateTime.now()));
        readingRepository.save(Reading.create(accA, new BigDecimal("2000"), ZonedDateTime.now()));
        readingRepository.save(Reading.create(accM, new BigDecimal("3000"), ZonedDateTime.now()));

        // When/Then: Latest ordered alphabetically by account name
        mockMvc.perform(get("/v1/readings/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].accountName").value("Apple"))
            .andExpect(jsonPath("$[1].accountName").value("Microsoft"))
            .andExpect(jsonPath("$[2].accountName").value("Zebra"));
    }

    // Test 27: Update cannot change accountId (not in DTO)
    @Test
    void updateReading_CannotChangeAccountId() throws Exception {
        // Given: Reading with account A
        Account accountA = accountRepository.save(Account.create("Account A", AccountType.BANK, "USD"));
        Account accountB = accountRepository.save(Account.create("Account B", AccountType.BANK, "EUR"));
        Reading reading = readingRepository.save(Reading.create(accountA, new BigDecimal("1000"), ZonedDateTime.now()));

        // When: Update (no accountId in UpdateDto)
        mockMvc.perform(put("/v1/readings/{id}", reading.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":2000,\"version\":0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").value(accountA.id().toString()));

        // Then: AccountId unchanged
        Reading updated = readingRepository.findById(reading.id()).orElseThrow();
        assert updated.account().id().equals(accountA.id());
    }
}
