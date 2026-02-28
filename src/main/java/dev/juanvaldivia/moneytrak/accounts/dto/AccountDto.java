package dev.juanvaldivia.moneytrak.accounts.dto;

import dev.juanvaldivia.moneytrak.accounts.AccountType;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO for Account response.
 *
 * @param id account unique identifier
 * @param name account name
 * @param type account type
 * @param currency ISO 4217 currency code
 * @param version optimistic locking version
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
public record AccountDto(
    UUID id,
    String name,
    AccountType type,
    String currency,
    Integer version,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {
}
