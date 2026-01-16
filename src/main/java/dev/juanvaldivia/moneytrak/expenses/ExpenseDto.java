package dev.juanvaldivia.moneytrak.expenses;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.UUID;

public record ExpenseDto(
        UUID eid, ZonedDateTime date, String description, BigDecimal amount, Currency currency) {}
