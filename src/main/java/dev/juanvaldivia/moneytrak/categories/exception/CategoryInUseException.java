package dev.juanvaldivia.moneytrak.categories.exception;

/**
 * Exception thrown when attempting to delete a category that has linked transactions.
 * Results in HTTP 409 Conflict response.
 */
public class CategoryInUseException extends RuntimeException {

    /**
     * Constructs a new CategoryInUseException with the specified detail message.
     *
     * @param message the detail message
     */
    public CategoryInUseException(String message) {
        super(message);
    }
}
