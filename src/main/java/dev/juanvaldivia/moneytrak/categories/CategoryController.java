package dev.juanvaldivia.moneytrak.categories;

import dev.juanvaldivia.moneytrak.categories.dto.CategoryCreationDto;
import dev.juanvaldivia.moneytrak.categories.dto.CategoryDto;
import dev.juanvaldivia.moneytrak.categories.dto.CategoryUpdateDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller for category management endpoints.
 * Provides CRUD operations for transaction categories at /v1/categories.
 */
@RestController
@RequestMapping("/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Create a new category.
     * POST /v1/categories
     *
     * @param dto category creation data
     * @return 201 Created with Location header and created category
     */
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryCreationDto dto) {
        CategoryDto created = categoryService.create(dto);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /**
     * Get all categories (predefined and custom).
     * GET /v1/categories
     *
     * @return 200 OK with list of all categories
     */
    @GetMapping
    public ResponseEntity<Page<CategoryDto>> getAllCategories(
        @PageableDefault(size = 50, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(categoryService.findAll(pageable));
    }

    /**
     * Get category by ID.
     * GET /v1/categories/{id}
     *
     * @param id category UUID
     * @return 200 OK with category or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable UUID id) {
        CategoryDto category = categoryService.findById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Update existing category.
     * PUT /v1/categories/{id}
     *
     * @param id category UUID
     * @param dto update data with new name and version
     * @return 200 OK with updated category or 404/409 on error
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
        @PathVariable UUID id,
        @Valid @RequestBody CategoryUpdateDto dto
    ) {
        CategoryDto updated = categoryService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete category.
     * DELETE /v1/categories/{id}
     *
     * Only allowed if category has no linked transactions.
     *
     * @param id category UUID
     * @return 204 No Content or 404/409 on error
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
