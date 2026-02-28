package dev.juanvaldivia.moneytrak.readings;

import dev.juanvaldivia.moneytrak.readings.dto.ReadingCreationDto;
import dev.juanvaldivia.moneytrak.readings.dto.ReadingDto;
import dev.juanvaldivia.moneytrak.readings.dto.ReadingUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for reading management operations.
 * Handles reading CRUD with soft deletion and latest reading retrieval.
 */
public interface ReadingService {

    /**
     * Create a new reading.
     *
     * @param dto reading creation data
     * @return created reading with account details
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if account not found
     */
    ReadingDto createReading(ReadingCreationDto dto);

    /**
     * Get reading by ID.
     * Soft-deleted readings return 404.
     *
     * @param id reading UUID
     * @return reading details with account information
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found or soft-deleted
     */
    ReadingDto getReadingById(UUID id);

    /**
     * Update existing reading with optimistic locking.
     * AccountId is immutable and cannot be changed.
     * Soft-deleted readings cannot be updated.
     *
     * @param id reading UUID
     * @param dto update data including version
     * @return updated reading
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if reading not found or soft-deleted
     * @throws dev.juanvaldivia.moneytrak.exception.ConflictException if version mismatch
     */
    ReadingDto updateReading(UUID id, ReadingUpdateDto dto);

    /**
     * Soft delete reading by ID.
     * Sets deleted=true to preserve historical data.
     *
     * @param id reading UUID
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found or already soft-deleted
     */
    void deleteReading(UUID id);

    /**
     * Get latest reading for each account.
     * Returns only the most recent non-deleted reading per account.
     * Excludes accounts with no readings or only soft-deleted readings.
     * Results ordered by account name (then id for deterministic ordering).
     *
     * @return list of latest readings with account details
     */
    List<ReadingDto> getLatestReadings();

    /**
     * Get all active readings for a specific account ordered by date descending.
     * Excludes soft-deleted readings. Results are paginated.
     *
     * @param accountId account UUID
     * @param pageable pagination parameters
     * @return page of readings ordered by date descending
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if account not found
     */
    Page<ReadingDto> getAccountReadingHistory(UUID accountId, Pageable pageable);
}
