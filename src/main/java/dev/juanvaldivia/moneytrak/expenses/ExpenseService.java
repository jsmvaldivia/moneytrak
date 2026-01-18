package dev.juanvaldivia.moneytrak.expenses;

import dev.juanvaldivia.moneytrak.expenses.dto.ExpenseCreationDto;
import dev.juanvaldivia.moneytrak.expenses.dto.ExpenseDto;
import dev.juanvaldivia.moneytrak.expenses.dto.ExpenseUpdateDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for expense management operations.
 * Defines business logic for creating, retrieving, updating, and deleting expenses.
 *
 * <p>Implementations should handle validation, persistence, and optimistic locking.
 *
 * @see LocalExpenseService
 */
public interface ExpenseService {
    /**
     * Creates a new expense record.
     *
     * @param dto the expense creation data
     * @return the created expense with generated ID and timestamps
     */
    ExpenseDto createExpense(ExpenseCreationDto dto);

    /**
     * Retrieves all expenses ordered by date descending.
     *
     * @return list of all expenses, empty if none exist
     */
    List<ExpenseDto> listExpenses();

    /**
     * Retrieves a specific expense by ID.
     *
     * @param id the expense UUID
     * @return the expense details
     * @throws dev.juanvaldivia.moneytrak.expenses.exception.NotFoundException if not found
     */
    ExpenseDto getExpenseById(UUID id);

    /**
     * Updates an existing expense with optimistic locking.
     *
     * @param id the expense UUID
     * @param dto the update data including version
     * @return the updated expense with incremented version
     * @throws dev.juanvaldivia.moneytrak.expenses.exception.NotFoundException if not found
     * @throws dev.juanvaldivia.moneytrak.expenses.exception.ConflictException if version mismatch
     */
    ExpenseDto
    updateExpense(UUID id, ExpenseUpdateDto dto);

    /**
     * Deletes an expense by ID.
     *
     * @param id the expense UUID
     * @throws dev.juanvaldivia.moneytrak.expenses.exception.NotFoundException if not found
     */
    void deleteExpense(UUID id);
}
