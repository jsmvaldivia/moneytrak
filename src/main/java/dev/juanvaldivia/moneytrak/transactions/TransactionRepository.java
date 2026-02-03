package dev.juanvaldivia.moneytrak.transactions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for Transaction entity.
 * Provides transaction queries with category filtering support.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Find all transactions ordered by date descending (most recent first).
     * Used for listing transactions in reverse chronological order.
     *
     * @return list of all transactions
     */
    @Query("SELECT t FROM Transaction t ORDER BY t.date DESC")
    List<Transaction> findAllOrderByDateDesc();

    /**
     * Find transactions by category ID, ordered by date descending.
     * Used for category-based filtering.
     *
     * @param categoryId category UUID
     * @return list of transactions in the specified category
     */
    @Query("SELECT t FROM Transaction t WHERE t.category.id = :categoryId ORDER BY t.date DESC")
    List<Transaction> findByCategoryIdOrderByDateDesc(@Param("categoryId") UUID categoryId);

    /**
     * Count transactions linked to a specific category.
     * Used for validation before category deletion.
     *
     * @param categoryId category UUID
     * @return number of transactions in the category
     */
    long countByCategoryId(UUID categoryId);

    /**
     * Calculate sum of amounts by transaction type.
     * Used for expense and income totals.
     *
     * @param type transaction type (EXPENSE or INCOME)
     * @return sum of amounts, or 0 if no transactions
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionType = :type")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);

    /**
     * Find transactions by stability, ordered by date descending.
     * Used for filtering FIXED vs VARIABLE transactions.
     *
     * @param stability transaction stability (FIXED or VARIABLE)
     * @return list of transactions with specified stability
     */
    @Query("SELECT t FROM Transaction t WHERE t.transactionStability = :stability ORDER BY t.date DESC")
    List<Transaction> findByTransactionStabilityOrderByDateDesc(@Param("stability") TransactionStability stability);
}
