package dev.juanvaldivia.moneytrak.accounts.exception;

import dev.juanvaldivia.moneytrak.exception.ConflictException;

/**
 * Exception thrown when attempting to delete an account that has active readings.
 */
public class AccountInUseException extends ConflictException {
    public AccountInUseException(String message) {
        super(message);
    }
}
