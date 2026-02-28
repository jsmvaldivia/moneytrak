package dev.juanvaldivia.moneytrak.readings.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * DTO for updating an existing reading.
 * All fields except version are optional for partial updates.
 * Note: accountId is immutable and cannot be changed.
 *
 * @param amount new balance amount (optional)
 * @param readingDate new reading date (optional)
 * @param version current version for optimistic locking (required)
 */
public record ReadingUpdateDto(
    @Digits(integer = 7, fraction = 8, message = "Amount must have at most 8 decimal places")
    BigDecimal amount,

    @PastOrPresent(message = "Reading date must not be in the future")
    ZonedDateTime readingDate,

    @NotNull(message = "Version is required for optimistic locking")
    Integer version
) {
}
