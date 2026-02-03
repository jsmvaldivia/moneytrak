package dev.juanvaldivia.moneytrak.categories;

import dev.juanvaldivia.moneytrak.categories.dto.CategoryCreationDto;
import dev.juanvaldivia.moneytrak.categories.dto.CategoryDto;
import dev.juanvaldivia.moneytrak.categories.dto.CategoryUpdateDto;
import dev.juanvaldivia.moneytrak.categories.exception.CategoryInUseException;
import dev.juanvaldivia.moneytrak.categories.mapper.CategoryMapper;
import dev.juanvaldivia.moneytrak.exception.ConflictException;
import dev.juanvaldivia.moneytrak.exception.NotFoundException;
import dev.juanvaldivia.moneytrak.transactions.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Local implementation of CategoryService.
 * Handles category CRUD operations with business logic and validation.
 */
@Service
@Transactional
public class LocalCategoryService implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final TransactionRepository transactionRepository;

    public LocalCategoryService(
        CategoryRepository categoryRepository,
        CategoryMapper categoryMapper,
        TransactionRepository transactionRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public CategoryDto create(CategoryCreationDto dto) {
        // Validate unique name (case-insensitive)
        if (categoryRepository.existsByNameIgnoreCase(dto.name())) {
            throw new ConflictException("Category with name '" + dto.name() + "' already exists");
        }

        // Create new custom category
        Category category = Category.createCustom(dto.name());
        Category saved = categoryRepository.save(category);

        return categoryMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll()
            .stream()
            .map(categoryMapper::toDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto findById(UUID id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        return categoryMapper.toDto(category);
    }

    @Override
    public CategoryDto update(UUID id, CategoryUpdateDto dto) {
        // Find existing category
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        // Check if name already exists (excluding current category)
        categoryRepository.findByNameIgnoreCase(dto.name())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new ConflictException("Category with name '" + dto.name() + "' already exists");
            });

        // Verify version for optimistic locking
        if (!category.getVersion().equals(dto.version())) {
            throw new ConflictException("Category has been modified by another user");
        }

        // Update category
        category.updateName(dto.name());
        Category updated = categoryRepository.save(category);

        return categoryMapper.toDto(updated);
    }

    @Override
    public void delete(UUID id) {
        // Find category
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        // Check if category has linked transactions
        long transactionCount = transactionRepository.countByCategoryId(id);

        if (transactionCount > 0) {
            throw new CategoryInUseException(
                "Cannot delete category '" + category.getName() +
                "' because it has " + transactionCount + " linked transaction(s)"
            );
        }

        // Delete category
        categoryRepository.delete(category);
    }
}
