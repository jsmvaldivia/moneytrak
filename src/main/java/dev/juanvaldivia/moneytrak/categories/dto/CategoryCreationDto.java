package dev.juanvaldivia.moneytrak.categories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new category.
 * Used in POST /v1/categories requests.
 *
 * @param name category name (required, max 100 characters)
 */
public record CategoryCreationDto(
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    String name
) {
}
