package dev.juanvaldivia.moneytrak.readings;

import dev.juanvaldivia.moneytrak.accounts.Account;
import dev.juanvaldivia.moneytrak.accounts.AccountRepository;
import dev.juanvaldivia.moneytrak.exception.ConflictException;
import dev.juanvaldivia.moneytrak.exception.NotFoundException;
import dev.juanvaldivia.moneytrak.readings.dto.ReadingCreationDto;
import dev.juanvaldivia.moneytrak.readings.dto.ReadingDto;
import dev.juanvaldivia.moneytrak.readings.dto.ReadingUpdateDto;
import dev.juanvaldivia.moneytrak.readings.mapper.ReadingMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Local implementation of ReadingService.
 * Handles reading CRUD with soft deletion and optimized latest reading queries.
 */
@Service
@Transactional
public class LocalReadingService implements ReadingService {

    private final ReadingRepository readingRepository;
    private final AccountRepository accountRepository;
    private final ReadingMapper mapper;
    private final EntityManager entityManager;

    public LocalReadingService(
        ReadingRepository readingRepository,
        AccountRepository accountRepository,
        ReadingMapper mapper,
        EntityManager entityManager
    ) {
        this.readingRepository = readingRepository;
        this.accountRepository = accountRepository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public ReadingDto createReading(ReadingCreationDto dto) {
        // Validate account exists
        Account account = accountRepository.findById(dto.accountId())
            .orElseThrow(() -> new NotFoundException("Account not found with id: " + dto.accountId()));

        Reading entity = mapper.toEntity(dto, account);
        Reading saved = readingRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReadingDto getReadingById(UUID id) {
        Reading reading = readingRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Reading not found with id: " + id));
        return mapper.toDto(reading);
    }

    @Override
    public ReadingDto updateReading(UUID id, ReadingUpdateDto dto) {
        Reading existing = readingRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Reading not found with id: " + id));

        if (!existing.version().equals(dto.version())) {
            throw new ConflictException("Version mismatch: reading has been modified");
        }

        try {
            mapper.updateEntity(existing, dto);
            Reading saved = readingRepository.save(existing);
            entityManager.flush(); // Force version increment
            return mapper.toDto(saved);
        } catch (OptimisticLockException e) {
            throw new ConflictException("Version mismatch: reading has been modified");
        }
    }

    @Override
    public void deleteReading(UUID id) {
        Reading reading = readingRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Reading not found with id: " + id));

        reading.markDeleted();
        readingRepository.save(reading);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingDto> getLatestReadings() {
        return readingRepository.findLatestReadingsWithAccounts().stream()
            .map(mapper::toDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReadingDto> getAccountReadingHistory(UUID accountId, Pageable pageable) {
        // Validate account exists
        if (!accountRepository.existsById(accountId)) {
            throw new NotFoundException("Account not found with id: " + accountId);
        }

        return readingRepository.findByAccountIdAndDeletedFalse(accountId, pageable)
            .map(mapper::toDto);
    }
}
