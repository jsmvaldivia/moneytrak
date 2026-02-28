package dev.juanvaldivia.moneytrak.accounts.dto;

import dev.juanvaldivia.moneytrak.accounts.AccountType;
import dev.juanvaldivia.moneytrak.validation.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new account.
 *
 * @param name account name (required, max 100 chars)
 * @param type account type (required)
 * @param currency ISO 4217 currency code (required)
 */
public record AccountCreationDto(
    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    String name,

    @NotNull(message = "Account type is required")
    AccountType type,

    @NotNull(message = "Currency is required")
    @Currency
    String currency
) {
}
