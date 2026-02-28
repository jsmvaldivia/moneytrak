package dev.juanvaldivia.moneytrak.readings.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO for creating a new reading.
 *
 * @param accountId account UUID (required, immutable after creation)
 * @param amount balance amount (required, supports negative values, max 8 decimals)
 * @param readingDate date and time of the reading (required, not in future)
 */
public record ReadingCreationDto(
    @NotNull(message = "Account ID is required")
    UUID accountId,

    @NotNull(message = "Amount is required")
    @Digits(integer = 7, fraction = 8, message = "Amount must have at most 8 decimal places and not exceed 9,999,999.99999999")
    BigDecimal amount,

    @NotNull(message = "Reading date is required")
    @PastOrPresent(message = "Reading date must not be in the future")
    ZonedDateTime readingDate
) {
}
