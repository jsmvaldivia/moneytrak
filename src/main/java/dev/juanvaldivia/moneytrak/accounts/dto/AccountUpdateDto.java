package dev.juanvaldivia.moneytrak.accounts.dto;

import dev.juanvaldivia.moneytrak.accounts.AccountType;
import dev.juanvaldivia.moneytrak.validation.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing account.
 * All fields except version are optional for partial updates.
 *
 * @param name new account name (optional)
 * @param type new account type (optional)
 * @param currency new currency code (optional)
 * @param version current version for optimistic locking (required)
 */
public record AccountUpdateDto(
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    String name,

    AccountType type,

    @Currency
    String currency,

    @NotNull(message = "Version is required for optimistic locking")
    Integer version
) {
}
