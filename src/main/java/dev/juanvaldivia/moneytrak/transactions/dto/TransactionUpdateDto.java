package dev.juanvaldivia.moneytrak.transactions.dto;

import dev.juanvaldivia.moneytrak.validation.ValidCurrency;
import dev.juanvaldivia.moneytrak.transactions.TransactionStability;
import dev.juanvaldivia.moneytrak.transactions.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO for updating an existing transaction.
 * All fields except version are optional for partial updates.
 *
 * @param description new description (optional)
 * @param amount new amount (optional)
 * @param currency new currency (optional)
 * @param date new date (optional)
 * @param transactionType new type EXPENSE/INCOME (optional)
 * @param transactionStability new stability FIXED/VARIABLE (optional)
 * @param categoryId new category ID (optional)
 * @param version current version for optimistic locking (required)
 */
public record TransactionUpdateDto(
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,

    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Digits(integer = 9, fraction = 2, message = "Amount must have at most 2 decimal places")
    BigDecimal amount,

    @ValidCurrency
    String currency,

    @PastOrPresent(message = "Date must not be in the future")
    ZonedDateTime date,

    TransactionType transactionType,

    TransactionStability transactionStability,

    UUID categoryId,

    @NotNull(message = "Version is required for optimistic locking")
    Integer version
) {
}
