package dev.juanvaldivia.moneytrak.transactions.dto;

import dev.juanvaldivia.moneytrak.validation.Currency;
import dev.juanvaldivia.moneytrak.transactions.TransactionStability;
import dev.juanvaldivia.moneytrak.transactions.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO for creating a new transaction.
 * CategoryId is optional (defaults to "Others" if not specified).
 * TransactionType defaults to EXPENSE if not specified.
 * TransactionStability defaults to VARIABLE if not specified.
 *
 * @param description transaction description (required, max 500 chars)
 * @param amount positive amount (required)
 * @param currency ISO 4217 currency code (required)
 * @param date transaction date (required, not in future)
 * @param type EXPENSE or INCOME (optional, defaults to EXPENSE)
 * @param stability FIXED or VARIABLE (optional, defaults to VARIABLE)
 * @param categoryId category UUID (optional, defaults to "Others")
 */
public record TransactionCreationDto(
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Digits(integer = 9, fraction = 2, message = "Amount must have at most 2 decimal places and not exceed 999,999,999.99")
    BigDecimal amount,

    @NotNull(message = "Currency is required")
    @Currency
    String currency,

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date must not be in the future")
    ZonedDateTime date,

    @NotNull(message = "Type is required")
    TransactionType type,

    TransactionStability stability,

    UUID categoryId
) {
}
