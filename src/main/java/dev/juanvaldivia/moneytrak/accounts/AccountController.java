package dev.juanvaldivia.moneytrak.accounts;

import dev.juanvaldivia.moneytrak.accounts.dto.AccountCreationDto;
import dev.juanvaldivia.moneytrak.accounts.dto.AccountDto;
import dev.juanvaldivia.moneytrak.accounts.dto.AccountUpdateDto;
import dev.juanvaldivia.moneytrak.readings.ReadingService;
import dev.juanvaldivia.moneytrak.readings.dto.ReadingDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for account management.
 * Provides CRUD endpoints for financial accounts with 1000 account limit enforcement.
 * All endpoints are versioned under /v1/accounts.
 */
@Tag(name = "Accounts", description = "Account management endpoints")
@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final ReadingService readingService;

    public AccountController(AccountService accountService, ReadingService readingService) {
        this.accountService = accountService;
        this.readingService = readingService;
    }

    /**
     * Create a new account.
     * POST /v1/accounts
     *
     * @param dto account creation data
     * @return 201 Created with Location header and created account
     * @throws dev.juanvaldivia.moneytrak.accounts.exception.AccountLimitExceededException if 1000 accounts exist (409)
     */
    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountCreationDto dto) {
        AccountDto created = accountService.createAccount(dto);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /**
     * List all accounts ordered by name (then by id for deterministic ordering).
     * GET /v1/accounts
     *
     * @return 200 OK with list of all accounts
     */
    @GetMapping
    public ResponseEntity<List<AccountDto>> listAccounts() {
        return ResponseEntity.ok(accountService.listAccounts());
    }

    /**
     * Get account by ID.
     * GET /v1/accounts/{id}
     *
     * @param id account UUID
     * @return 200 OK with account details
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found (404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    /**
     * Update existing account with optimistic locking.
     * PUT /v1/accounts/{id}
     *
     * Partial updates supported - null fields preserve existing values.
     *
     * @param id account UUID
     * @param dto update data with version for optimistic locking
     * @return 200 OK with updated account
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found (404)
     * @throws dev.juanvaldivia.moneytrak.exception.ConflictException if version mismatch (409)
     */
    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(
        @PathVariable UUID id,
        @Valid @RequestBody AccountUpdateDto dto
    ) {
        return ResponseEntity.ok(accountService.updateAccount(id, dto));
    }

    /**
     * Delete account by ID.
     * DELETE /v1/accounts/{id}
     *
     * @param id account UUID
     * @return 204 No Content
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found (404)
     * @throws dev.juanvaldivia.moneytrak.accounts.exception.AccountInUseException if account has active readings (409)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all readings for a specific account with pagination.
     * GET /v1/accounts/{id}/readings?page=0&size=50&sort=readingDate,desc
     *
     * Returns active (non-deleted) readings ordered by date descending.
     * Default page size is 50 readings.
     *
     * @param id account UUID
     * @param pageable pagination and sort parameters (default: page=0, size=50, sort=readingDate,desc)
     * @return 200 OK with page of readings
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if account not found (404)
     */
    @GetMapping("/{id}/readings")
    public ResponseEntity<Page<ReadingDto>> getAccountReadings(
        @PathVariable UUID id,
        @PageableDefault(size = 50, sort = "readingDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(readingService.getAccountReadingHistory(id, pageable));
    }
}
