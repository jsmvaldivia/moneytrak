package dev.juanvaldivia.moneytrak.transactions;

import dev.juanvaldivia.moneytrak.transactions.dto.SummaryDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionCreationDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionUpdateDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller for transaction management.
 * Renamed from ExpenseController to support broader transaction scope (income + expenses).
 *
 * <p>Provides CRUD endpoints for transactions with category linking and composable filtering.
 * All endpoints are versioned under /v1/transactions.
 */
@RestController
@RequestMapping("/v1/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    /**
     * Create a new transaction.
     * POST /v1/transactions
     *
     * If categoryId is not provided, defaults to "Others" category.
     *
     * @param dto transaction creation data
     * @return 201 Created with Location header and created transaction
     */
    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@Valid @RequestBody TransactionCreationDto dto) {
        TransactionDto created = service.createTransaction(dto);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /**
     * List transactions with optional composable filters and pagination.
     * GET /v1/transactions?categoryId={uuid}&stability={FIXED|VARIABLE}&page=0&size=20&sort=date,desc
     *
     * Both filters are optional and can be combined.
     * Returns transactions ordered by date descending by default.
     *
     * @param categoryId optional category UUID for filtering
     * @param stability optional transaction stability for filtering
     * @param pageable pagination and sort parameters (default: page=0, size=20, sort=date,desc)
     * @return 200 OK with page of transactions
     */
    @GetMapping
    public ResponseEntity<Page<TransactionDto>> listTransactions(
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(required = false) TransactionStability stability,
        @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(service.listTransactions(categoryId, stability, pageable));
    }

    /**
     * Get transaction by ID.
     * GET /v1/transactions/{id}
     *
     * @param id transaction UUID
     * @return 200 OK with transaction details including category info
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found (404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getTransactionById(id));
    }

    /**
     * Update existing transaction with optimistic locking.
     * PUT /v1/transactions/{id}
     *
     * Partial updates supported - null fields preserve existing values.
     * If categoryId is provided, validates it exists.
     *
     * @param id transaction UUID
     * @param dto update data with version for optimistic locking
     * @return 200 OK with updated transaction
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if transaction or category not found (404)
     * @throws dev.juanvaldivia.moneytrak.exception.ConflictException if version mismatch (409)
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> updateTransaction(
        @PathVariable UUID id,
        @Valid @RequestBody TransactionUpdateDto dto
    ) {
        return ResponseEntity.ok(service.updateTransaction(id, dto));
    }

    /**
     * Delete transaction by ID.
     * DELETE /v1/transactions/{id}
     *
     * @param id transaction UUID
     * @return 204 No Content
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found (404)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
        service.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get total of all EXPENSE transactions.
     * GET /v1/transactions/summary/expenses
     *
     * @return 200 OK with summary containing total expense amount
     */
    @GetMapping("/summary/expenses")
    public ResponseEntity<SummaryDto> getExpenseTotal() {
        return ResponseEntity.ok(service.calculateExpenseTotal());
    }

    /**
     * Get total of all INCOME transactions.
     * GET /v1/transactions/summary/income
     *
     * @return 200 OK with summary containing total income amount
     */
    @GetMapping("/summary/income")
    public ResponseEntity<SummaryDto> getIncomeTotal() {
        return ResponseEntity.ok(service.calculateIncomeTotal());
    }
}
