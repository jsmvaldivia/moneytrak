package dev.juanvaldivia.moneytrak.expenses.mapper;

import dev.juanvaldivia.moneytrak.expenses.Expense;
import dev.juanvaldivia.moneytrak.expenses.dto.*;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public Expense toEntity(ExpenseCreationDto dto) {
        return Expense.create(
            dto.description(),
            dto.amount(),
            dto.currency(),
            dto.date()
        );
    }

    public void updateEntity(Expense existing, ExpenseUpdateDto dto) {
        existing.update(
            dto.description() != null ? dto.description() : existing.description(),
            dto.amount() != null ? dto.amount() : existing.amount(),
            dto.currency() != null ? dto.currency() : existing.currency(),
            dto.date() != null ? dto.date() : existing.date()
        );
    }

    public ExpenseDto toDto(Expense entity) {
        return new ExpenseDto(
            entity.id(),
            entity.description(),
            entity.amount(),
            entity.currency(),
            entity.date(),
            entity.version(),
            entity.createdAt(),
            entity.updatedAt()
        );
    }
}
