package dev.juanvaldivia.moneytrak.readings.dto;

import dev.juanvaldivia.moneytrak.accounts.AccountType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO for Reading response.
 * Includes embedded account information for API responses.
 *
 * @param id reading unique identifier
 * @param accountId linked account ID
 * @param accountName linked account name
 * @param accountType linked account type
 * @param accountCurrency linked account currency
 * @param amount balance amount (supports negative values)
 * @param readingDate date and time of the reading
 * @param version optimistic locking version
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
public record ReadingDto(
    UUID id,
    UUID accountId,
    String accountName,
    AccountType accountType,
    String accountCurrency,
    BigDecimal amount,
    ZonedDateTime readingDate,
    Integer version,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {
}
