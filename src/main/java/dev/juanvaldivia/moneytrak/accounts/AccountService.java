package dev.juanvaldivia.moneytrak.accounts;

import dev.juanvaldivia.moneytrak.accounts.dto.AccountCreationDto;
import dev.juanvaldivia.moneytrak.accounts.dto.AccountDto;
import dev.juanvaldivia.moneytrak.accounts.dto.AccountUpdateDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for account management operations.
 * Handles account CRUD with 1000 account limit enforcement.
 */
public interface AccountService {

    /**
     * Create a new account.
     *
     * @param dto account creation data
     * @return created account
     * @throws dev.juanvaldivia.moneytrak.accounts.exception.AccountLimitExceededException if 1000 accounts already exist
     */
    AccountDto createAccount(AccountCreationDto dto);

    /**
     * List all accounts ordered by name (then by id for deterministic ordering).
     *
     * @return list of all accounts in deterministic order
     */
    List<AccountDto> listAccounts();

    /**
     * Get account by ID.
     *
     * @param id account UUID
     * @return account details
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found
     */
    AccountDto getAccountById(UUID id);

    /**
     * Update existing account with optimistic locking.
     *
     * @param id account UUID
     * @param dto update data including version
     * @return updated account
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found
     * @throws dev.juanvaldivia.moneytrak.exception.ConflictException if version mismatch
     */
    AccountDto updateAccount(UUID id, AccountUpdateDto dto);

    /**
     * Delete account by ID.
     *
     * @param id account UUID
     * @throws dev.juanvaldivia.moneytrak.exception.NotFoundException if not found
     * @throws dev.juanvaldivia.moneytrak.accounts.exception.AccountInUseException if account has active readings
     */
    void deleteAccount(UUID id);
}
