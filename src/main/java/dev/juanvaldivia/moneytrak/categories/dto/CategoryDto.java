package dev.juanvaldivia.moneytrak.categories.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO for Category response.
 * Returned by GET endpoints for categories.
 *
 * @param id category unique identifier
 * @param name category name
 * @param isPredefined true if system-predefined, false if user-created
 * @param version optimistic locking version (required for PUT requests)
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
public record CategoryDto(
    UUID id,
    String name,
    Boolean isPredefined,
    Integer version,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {
}