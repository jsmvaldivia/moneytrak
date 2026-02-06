package dev.juanvaldivia.moneytrak.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CurrencyValidator implements ConstraintValidator<Currency, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;  // @NotNull handles null check
        }

        try {
            java.util.Currency.getInstance(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;  // Invalid ISO 4217 code
        }
    }
}
