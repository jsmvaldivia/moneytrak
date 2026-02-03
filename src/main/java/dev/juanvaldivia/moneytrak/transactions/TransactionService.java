package dev.juanvaldivia.moneytrak.transactions;

import dev.juanvaldivia.moneytrak.transactions.dto.TransactionCreationDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionUpdateDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for transaction management operations.
 * Renamed from ExpenseService to support broader transaction scope.
 *
 * <p>Handles transaction CRUD with category linking, type classification, and stability.
 */
public interface TransactionService {

    /**
     * Create a new transaction.
     * If categoryId is not provided, defaults to "Others" category.
     *
     * @param dto transaction creation data
     * @return created transaction with category details
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if category not found
     */
    TransactionDto createTransaction(TransactionCreationDto dto);

    /**
     * List all transactions ordered by date descending.
     *
     * @return list of all transactions
     */
    List<TransactionDto> listTransactions();

    /**
     * List transactions filtered by category ID.
     *
     * @param categoryId category UUID
     * @return list of transactions in the category (empty if none)
     */
    List<TransactionDto> listTransactionsByCategory(UUID categoryId);

    /**
     * Get transaction by ID.
     *
     * @param id transaction UUID
     * @return transaction details with category information
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found
     */
    TransactionDto getTransactionById(UUID id);

    /**
     * Update existing transaction with optimistic locking.
     * If categoryId is provided in update, validates it exists.
     *
     * @param id transaction UUID
     * @param dto update data including version
     * @return updated transaction
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if transaction or category not found
     * @throws dev.juanvaldivia.moneytrak.exception.ConflictException if version mismatch
     */
    TransactionDto updateTransaction(UUID id, TransactionUpdateDto dto);

    /**
     * Delete transaction by ID.
     *
     * @param id transaction UUID
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found
     */
    void deleteTransaction(UUID id);

    /**
     * Calculate total amount for all EXPENSE transactions.
     *
     * @return sum of all expense amounts
     */
    java.math.BigDecimal calculateExpenseTotal();

    /**
     * Calculate total amount for all INCOME transactions.
     *
     * @return sum of all income amounts
     */
    java.math.BigDecimal calculateIncomeTotal();

    /**
     * List transactions filtered by stability.
     *
     * @param stability transaction stability (FIXED or VARIABLE)
     * @return list of transactions with specified stability
     */
    List<TransactionDto> listTransactionsByStability(TransactionStability stability);
}
