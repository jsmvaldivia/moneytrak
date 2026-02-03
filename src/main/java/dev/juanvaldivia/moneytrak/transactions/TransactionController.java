package dev.juanvaldivia.moneytrak.transactions;

import dev.juanvaldivia.moneytrak.transactions.dto.TransactionCreationDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionUpdateDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for transaction management.
 * Renamed from ExpenseController to support broader transaction scope (income + expenses).
 *
 * <p>Provides CRUD endpoints for transactions with category linking and filtering.
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
     * List all transactions or filter by category/stability.
     * GET /v1/transactions?categoryId={uuid}&stability={FIXED|VARIABLE}
     *
     * Returns transactions ordered by date descending (newest first).
     * Supports filtering by categoryId and/or transactionStability.
     * Returns empty array if valid filters have no matching transactions.
     *
     * @param categoryId optional category UUID for filtering
     * @param stability optional transaction stability for filtering
     * @return 200 OK with list of transactions
     */
    @GetMapping
    public ResponseEntity<List<TransactionDto>> listTransactions(
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(required = false) TransactionStability stability
    ) {
        if (categoryId != null) {
            // Filter by category
            List<TransactionDto> transactions = service.listTransactionsByCategory(categoryId);
            return ResponseEntity.ok(transactions);
        } else if (stability != null) {
            // Filter by stability
            List<TransactionDto> transactions = service.listTransactionsByStability(stability);
            return ResponseEntity.ok(transactions);
        } else {
            // Return all transactions
            List<TransactionDto> transactions = service.listTransactions();
            return ResponseEntity.ok(transactions);
        }
    }

    /**
     * Get transaction by ID.
     * GET /v1/transactions/{id}
     *
     * @param id transaction UUID
     * @return 200 OK with transaction details including category info
     * @throws dev.juanvaldivia.moneytrak.expenses.exception.NotFoundException if not found (404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable UUID id) {
        TransactionDto transaction = service.getTransactionById(id);
        return ResponseEntity.ok(transaction);
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
     * @throws dev.juanvaldivia.moneytrak.expenses.exception.NotFoundException if transaction or category not found (404)
     * @throws dev.juanvaldivia.moneytrak.expenses.exception.ConflictException if version mismatch (409)
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> updateTransaction(
        @PathVariable UUID id,
        @Valid @RequestBody TransactionUpdateDto dto
    ) {
        TransactionDto updated = service.updateTransaction(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete transaction by ID.
     * DELETE /v1/transactions/{id}
     *
     * @param id transaction UUID
     * @return 204 No Content
     * @throws dev.juanvaldivia.moneytrak.expenses.exception.NotFoundException if not found (404)
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
     * @return 200 OK with total expense amount
     */
    @GetMapping("/summary/expenses")
    public ResponseEntity<java.math.BigDecimal> getExpenseTotal() {
        java.math.BigDecimal total = service.calculateExpenseTotal();
        return ResponseEntity.ok(total);
    }

    /**
     * Get total of all INCOME transactions.
     * GET /v1/transactions/summary/income
     *
     * @return 200 OK with total income amount
     */
    @GetMapping("/summary/income")
    public ResponseEntity<java.math.BigDecimal> getIncomeTotal() {
        java.math.BigDecimal total = service.calculateIncomeTotal();
        return ResponseEntity.ok(total);
    }
}
