package dev.juanvaldivia.moneytrak.transactions;

import dev.juanvaldivia.moneytrak.categories.Category;
import dev.juanvaldivia.moneytrak.categories.CategoryRepository;
import dev.juanvaldivia.moneytrak.exception.ConflictException;
import dev.juanvaldivia.moneytrak.exception.NotFoundException;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionCreationDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionUpdateDto;
import dev.juanvaldivia.moneytrak.transactions.mapper.TransactionMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final EntityManager entityManager;

    public LocalTransactionService(
        TransactionRepository transactionRepository,
        CategoryRepository categoryRepository,
        TransactionMapper mapper,
        EntityManager entityManager
    ) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public TransactionDto createTransaction(TransactionCreationDto dto) {
        // Resolve category: use provided categoryId or default to "Others"
        Category category = resolveCategoryForCreation(dto.categoryId());

        // Create transaction entity
        Transaction entity = mapper.toEntity(dto, category);
        Transaction saved = transactionRepository.save(entity);

        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> listTransactions() {
        List<Transaction> transactions = transactionRepository.findAllOrderByDateDesc();
        return transactions.stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> listTransactionsByCategory(UUID categoryId) {
        // Validate category exists (return empty list if valid category with no transactions)
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Category not found with id: " + categoryId);
        }

        List<Transaction> transactions = transactionRepository.findByCategoryIdOrderByDateDesc(categoryId);
        return transactions.stream().map(mapper::toDto).toList();
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
        // Find existing transaction
        Transaction existing = transactionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));

        // Verify version for optimistic locking
        if (!existing.version().equals(dto.version())) {
            throw new ConflictException("Version mismatch: transaction has been modified");
        }

        // Resolve new category if provided
        Category newCategory = null;
        if (dto.categoryId() != null) {
            newCategory = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + dto.categoryId()));
        }

        try {
            // Update entity
            mapper.updateEntity(existing, dto, newCategory);
            Transaction saved = transactionRepository.save(existing);
            entityManager.flush(); // Force JPA to increment version

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
    public java.math.BigDecimal calculateExpenseTotal() {
        return transactionRepository.sumAmountByType(TransactionType.EXPENSE);
    }

    @Override
    @Transactional(readOnly = true)
    public java.math.BigDecimal calculateIncomeTotal() {
        return transactionRepository.sumAmountByType(TransactionType.INCOME);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> listTransactionsByStability(TransactionStability stability) {
        List<Transaction> transactions = transactionRepository.findByTransactionStabilityOrderByDateDesc(stability);
        return transactions.stream().map(mapper::toDto).toList();
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
            // Use provided category
            return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
        } else {
            // Default to "Others" category
            return categoryRepository.findByNameIgnoreCase("Others")
                .orElseThrow(() -> new IllegalStateException("Default category 'Others' not found"));
        }
    }
}
