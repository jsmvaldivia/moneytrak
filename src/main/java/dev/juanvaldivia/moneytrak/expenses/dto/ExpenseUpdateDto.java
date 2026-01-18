package dev.juanvaldivia.moneytrak.expenses.dto;

import dev.juanvaldivia.moneytrak.expenses.validation.ValidCurrency;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ExpenseUpdateDto(
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,  // Optional for partial updates

    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Digits(integer = 9, fraction = 2, message = "Amount must have at most 2 decimal places")
    BigDecimal amount,  // Optional for partial updates

    @ValidCurrency
    String currency,  // Optional for partial updates

    @PastOrPresent(message = "Date must not be in the future")
    ZonedDateTime date,  // Optional for partial updates

    @NotNull(message = "Version is required for optimistic locking")
    Integer version  // Required for version check
) {}
