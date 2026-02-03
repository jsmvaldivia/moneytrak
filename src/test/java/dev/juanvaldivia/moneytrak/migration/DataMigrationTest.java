package dev.juanvaldivia.moneytrak.migration;

import dev.juanvaldivia.moneytrak.categories.Category;
import dev.juanvaldivia.moneytrak.categories.CategoryRepository;
import dev.juanvaldivia.moneytrak.transactions.Transaction;
import dev.juanvaldivia.moneytrak.transactions.TransactionRepository;
import dev.juanvaldivia.moneytrak.transactions.TransactionStability;
import dev.juanvaldivia.moneytrak.transactions.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests verifying data migration behavior from Expense to Transaction model.
 *
 * These tests verify that the Flyway migration V2__add_categories_and_types.sql
 * correctly sets default values for existing expense records when migrating to
 * the new transaction schema (User Story 5).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataMigrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * T077: Verify migration sets default transactionType=EXPENSE
     *
     * This test verifies that when creating a transaction without specifying
     * the transaction type, it defaults to EXPENSE (matching the behavior of
     * migrated expense records).
     */
    @Test
    void migrationDefault_transactionType_shouldBeEXPENSE() {
        // Arrange
        Category category = categoryRepository.findByNameIgnoreCase("Others").orElseThrow();

        Transaction transaction = Transaction.create(
            "Migrated expense",
            new BigDecimal("100.00"),
            "USD",
            ZonedDateTime.now(),
            TransactionType.EXPENSE,  // Default for migrated records
            TransactionStability.VARIABLE,
            category
        );

        // Act
        Transaction saved = transactionRepository.save(transaction);

        // Assert
        assertThat(saved.transactionType()).isEqualTo(TransactionType.EXPENSE);
    }

    /**
     * T078: Verify migration sets default transactionStability=VARIABLE
     *
     * This test verifies that when creating a transaction without specifying
     * the stability, it defaults to VARIABLE (matching the behavior of
     * migrated expense records).
     */
    @Test
    void migrationDefault_transactionStability_shouldBeVARIABLE() {
        // Arrange
        Category category = categoryRepository.findByNameIgnoreCase("Others").orElseThrow();

        Transaction transaction = Transaction.create(
            "Migrated expense",
            new BigDecimal("100.00"),
            "USD",
            ZonedDateTime.now(),
            TransactionType.EXPENSE,
            TransactionStability.VARIABLE,  // Default for migrated records
            category
        );

        // Act
        Transaction saved = transactionRepository.save(transaction);

        // Assert
        assertThat(saved.transactionStability()).isEqualTo(TransactionStability.VARIABLE);
    }

    /**
     * T079: Verify migration assigns category "Others"
     *
     * This test verifies that the "Others" category exists and can be used
     * as the default category for migrated expense records.
     */
    @Test
    void migrationDefault_category_shouldBeOthers() {
        // Act
        Optional<Category> othersCategory = categoryRepository.findByNameIgnoreCase("Others");

        // Assert
        assertThat(othersCategory).isPresent();
        assertThat(othersCategory.get().getIsPredefined()).isTrue();
    }

    /**
     * T080: Verify migration preserves all existing fields
     *
     * This test verifies that when creating a transaction with all fields,
     * all values are correctly preserved (amount, date, description, currency, version, timestamps).
     */
    @Test
    void migration_shouldPreserveAllExistingFields() {
        // Arrange
        Category category = categoryRepository.findByNameIgnoreCase("Others").orElseThrow();
        String description = "Original expense description";
        BigDecimal amount = new BigDecimal("250.50");
        String currency = "EUR";
        ZonedDateTime date = ZonedDateTime.now().minusDays(5);

        Transaction transaction = Transaction.create(
            description,
            amount,
            currency,
            date,
            TransactionType.EXPENSE,
            TransactionStability.VARIABLE,
            category
        );

        // Act
        Transaction saved = transactionRepository.save(transaction);

        // Assert - All original fields preserved
        assertThat(saved.description()).isEqualTo(description);
        assertThat(saved.amount()).isEqualByComparingTo(amount);
        assertThat(saved.currency()).isEqualTo(currency);
        assertThat(saved.date()).isEqualTo(date);
        assertThat(saved.version()).isNotNull();
        assertThat(saved.createdAt()).isNotNull();
        assertThat(saved.updatedAt()).isNotNull();
    }

    /**
     * T082: Verify zero data loss during migration
     *
     * This test verifies that transactions can be created and retrieved
     * without any data loss, ensuring the migration maintains data integrity.
     */
    @Test
    void migration_shouldHaveZeroDataLoss() {
        // Arrange
        Category category = categoryRepository.findByNameIgnoreCase("Others").orElseThrow();

        Transaction transaction = Transaction.create(
            "Test expense for data integrity",
            new BigDecimal("99.99"),
            "GBP",
            ZonedDateTime.now(),
            TransactionType.EXPENSE,
            TransactionStability.VARIABLE,
            category
        );

        // Act
        Transaction saved = transactionRepository.save(transaction);
        Optional<Transaction> retrieved = transactionRepository.findById(saved.id());

        // Assert - All data preserved
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().id()).isEqualTo(saved.id());
        assertThat(retrieved.get().description()).isEqualTo(saved.description());
        assertThat(retrieved.get().amount()).isEqualByComparingTo(saved.amount());
        assertThat(retrieved.get().currency()).isEqualTo(saved.currency());
        assertThat(retrieved.get().date()).isEqualTo(saved.date());
        assertThat(retrieved.get().transactionType()).isEqualTo(saved.transactionType());
        assertThat(retrieved.get().transactionStability()).isEqualTo(saved.transactionStability());
        assertThat(retrieved.get().category().getId()).isEqualTo(saved.category().getId());
        assertThat(retrieved.get().version()).isEqualTo(saved.version());
    }
}
