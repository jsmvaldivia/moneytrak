package dev.juanvaldivia.moneytrak.expenses.exception;

import dev.juanvaldivia.moneytrak.expenses.dto.ErrorResponseDto;
import dev.juanvaldivia.moneytrak.expenses.dto.FieldErrorDto;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for all expense-related REST endpoints.
 * Provides consistent error response format across all API endpoints.
 *
 * <p>Error responses follow the format: {status, error, message, details[]}
 */
@RestControllerAdvice(basePackages = "dev.juanvaldivia.moneytrak.expenses")
public class
GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldErrorDto> details = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            details.add(new FieldErrorDto(error.getField(), error.getDefaultMessage()))
        );

        ErrorResponseDto errorResponse = new ErrorResponseDto(
            HttpStatus.BAD_REQUEST.value(),
            "ValidationError",
            "Invalid expense data",
            details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFoundException(NotFoundException ex) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
            HttpStatus.NOT_FOUND.value(),
            "NotFound",
            ex.getMessage(),
            List.of()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler({OptimisticLockException.class, ConflictException.class})
    public ResponseEntity<ErrorResponseDto> handleConflictException(Exception ex) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
            HttpStatus.CONFLICT.value(),
            "Conflict",
            "Version mismatch: expense has been modified",
            List.of()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "InternalError",
            "An unexpected error occurred",
            List.of()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
