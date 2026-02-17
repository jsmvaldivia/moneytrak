package dev.juanvaldivia.moneytrak.categories;

import dev.juanvaldivia.moneytrak.categories.dto.CategoryCreationDto;
import dev.juanvaldivia.moneytrak.categories.dto.CategoryDto;
import dev.juanvaldivia.moneytrak.categories.dto.CategoryUpdateDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for category management operations.
 * Defines the contract for CRUD operations on categories.
 */
public interface CategoryService {

    /**
     * Create a new category.
     *
     * @param dto category creation data
     * @return created category DTO
     * @throws dev.juanvaldivia.moneytrak.exception.ConflictException if category name already exists
     */
    CategoryDto create(CategoryCreationDto dto);

    /**
     * Find all categories (predefined and custom).
     *
     * @param pageable pagination and sort parameters
     * @return page of all categories
     */
    Page<CategoryDto> findAll(Pageable pageable);

    /**
     * Find category by ID.
     *
     * @param id category UUID
     * @return category DTO
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if category not found
     */
    CategoryDto findById(UUID id);

    /**
     * Update existing category.
     *
     * @param id category UUID
     * @param dto update data with new name and version
     * @return updated category DTO
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if category not found
     * @throws dev.juanvaldivia.moneytrak.exception.ConflictException if name already exists or version mismatch
     */
    CategoryDto update(UUID id, CategoryUpdateDto dto);

    /**
     * Delete category.
     * Only allowed if category has no linked transactions.
     *
     * @param id category UUID
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if category not found
     * @throws dev.juanvaldivia.moneytrak.categories.exception.CategoryInUseException if category has linked transactions
     */
    void delete(UUID id);
}
