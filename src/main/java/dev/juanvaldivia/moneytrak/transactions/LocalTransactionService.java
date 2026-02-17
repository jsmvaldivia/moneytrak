package dev.juanvaldivia.moneytrak.transactions;

import dev.juanvaldivia.moneytrak.categories.Category;
import dev.juanvaldivia.moneytrak.categories.CategoryRepository;
import dev.juanvaldivia.moneytrak.exception.ConflictException;
import dev.juanvaldivia.moneytrak.exception.NotFoundException;
import dev.juanvaldivia.moneytrak.transactions.dto.SummaryDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionCreationDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionUpdateDto;
import dev.juanvaldivia.moneytrak.transactions.mapper.TransactionMapper;
import jakarta.persistence.OptimisticLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Local implementation of TransactionService.
 * Handles transaction CRUD with category linking and default assignment.
 */
@Service
@Transactional
public class LocalTransactionService implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper mapper;

    public LocalTransactionService(
        TransactionRepository transactionRepository,
        CategoryRepository categoryRepository,
        TransactionMapper mapper
    ) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Override
    public TransactionDto createTransaction(TransactionCreationDto dto) {
        Category category = resolveCategoryForCreation(dto.categoryId());
        Transaction entity = mapper.toEntity(dto, category);
        Transaction saved = transactionRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDto> listTransactions(UUID categoryId, TransactionStability stability, Pageable pageable) {
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Category not found with id: " + categoryId);
        }
        return transactionRepository.findByFilters(categoryId, stability, pageable)
            .map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));
        return mapper.toDto(transaction);
    }

    @Override
    public TransactionDto updateTransaction(UUID id, TransactionUpdateDto dto) {
        Transaction existing = transactionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));

        if (!existing.version().equals(dto.version())) {
            throw new ConflictException("Version mismatch: transaction has been modified");
        }

        Category newCategory = null;
        if (dto.categoryId() != null) {
            newCategory = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + dto.categoryId()));
        }

        try {
            mapper.updateEntity(existing, dto, newCategory);
            Transaction saved = transactionRepository.save(existing);
            return mapper.toDto(saved);
        } catch (OptimisticLockException e) {
            throw new ConflictException("Version mismatch: transaction has been modified");
        }
    }

    @Override
    public void deleteTransaction(UUID id) {
        if (!transactionRepository.existsById(id)) {
            throw new NotFoundException("Transaction not found with id: " + id);
        }
        transactionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public SummaryDto calculateExpenseTotal() {
        return new SummaryDto(transactionRepository.sumAmountByType(TransactionType.EXPENSE));
    }

    @Override
    @Transactional(readOnly = true)
    public SummaryDto calculateIncomeTotal() {
        return new SummaryDto(transactionRepository.sumAmountByType(TransactionType.INCOME));
    }

    /**
     * Resolve category for transaction creation.
     * If categoryId is provided, validates and returns it.
     * If categoryId is null, returns the default "Others" category.
     *
     * @param categoryId optional category UUID
     * @return resolved category entity
     * @throws NotFoundException if categoryId provided but not found, or if "Others" default not found
     */
    private Category resolveCategoryForCreation(UUID categoryId) {
        if (categoryId != null) {
            return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
        } else {
            return categoryRepository.findByNameIgnoreCase("Others")
                .orElseThrow(() -> new IllegalStateException("Default category 'Others' not found"));
        }
    }
}
