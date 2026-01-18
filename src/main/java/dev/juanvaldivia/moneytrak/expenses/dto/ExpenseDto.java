package dev.juanvaldivia.moneytrak.expenses.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ExpenseDto(
    UUID id,
    String description,
    BigDecimal amount,
    String currency,
    ZonedDateTime date,
    Integer version,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {}
