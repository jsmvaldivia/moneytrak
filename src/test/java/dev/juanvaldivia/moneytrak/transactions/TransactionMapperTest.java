package dev.juanvaldivia.moneytrak.transactions;

import dev.juanvaldivia.moneytrak.categories.Category;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionCreationDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionDto;
import dev.juanvaldivia.moneytrak.transactions.dto.TransactionUpdateDto;
import dev.juanvaldivia.moneytrak.transactions.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TransactionMapper covering entity/DTO conversion logic.
 */
class TransactionMapperTest {

    private TransactionMapper mapper;
    private Category category;

    @BeforeEach
    void setUp() {
        mapper = new TransactionMapper();
        category = new Category("Food & Drinks", true, ZonedDateTime.now(), ZonedDateTime.now());
    }

    // ======================== toEntity ========================

    @Test
    void toEntity_withAllFields_shouldMapCorrectly() {
        TransactionCreationDto dto = new TransactionCreationDto(
            "Lunch",
            new BigDecimal("15.50"),
            "EUR",
            ZonedDateTime.now().minusDays(1),
            TransactionType.EXPENSE,
            TransactionStability.FIXED,
            null
        );

        Transaction result = mapper.toEntity(dto, category);

        assertThat(result.description()).isEqualTo("Lunch");
        assertThat(result.amount()).isEqualByComparingTo("15.50");
        assertThat(result.currency()).isEqualTo("EUR");
        assertThat(result.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.stability()).isEqualTo(TransactionStability.FIXED);
        assertThat(result.category()).isSameAs(category);
    }

    @Test
    void toEntity_withNullStability_shouldDefaultToVariable() {
        TransactionCreationDto dto = new TransactionCreationDto(
            "Coffee",
            new BigDecimal("3.50"),
            "EUR",
            ZonedDateTime.now().minusDays(1),
            TransactionType.EXPENSE,
            null,  // no stability provided
            null
        );

        Transaction result = mapper.toEntity(dto, category);

        assertThat(result.stability()).isEqualTo(TransactionStability.VARIABLE);
    }

    // ======================== updateEntity ========================

    @Test
    void updateEntity_withNullFields_shouldPreserveExistingValues() {
        Transaction existing = Transaction.create(
            "Old description",
            new BigDecimal("10.00"),
            "EUR",
            ZonedDateTime.now().minusDays(2),
            TransactionType.EXPENSE,
            TransactionStability.FIXED,
            category
        );

        TransactionUpdateDto dto = new TransactionUpdateDto(
            null, null, null, null, null, null, null, 0
        );

        mapper.updateEntity(existing, dto, null);

        assertThat(existing.description()).isEqualTo("Old description");
        assertThat(existing.amount()).isEqualByComparingTo("10.00");
        assertThat(existing.currency()).isEqualTo("EUR");
        assertThat(existing.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(existing.stability()).isEqualTo(TransactionStability.FIXED);
        assertThat(existing.category()).isSameAs(category);
    }

    @Test
    void updateEntity_withNewValues_shouldUpdateFields() {
        Category newCategory = new Category("Bank", true, ZonedDateTime.now(), ZonedDateTime.now());
        Transaction existing = Transaction.create(
            "Old description",
            new BigDecimal("10.00"),
            "EUR",
            ZonedDateTime.now().minusDays(2),
            TransactionType.EXPENSE,
            TransactionStability.VARIABLE,
            category
        );

        TransactionUpdateDto dto = new TransactionUpdateDto(
            "New description",
            new BigDecimal("25.00"),
            "USD",
            ZonedDateTime.now().minusDays(1),
            TransactionType.INCOME,
            TransactionStability.FIXED,
            null,
            0
        );

        mapper.updateEntity(existing, dto, newCategory);

        assertThat(existing.description()).isEqualTo("New description");
        assertThat(existing.amount()).isEqualByComparingTo("25.00");
        assertThat(existing.currency()).isEqualTo("USD");
        assertThat(existing.type()).isEqualTo(TransactionType.INCOME);
        assertThat(existing.stability()).isEqualTo(TransactionStability.FIXED);
        assertThat(existing.category()).isSameAs(newCategory);
    }

    @Test
    void updateEntity_withNullCategory_shouldPreserveExistingCategory() {
        Transaction existing = Transaction.create(
            "Some transaction",
            new BigDecimal("5.00"),
            "EUR",
            ZonedDateTime.now().minusDays(1),
            TransactionType.EXPENSE,
            TransactionStability.VARIABLE,
            category
        );

        TransactionUpdateDto dto = new TransactionUpdateDto(
            "Updated", null, null, null, null, null, null, 0
        );

        mapper.updateEntity(existing, dto, null);

        assertThat(existing.category()).isSameAs(category);
    }

    // ======================== toDto ========================

    @Test
    void toDto_shouldMapAllFieldsCorrectly() {
        Transaction entity = Transaction.create(
            "Groceries",
            new BigDecimal("42.00"),
            "EUR",
            ZonedDateTime.now().minusDays(1),
            TransactionType.EXPENSE,
            TransactionStability.VARIABLE,
            category
        );

        TransactionDto result = mapper.toDto(entity);

        assertThat(result.description()).isEqualTo("Groceries");
        assertThat(result.amount()).isEqualByComparingTo("42.00");
        assertThat(result.currency()).isEqualTo("EUR");
        assertThat(result.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.stability()).isEqualTo(TransactionStability.VARIABLE);
        assertThat(result.categoryName()).isEqualTo("Food & Drinks");
        assertThat(result.version()).isEqualTo(0);
    }
}
