package dev.juanvaldivia.moneytrak.categories;

import dev.juanvaldivia.moneytrak.categories.dto.CategoryCreationDto;
import dev.juanvaldivia.moneytrak.categories.dto.CategoryDto;
import dev.juanvaldivia.moneytrak.categories.dto.CategoryUpdateDto;
import dev.juanvaldivia.moneytrak.categories.exception.CategoryInUseException;
import dev.juanvaldivia.moneytrak.categories.mapper.CategoryMapper;
import dev.juanvaldivia.moneytrak.exception.ConflictException;
import dev.juanvaldivia.moneytrak.exception.NotFoundException;
import dev.juanvaldivia.moneytrak.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LocalCategoryService covering business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class LocalCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private LocalCategoryService service;

    private Category existingCategory;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        existingCategory = new Category("Food & Drinks", true, ZonedDateTime.now(), ZonedDateTime.now());
    }

    // ======================== create ========================

    @Test
    void create_withDuplicateName_shouldThrowConflict() {
        CategoryCreationDto dto = new CategoryCreationDto("Food & Drinks");
        when(categoryRepository.existsByNameIgnoreCase("Food & Drinks")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Food & Drinks");
    }

    @Test
    void create_withUniqueName_shouldSaveAndReturnDto() {
        CategoryCreationDto dto = new CategoryCreationDto("Medical");
        Category newCategory = Category.createCustom("Medical");
        CategoryDto expectedDto = new CategoryDto(UUID.randomUUID(), "Medical", false, 0,
            ZonedDateTime.now(), ZonedDateTime.now());

        when(categoryRepository.existsByNameIgnoreCase("Medical")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);
        when(categoryMapper.toDto(newCategory)).thenReturn(expectedDto);

        CategoryDto result = service.create(dto);

        assertThat(result.name()).isEqualTo("Medical");
    }

    // ======================== update ========================

    @Test
    void update_renamingOthers_shouldThrowConflict() {
        Category others = new Category("Others", true, ZonedDateTime.now(), ZonedDateTime.now());
        CategoryUpdateDto dto = new CategoryUpdateDto("Miscellaneous", 0);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(others));

        assertThatThrownBy(() -> service.update(categoryId, dto))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Others");
    }

    @Test
    void update_withVersionMismatch_shouldThrowConflict() {
        CategoryUpdateDto dto = new CategoryUpdateDto("New Name", 999);  // wrong version

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.findByNameIgnoreCase("New Name")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(categoryId, dto))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("modified");
    }

    @Test
    void update_withDuplicateNameOnDifferentCategory_shouldThrowConflict() {
        Category otherCategory = new Category("Bank", true, ZonedDateTime.now(), ZonedDateTime.now());
        UUID otherCategoryId = UUID.randomUUID();
        // Need to set id on the other category via reflection or use a different category
        CategoryUpdateDto dto = new CategoryUpdateDto("Bank", 0);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.findByNameIgnoreCase("Bank")).thenReturn(Optional.of(otherCategory));

        // The filter checks !existing.getId().equals(id) - otherCategory has a different (null) id
        // In this test, otherCategory.id is null (not set) while categoryId is a real UUID
        // So the filter will not match (null != categoryId), thus conflict is thrown
        assertThatThrownBy(() -> service.update(categoryId, dto))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Bank");
    }

    @Test
    void update_withNonExistentId_shouldThrowNotFound() {
        CategoryUpdateDto dto = new CategoryUpdateDto("New Name", 0);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(categoryId, dto))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(categoryId.toString());
    }

    // ======================== delete ========================

    @Test
    void delete_withLinkedTransactions_shouldThrowCategoryInUse() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(transactionRepository.countByCategoryId(categoryId)).thenReturn(3L);

        assertThatThrownBy(() -> service.delete(categoryId))
            .isInstanceOf(CategoryInUseException.class)
            .hasMessageContaining("3");
    }

    @Test
    void delete_withNoLinkedTransactions_shouldDelete() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(transactionRepository.countByCategoryId(categoryId)).thenReturn(0L);

        service.delete(categoryId);

        verify(categoryRepository).delete(existingCategory);
    }

    @Test
    void delete_withNonExistentId_shouldThrowNotFound() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(categoryId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(categoryId.toString());
    }

    // ======================== findAll ========================

    @Test
    void findAll_shouldReturnPagedResults() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Category> categoryPage = new PageImpl<>(List.of(existingCategory));

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toDto(any(Category.class)))
            .thenReturn(new CategoryDto(UUID.randomUUID(), "Food & Drinks", true, 0,
                ZonedDateTime.now(), ZonedDateTime.now()));

        Page<CategoryDto> result = service.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
