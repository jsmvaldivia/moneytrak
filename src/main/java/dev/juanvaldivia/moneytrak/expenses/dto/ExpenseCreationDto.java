package dev.juanvaldivia.moneytrak.expenses.dto;

import dev.juanvaldivia.moneytrak.expenses.validation.ValidCurrency;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ExpenseCreationDto(
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Digits(integer = 9, fraction = 2, message = "Amount must have at most 2 decimal places and not exceed 999,999,999.99")
    BigDecimal amount,

    @NotNull(message = "Currency is required")
    @ValidCurrency  // Custom validator
    String currency,

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date must not be in the future")
    ZonedDateTime date
) {}
