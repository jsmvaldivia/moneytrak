package dev.juanvaldivia.moneytrak.accounts.mapper;

import dev.juanvaldivia.moneytrak.accounts.Account;
import dev.juanvaldivia.moneytrak.accounts.dto.AccountCreationDto;
import dev.juanvaldivia.moneytrak.accounts.dto.AccountDto;
import dev.juanvaldivia.moneytrak.accounts.dto.AccountUpdateDto;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Account entity and DTOs.
 */
@Component
public class AccountMapper {

    /**
     * Convert AccountCreationDto to Account entity.
     *
     * @param dto creation DTO
     * @return new Account entity
     */
    public Account toEntity(AccountCreationDto dto) {
        return Account.create(dto.name(), dto.type(), dto.currency());
    }

    /**
     * Update existing Account entity from AccountUpdateDto.
     * Handles partial updates (null fields are ignored).
     *
     * @param existing existing account entity
     * @param dto update DTO
     */
    public void updateEntity(Account existing, AccountUpdateDto dto) {
        existing.update(
            dto.name() != null ? dto.name() : existing.name(),
            dto.type() != null ? dto.type() : existing.type(),
            dto.currency() != null ? dto.currency() : existing.currency()
        );
    }

    /**
     * Convert Account entity to AccountDto for API response.
     *
     * @param entity account entity
     * @return account DTO
     */
    public AccountDto toDto(Account entity) {
        return new AccountDto(
            entity.id(),
            entity.name(),
            entity.type(),
            entity.currency(),
            entity.version(),
            entity.createdAt(),
            entity.updatedAt()
        );
    }
}
