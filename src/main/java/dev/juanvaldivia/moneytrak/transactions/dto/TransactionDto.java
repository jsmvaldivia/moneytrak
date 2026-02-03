package dev.juanvaldivia.moneytrak.transactions.dto;

import dev.juanvaldivia.moneytrak.transactions.TransactionStability;
import dev.juanvaldivia.moneytrak.transactions.TransactionType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO for Transaction response.
 * Includes category information for API responses.
 *
 * @param id transaction unique identifier
 * @param description transaction description
 * @param amount positive amount
 * @param currency ISO 4217 currency code
 * @param date transaction date
 * @param transactionType EXPENSE or INCOME
 * @param transactionStability FIXED or VARIABLE
 * @param categoryId linked category ID
 * @param categoryName linked category name
 * @param version optimistic locking version
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
public record TransactionDto(
    UUID id,
    String description,
    BigDecimal amount,
    String currency,
    ZonedDateTime date,
    TransactionType transactionType,
    TransactionStability transactionStability,
    UUID categoryId,
    String categoryName,
    Integer version,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {
}
