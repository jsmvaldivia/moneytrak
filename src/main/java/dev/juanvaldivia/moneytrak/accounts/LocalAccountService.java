package dev.juanvaldivia.moneytrak.accounts;

import dev.juanvaldivia.moneytrak.accounts.dto.AccountCreationDto;
import dev.juanvaldivia.moneytrak.accounts.dto.AccountDto;
import dev.juanvaldivia.moneytrak.accounts.dto.AccountUpdateDto;
import dev.juanvaldivia.moneytrak.accounts.exception.AccountInUseException;
import dev.juanvaldivia.moneytrak.accounts.exception.AccountLimitExceededException;
import dev.juanvaldivia.moneytrak.accounts.mapper.AccountMapper;
import dev.juanvaldivia.moneytrak.exception.ConflictException;
import dev.juanvaldivia.moneytrak.exception.NotFoundException;
import dev.juanvaldivia.moneytrak.readings.ReadingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Local implementation of AccountService.
 * Handles account CRUD with 1000 account limit enforcement and reading validation.
 */
@Service
@Transactional
public class LocalAccountService implements AccountService {

    private static final int ACCOUNT_LIMIT = 1000;

    private final AccountRepository accountRepository;
    private final ReadingRepository readingRepository;
    private final AccountMapper mapper;
    private final EntityManager entityManager;

    public LocalAccountService(
        AccountRepository accountRepository,
        ReadingRepository readingRepository,
        AccountMapper mapper,
        EntityManager entityManager
    ) {
        this.accountRepository = accountRepository;
        this.readingRepository = readingRepository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public AccountDto createAccount(AccountCreationDto dto) {
        // Enforce 1000 account limit
        if (accountRepository.count() >= ACCOUNT_LIMIT) {
            throw new AccountLimitExceededException("Account limit of " + ACCOUNT_LIMIT + " reached");
        }

        Account entity = mapper.toEntity(dto);
        Account saved = accountRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> listAccounts() {
        return accountRepository.findAllOrderedByName().stream()
            .map(mapper::toDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccountById(UUID id) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Account not found with id: " + id));
        return mapper.toDto(account);
    }

    @Override
    public AccountDto updateAccount(UUID id, AccountUpdateDto dto) {
        Account existing = accountRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Account not found with id: " + id));

        if (!existing.version().equals(dto.version())) {
            throw new ConflictException("Version mismatch: account has been modified");
        }

        try {
            mapper.updateEntity(existing, dto);
            Account saved = accountRepository.save(existing);
            entityManager.flush(); // Force version increment
            return mapper.toDto(saved);
        } catch (OptimisticLockException e) {
            throw new ConflictException("Version mismatch: account has been modified");
        }
    }

    @Override
    public void deleteAccount(UUID id) {
        if (!accountRepository.existsById(id)) {
            throw new NotFoundException("Account not found with id: " + id);
        }

        // Validate account has no active readings
        long activeReadingCount = readingRepository.countByAccountIdAndDeletedFalse(id);
        if (activeReadingCount > 0) {
            throw new AccountInUseException("Cannot delete account with " + activeReadingCount + " active reading(s)");
        }

        accountRepository.deleteById(id);
    }
}
