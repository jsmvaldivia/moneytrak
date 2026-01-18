package dev.juanvaldivia.moneytrak.expenses;

import dev.juanvaldivia.moneytrak.expenses.dto.ExpenseCreationDto;
import dev.juanvaldivia.moneytrak.expenses.dto.ExpenseDto;
import dev.juanvaldivia.moneytrak.expenses.dto.ExpenseUpdateDto;
import dev.juanvaldivia.moneytrak.expenses.exception.ConflictException;
import dev.juanvaldivia.moneytrak.expenses.exception.NotFoundException;
import dev.juanvaldivia.moneytrak.expenses.mapper.ExpenseMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LocalExpenseService implements ExpenseService {

    private final ExpenseRepository repository;
    private final ExpenseMapper mapper;
    private final EntityManager entityManager;

    public LocalExpenseService(ExpenseRepository repository, ExpenseMapper mapper, EntityManager entityManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public ExpenseDto createExpense(ExpenseCreationDto dto) {
        Expense entity = mapper.toEntity(dto);
        Expense saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDto> listExpenses() {
        List<Expense> expenses = repository.findAllOrderByDateDesc();
        return expenses.stream()
            .map(mapper::toDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDto getExpenseById(UUID id) {
        Expense expense = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Expense not found with id: " + id));
        return mapper.toDto(expense);
    }

    @Override
    public ExpenseDto updateExpense(UUID id, ExpenseUpdateDto dto) {
        Expense existing = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Expense not found with id: " + id));

        if (!existing.version().equals(dto.version())) {
            throw new ConflictException("Version mismatch: expense has been modified");
        }

        try {
mapper.updateEntity(existing, dto);
            Expense saved = repository.save(existing);
            entityManager.flush();  // Force JPA to increment version
            return mapper.toDto(saved);
        } catch (OptimisticLockException e) {
            throw new ConflictException("Version mismatch: expense has been modified");
        }
    }

    @Override
    public void deleteExpense(UUID id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Expense not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
