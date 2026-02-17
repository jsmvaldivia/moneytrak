package dev.juanvaldivia.moneytrak.transactions;

import dev.juanvaldivia.moneytrak.categories.Category;
import dev.juanvaldivia.moneytrak.categories.CategoryRepository;
import dev.juanvaldivia.moneytrak.exception.ConflictException;
import dev.juanvaldivia.moneytrak.exception.NotFoundException;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionCreationDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionUpdateDto;
import dev.juanvaldivia.moneytrak.transactions.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LocalTransactionService covering business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class LocalTransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionMapper mapper;

    @InjectMocks
    private LocalTransactionService service;

    private Category othersCategory;
    private Category foodCategory;

    @BeforeEach
    void setUp() {
        othersCategory = new Category("Others", true, ZonedDateTime.now(), ZonedDateTime.now());
        foodCategory = new Category("Food & Drinks", true, ZonedDateTime.now(), ZonedDateTime.now());
    }

    // ======================== createTransaction ========================

    @Test
    void createTransaction_withNoCategoryId_shouldDefaultToOthers() {
        TransactionCreationDto dto = new TransactionCreationDto(
            "Lunch", new BigDecimal("10.00"), "EUR",
            ZonedDateTime.now().minusDays(1), TransactionType.EXPENSE, null, null
        );
        Transaction entity = Transaction.create("Lunch", new BigDecimal("10.00"), "EUR",
            ZonedDateTime.now().minusDays(1), TransactionType.EXPENSE, TransactionStability.VARIABLE, othersCategory);
        TransactionDto expectedDto = new TransactionDto(UUID.randomUUID(), "Lunch", new BigDecimal("10.00"),
            "EUR", ZonedDateTime.now(), TransactionType.EXPENSE, TransactionStability.VARIABLE,
            null, "Others", 0, ZonedDateTime.now(), ZonedDateTime.now());

        when(categoryRepository.findByNameIgnoreCase("Others")).thenReturn(Optional.of(othersCategory));
        when(mapper.toEntity(dto, othersCategory)).thenReturn(entity);
        when(transactionRepository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(expectedDto);

        TransactionDto result = service.createTransaction(dto);

        assertThat(result.categoryName()).isEqualTo("Others");
        verify(categoryRepository).findByNameIgnoreCase("Others");
    }

    @Test
    void createTransaction_withValidCategoryId_shouldUseSpecifiedCategory() {
        UUID categoryId = UUID.randomUUID();
        TransactionCreationDto dto = new TransactionCreationDto(
            "Groceries", new BigDecimal("50.00"), "EUR",
            ZonedDateTime.now().minusDays(1), TransactionType.EXPENSE, null, categoryId
        );
        Transaction entity = Transaction.create("Groceries", new BigDecimal("50.00"), "EUR",
            ZonedDateTime.now().minusDays(1), TransactionType.EXPENSE, TransactionStability.VARIABLE, foodCategory);
        TransactionDto expectedDto = new TransactionDto(UUID.randomUUID(), "Groceries", new BigDecimal("50.00"),
            "EUR", ZonedDateTime.now(), TransactionType.EXPENSE, TransactionStability.VARIABLE,
            categoryId, "Food & Drinks", 0, ZonedDateTime.now(), ZonedDateTime.now());

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(foodCategory));
        when(mapper.toEntity(dto, foodCategory)).thenReturn(entity);
        when(transactionRepository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(expectedDto);

        TransactionDto result = service.createTransaction(dto);

        assertThat(result.categoryName()).isEqualTo("Food & Drinks");
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).findByNameIgnoreCase(any());
    }

    @Test
    void createTransaction_withNonExistentCategoryId_shouldThrowNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        TransactionCreationDto dto = new TransactionCreationDto(
            "Groceries", new BigDecimal("50.00"), "EUR",
            ZonedDateTime.now().minusDays(1), TransactionType.EXPENSE, null, nonExistentId
        );

        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createTransaction(dto))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(nonExistentId.toString());
    }

    @Test
    void createTransaction_whenOthersNotFound_shouldThrowIllegalState() {
        TransactionCreationDto dto = new TransactionCreationDto(
            "Lunch", new BigDecimal("10.00"), "EUR",
            ZonedDateTime.now().minusDays(1), TransactionType.EXPENSE, null, null
        );

        when(categoryRepository.findByNameIgnoreCase("Others")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createTransaction(dto))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Others");
    }

    // ======================== listTransactions ========================

    @Test
    void listTransactions_withNoCategoryIdFilter_shouldNotValidateCategory() {
        Pageable pageable = PageRequest.of(0, 20);
        when(transactionRepository.findByFilters(null, null, pageable))
            .thenReturn(Page.empty());

        service.listTransactions(null, null, pageable);

        verify(categoryRepository, never()).existsById(any());
    }

    @Test
    void listTransactions_withNonExistentCategoryId_shouldThrowNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        when(categoryRepository.existsById(nonExistentId)).thenReturn(false);

        assertThatThrownBy(() -> service.listTransactions(nonExistentId, null, pageable))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(nonExistentId.toString());
    }

    @Test
    void listTransactions_withValidFilters_shouldDelegateToRepository() {
        UUID categoryId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Transaction> emptyPage = new PageImpl<>(List.of());

        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(transactionRepository.findByFilters(categoryId, TransactionStability.FIXED, pageable))
            .thenReturn(emptyPage);

        Page<TransactionDto> result = service.listTransactions(categoryId, TransactionStability.FIXED, pageable);

        assertThat(result.getTotalElements()).isZero();
        verify(transactionRepository).findByFilters(categoryId, TransactionStability.FIXED, pageable);
    }

    // ======================== updateTransaction ========================

    @Test
    void updateTransaction_withVersionMismatch_shouldThrowConflict() {
        UUID txId = UUID.randomUUID();
        Transaction existing = Transaction.create("Old", new BigDecimal("10.00"), "EUR",
            ZonedDateTime.now().minusDays(1), TransactionType.EXPENSE, TransactionStability.VARIABLE, othersCategory);
        TransactionUpdateDto dto = new TransactionUpdateDto(
            null, null, null, null, null, null, null, 999  // wrong version
        );

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateTransaction(txId, dto))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Version mismatch");
    }

    @Test
    void updateTransaction_withNonExistentId_shouldThrowNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        TransactionUpdateDto dto = new TransactionUpdateDto(
            null, null, null, null, null, null, null, 0
        );

        when(transactionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTransaction(nonExistentId, dto))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(nonExistentId.toString());
    }

    // ======================== deleteTransaction ========================

    @Test
    void deleteTransaction_withNonExistentId_shouldThrowNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        when(transactionRepository.existsById(nonExistentId)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteTransaction(nonExistentId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining(nonExistentId.toString());
    }

    @Test
    void deleteTransaction_withExistingId_shouldDelegate() {
        UUID txId = UUID.randomUUID();

        when(transactionRepository.existsById(txId)).thenReturn(true);

        service.deleteTransaction(txId);

        verify(transactionRepository).deleteById(txId);
    }
}
