package dev.juanvaldivia.moneytrak.expenses.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
