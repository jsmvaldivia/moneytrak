package dev.juanvaldivia.moneytrak.expenses;

import dev.juanvaldivia.moneytrak.expenses.dto.ExpenseCreationDto;
import dev.juanvaldivia.moneytrak.expenses.dto.ExpenseDto;
import dev.juanvaldivia.moneytrak.expenses.dto.ExpenseUpdateDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for expense management operations.
 * Provides CRUD endpoints for tracking expenses with validation and optimistic locking.
 *
 * <p>All endpoints are versioned under /v1/expenses and return JSON responses.
 * Currency codes must be valid ISO 4217 codes. Dates must be in the past or present.
 *
 * @version 1.0
 */
@RestController
@RequestMapping("/v1/expenses")
public class ExpenseController {

    private final ExpenseService service;

    /**
     * Constructs an ExpenseController with the specified service.
     *
     * @param service the expense service to handle business logic
     */
    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    /**
     * Creates a new expense record.
     *
     * @param dto the expense creation data including description, amount, currency, and date
     * @return the created expense with generated ID and version 0
     * @throws org.springframework.web.bind.MethodArgumentNotValidException if validation fails (400)
     */
    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(@Valid @RequestBody ExpenseCreationDto dto) {
        ExpenseDto created = service.createExpense(dto);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /**
     * Retrieves all expenses ordered by date in reverse chronological order (newest first).
     *
     * @return list of all expenses, empty list if none exist
     */
    @GetMapping
    public ResponseEntity<List<ExpenseDto>> listExpenses() {
        List<ExpenseDto> expenses = service.listExpenses();
        return ResponseEntity.ok(expenses);
    }

    /**
     * Retrieves a specific expense by its unique identifier.
     *
     * @param id the UUID of the expense to retrieve
     * @return the expense details
     * @throws dev.juanvaldivia.moneytrak.expenses.exception.NotFoundException if expense not found (404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpense(@PathVariable UUID id) {
        ExpenseDto expense = service.getExpenseById(id);
        return ResponseEntity.ok(expense);
    }

    /**
     * Updates an existing expense with optimistic locking.
     * Partial updates are supported - null fields in the DTO will preserve existing values.
     *
     * @param id the UUID of the expense to update
     * @param dto the update data with version for optimistic locking
     * @return the updated expense with incremented version
     * @throws dev.juanvaldivia.moneytrak.expenses.exception.NotFoundException if expense not found (404)
     * @throws dev.juanvaldivia.moneytrak.expenses.exception.ConflictException if version mismatch (409)
     * @throws org.springframework.web.bind.MethodArgumentNotValidException if validation fails (400)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto> updateExpense(
        @PathVariable UUID id,
        @Valid @RequestBody ExpenseUpdateDto dto
    ) {
        ExpenseDto updated = service.updateExpense(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes an expense by its unique identifier.
     *
     * @param id the UUID of the expense to delete
     * @return 204 No Content on successful deletion
     * @throws dev.juanvaldivia.moneytrak.expenses.exception.NotFoundException if expense not found (404)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID id) {
        service.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
