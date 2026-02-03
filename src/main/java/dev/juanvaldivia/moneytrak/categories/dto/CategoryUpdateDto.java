package dev.juanvaldivia.moneytrak.categories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing category.
 * Used in PUT /v1/categories/{id} requests.
 *
 * <p>Includes version field for optimistic locking to prevent concurrent update conflicts.
 *
 * @param name new category name (required, max 100 characters)
 * @param version current version for optimistic locking
 */
public record CategoryUpdateDto(
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    String name,

    @NotNull(message = "Version is required for optimistic locking")
    Integer version
) {
}
