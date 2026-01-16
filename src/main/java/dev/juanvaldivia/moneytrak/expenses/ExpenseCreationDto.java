package dev.juanvaldivia.moneytrak.expenses;

import jakarta.validation.constraints.*;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;

record ExpenseCreationDto(
    @NotNull(message = "Date is required") @PastOrPresent(message = "Date cannot be in the future")
    ZonedDateTime date,

    @Size(min = 1, max = 500, message = "Description must be between 1 and 500 characters")
    String description,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 4, message = "Invalid amount format")
    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal amount,

    @JsonSerialize(using = ToStringSerializer.class) @NotNull(message = "Currency is required")
    Currency currency) {}
