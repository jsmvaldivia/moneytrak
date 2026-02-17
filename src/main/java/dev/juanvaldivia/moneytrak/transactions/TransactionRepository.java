package dev.juanvaldivia.moneytrak.transactions;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Spring Data JPA repository for Transaction entity.
 * Provides composable filtering with pagination support.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Find transactions with optional category and stability filters, ordered by date descending.
     * Both filters are independently optional and can be combined.
     * Uses JOIN FETCH to avoid N+1 queries when accessing category data.
     *
     * @param categoryId optional category UUID filter (null = all categories)
     * @param stability optional stability filter (null = all stability values)
     * @param pageable pagination and sort parameters
     * @return page of matching transactions with categories eagerly loaded
     */
    @Query(value = "SELECT t FROM Transaction t JOIN FETCH t.category " +
            "WHERE (:categoryId IS NULL OR t.category.id = :categoryId) " +
            "AND (:stability IS NULL OR t.stability = :stability)",
            countQuery = "SELECT COUNT(t) FROM Transaction t " +
            "WHERE (:categoryId IS NULL OR t.category.id = :categoryId) " +
            "AND (:stability IS NULL OR t.stability = :stability)")
    Page<Transaction> findByFilters(
            @Param("categoryId") UUID categoryId,
            @Param("stability") TransactionStability stability,
            Pageable pageable);

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
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);
}
