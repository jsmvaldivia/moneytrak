package dev.juanvaldivia.moneytrak.exception;

public record FieldErrorDto(
    String field,
    String message
) {}
