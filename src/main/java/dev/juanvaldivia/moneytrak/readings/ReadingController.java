package dev.juanvaldivia.moneytrak.readings;

import dev.juanvaldivia.moneytrak.readings.dto.ReadingCreationDto;
import dev.juanvaldivia.moneytrak.readings.dto.ReadingDto;
import dev.juanvaldivia.moneytrak.readings.dto.ReadingUpdateDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for reading management.
 * Provides CRUD endpoints for portfolio readings with soft deletion support.
 * All endpoints are versioned under /v1/readings.
 */
@Tag(name = "Readings", description = "Portfolio reading management endpoints")
@RestController
@RequestMapping("/v1/readings")
public class ReadingController {

    private final ReadingService service;

    public ReadingController(ReadingService service) {
        this.service = service;
    }

    /**
     * Create a new reading.
     * POST /v1/readings
     *
     * @param dto reading creation data
     * @return 201 Created with Location header and created reading
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if account not found (404)
     */
    @PostMapping
    public ResponseEntity<ReadingDto> createReading(@Valid @RequestBody ReadingCreationDto dto) {
        ReadingDto created = service.createReading(dto);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /**
     * Get reading by ID.
     * GET /v1/readings/{id}
     *
     * @param id reading UUID
     * @return 200 OK with reading details including account info
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found or soft-deleted (404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReadingDto> getReading(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getReadingById(id));
    }

    /**
     * Update existing reading with optimistic locking.
     * PUT /v1/readings/{id}
     *
     * Partial updates supported - null fields preserve existing values.
     * AccountId is immutable and cannot be changed.
     *
     * @param id reading UUID
     * @param dto update data with version for optimistic locking
     * @return 200 OK with updated reading
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found or soft-deleted (404)
     * @throws dev.juanvaldivia.moneytrak.exception.ConflictException if version mismatch (409)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReadingDto> updateReading(
        @PathVariable UUID id,
        @Valid @RequestBody ReadingUpdateDto dto
    ) {
        return ResponseEntity.ok(service.updateReading(id, dto));
    }

    /**
     * Soft delete reading by ID.
     * DELETE /v1/readings/{id}
     *
     * Sets deleted=true to preserve historical data.
     *
     * @param id reading UUID
     * @return 204 No Content
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found or already soft-deleted (404)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReading(@PathVariable UUID id) {
        service.deleteReading(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get latest reading for each account.
     * GET /v1/readings/latest
     *
     * Returns only the most recent non-deleted reading per account.
     * Excludes accounts with no readings or only soft-deleted readings.
     * Results ordered by account name (then id for deterministic ordering).
     *
     * @return 200 OK with list of latest readings
     */
    @GetMapping("/latest")
    public ResponseEntity<List<ReadingDto>> getLatestReadings() {
        return ResponseEntity.ok(service.getLatestReadings());
    }
}
