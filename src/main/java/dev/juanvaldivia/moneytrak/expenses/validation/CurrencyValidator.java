package dev.juanvaldivia.moneytrak.expenses.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Currency;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;  // @NotNull handles null check
        }

        try {
            Currency.getInstance(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;  // Invalid ISO 4217 code
        }
    }
}
