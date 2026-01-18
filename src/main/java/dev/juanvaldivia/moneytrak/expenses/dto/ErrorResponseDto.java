package dev.juanvaldivia.moneytrak.expenses.dto;

import java.util.List;

public record ErrorResponseDto(
    int status,
    String error,  // "ValidationError", "NotFound", "Conflict", "InternalError"
    String message,
    List<FieldErrorDto> details
) {}
