package dev.juanvaldivia.moneytrak.readings;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Reading entity.
 * All queries filter out soft-deleted readings (deleted=true).
 */
@Repository
public interface ReadingRepository extends JpaRepository<Reading, UUID> {

    /**
     * Find reading by ID excluding soft-deleted readings.
     * Uses JOIN FETCH to eagerly load account data and prevent N+1 queries.
     *
     * @param id reading UUID
     * @return optional reading with account eagerly loaded, empty if not found or deleted
     */
    @Query("SELECT r FROM Reading r JOIN FETCH r.account WHERE r.id = :id AND r.deleted = false")
    Optional<Reading> findByIdAndDeletedFalse(@Param("id") UUID id);

    /**
     * Count active (non-deleted) readings for a specific account.
     * Used for validation before account deletion.
     *
     * @param accountId account UUID
     * @return number of active readings
     */
    long countByAccountIdAndDeletedFalse(UUID accountId);

    /**
     * Find latest reading for each account.
     * Returns only the most recent non-deleted reading per account.
     * Uses subquery to find max reading date per account for optimal performance.
     * Uses JOIN FETCH to eagerly load account data and prevent N+1 queries.
     * Results are ordered by account name, then account id for deterministic ordering.
     *
     * @return list of latest readings with accounts eagerly loaded
     */
    @Query("SELECT r FROM Reading r " +
           "JOIN FETCH r.account a " +
           "WHERE r.deleted = false " +
           "AND r.readingDate = (" +
           "    SELECT MAX(r2.readingDate) " +
           "    FROM Reading r2 " +
           "    WHERE r2.account.id = r.account.id " +
           "    AND r2.deleted = false" +
           ") " +
           "ORDER BY a.name, a.id")
    List<Reading> findLatestReadingsWithAccounts();

    /**
     * Find all active readings for a specific account ordered by date descending.
     * Used for account reading history with pagination.
     *
     * @param accountId account UUID
     * @param pageable pagination parameters
     * @return page of readings ordered by date descending (most recent first)
     */
    Page<Reading> findByAccountIdAndDeletedFalse(UUID accountId, Pageable pageable);
}
