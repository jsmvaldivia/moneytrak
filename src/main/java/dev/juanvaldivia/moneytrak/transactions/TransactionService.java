package dev.juanvaldivia.moneytrak.transactions;

import dev.juanvaldivia.moneytrak.transactions.dto.TransactionCreationDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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
     * List transactions with optional composable filters.
     * Both categoryId and stability are independently optional and can be combined.
     * If categoryId is provided, validates the category exists first.
     *
     * @param categoryId optional category UUID filter
     * @param stability optional stability filter
     * @param pageable pagination and sort parameters
     * @return page of matching transactions
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if categoryId provided but not found
     */
    Page<TransactionDto> listTransactions(UUID categoryId, TransactionStability stability, Pageable pageable);

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
    BigDecimal calculateExpenseTotal();

    /**
     * Calculate total amount for all INCOME transactions.
     *
     * @return sum of all income amounts
     */
    BigDecimal calculateIncomeTotal();
}
