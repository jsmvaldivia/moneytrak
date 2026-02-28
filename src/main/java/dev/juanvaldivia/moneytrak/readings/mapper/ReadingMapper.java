package dev.juanvaldivia.moneytrak.readings.mapper;

import dev.juanvaldivia.moneytrak.accounts.Account;
import dev.juanvaldivia.moneytrak.readings.Reading;
import dev.juanvaldivia.moneytrak.readings.dto.ReadingCreationDto;
import dev.juanvaldivia.moneytrak.readings.dto.ReadingDto;
import dev.juanvaldivia.moneytrak.readings.dto.ReadingUpdateDto;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Reading entity and DTOs.
 * Handles account relationship mapping and accountId immutability.
 */
@Component
public class ReadingMapper {

    /**
     * Convert ReadingCreationDto to Reading entity.
     * Account must be looked up separately before calling this method.
     *
     * @param dto creation DTO
     * @param account resolved account entity
     * @return new Reading entity
     */
    public Reading toEntity(ReadingCreationDto dto, Account account) {
        return Reading.create(account, dto.amount(), dto.readingDate());
    }

    /**
     * Update existing Reading entity from ReadingUpdateDto.
     * Handles partial updates (null fields are ignored).
     * AccountId is immutable and cannot be changed.
     *
     * @param existing existing reading entity
     * @param dto update DTO
     */
    public void updateEntity(Reading existing, ReadingUpdateDto dto) {
        existing.update(
            dto.amount() != null ? dto.amount() : existing.amount(),
            dto.readingDate() != null ? dto.readingDate() : existing.readingDate()
        );
    }

    /**
     * Convert Reading entity to ReadingDto for API response.
     * Includes embedded account details (id, name, type, currency).
     *
     * @param entity reading entity
     * @return reading DTO with account details
     */
    public ReadingDto toDto(Reading entity) {
        Account account = entity.account();
        return new ReadingDto(
            entity.id(),
            account != null ? account.id() : null,
            account != null ? account.name() : null,
            account != null ? account.type() : null,
            account != null ? account.currency() : null,
            entity.amount(),
            entity.readingDate(),
            entity.version(),
            entity.createdAt(),
            entity.updatedAt()
        );
    }
}
