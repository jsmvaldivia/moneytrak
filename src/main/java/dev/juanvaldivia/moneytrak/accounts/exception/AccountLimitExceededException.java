package dev.juanvaldivia.moneytrak.accounts.exception;

import dev.juanvaldivia.moneytrak.exception.ConflictException;

/**
 * Exception thrown when attempting to create more than 1000 accounts.
 */
public class AccountLimitExceededException extends ConflictException {
    public AccountLimitExceededException(String message) {
        super(message);
    }
}
