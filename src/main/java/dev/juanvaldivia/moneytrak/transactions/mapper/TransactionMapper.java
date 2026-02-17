package dev.juanvaldivia.moneytrak.transactions.mapper;

import dev.juanvaldivia.moneytrak.categories.Category;
import dev.juanvaldivia.moneytrak.transactions.Transaction;
import dev.juanvaldivia.moneytrak.transactions.TransactionStability;
import dev.juanvaldivia.moneytrak.transactions.TransactionType;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionCreationDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionUpdateDto;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Transaction entity and DTOs.
 * Handles category relationship mapping.
 */
@Component
public class TransactionMapper {

    /**
     * Convert TransactionCreationDto to Transaction entity.
     * Category must be looked up separately before calling this method.
     *
     * @param dto creation DTO
     * @param category resolved category entity
     * @return new Transaction entity
     */
    public Transaction toEntity(TransactionCreationDto dto, Category category) {
        return Transaction.create(
            dto.description(),
            dto.amount(),
            dto.currency(),
            dto.date(),
            dto.type(),
            dto.stability() != null ? dto.stability() : TransactionStability.VARIABLE,
            category
        );
    }

    /**
     * Update existing Transaction entity from TransactionUpdateDto.
     * Handles partial updates (null fields are ignored).
     * Category must be looked up separately if categoryId is provided.
     *
     * @param existing existing transaction entity
     * @param dto update DTO
     * @param newCategory new category entity (only if dto.categoryId() is provided)
     */
    public void updateEntity(Transaction existing, TransactionUpdateDto dto, Category newCategory) {
        existing.update(
            dto.description() != null ? dto.description() : existing.description(),
            dto.amount() != null ? dto.amount() : existing.amount(),
            dto.currency() != null ? dto.currency() : existing.currency(),
            dto.date() != null ? dto.date() : existing.date(),
            dto.type() != null ? dto.type() : existing.type(),
            dto.stability() != null ? dto.stability() : existing.stability(),
            newCategory != null ? newCategory : existing.category()
        );
    }

    /**
     * Convert Transaction entity to TransactionDto for API response.
     * Includes category information (id and name).
     *
     * @param entity transaction entity
     * @return transaction DTO with category details
     */
    public TransactionDto toDto(Transaction entity) {
        Category category = entity.category();
        return new TransactionDto(
            entity.id(),
            entity.description(),
            entity.amount(),
            entity.currency(),
            entity.date(),
            entity.type(),
            entity.stability(),
            category != null ? category.getId() : null,
            category != null ? category.getName() : null,
            entity.version(),
            entity.createdAt(),
            entity.updatedAt()
        );
    }
}
