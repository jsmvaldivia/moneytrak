package dev.juanvaldivia.moneytrak.transactions;

import dev.juanvaldivia.moneytrak.categories.Category;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA Entity representing a financial transaction.
 * Renamed from Expense to reflect broader scope (income + expenses).
 *
 * <p>Supports categorization, transaction types (EXPENSE/INCOME), and stability (FIXED/VARIABLE).
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 11, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private ZonedDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 32, nullable = false)
    private TransactionType type = TransactionType.EXPENSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_stability", length = 32, nullable = false)
    private TransactionStability stability = TransactionStability.VARIABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transaction_category"))
    private Category category;

    @Version
    private Integer version;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    protected Transaction() {
        // JPA requires no-arg constructor
    }

    private Transaction(
        UUID id,
        String description,
        BigDecimal amount,
        String currency,
        ZonedDateTime date,
        TransactionType type,
        TransactionStability stability,
        Category category,
        Integer version,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.currency = currency;
        this.date = date;
        this.type = type != null ? type : TransactionType.EXPENSE;
        this.stability = stability != null ? stability : TransactionStability.VARIABLE;
        this.category = category;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Create a new transaction.
     *
     * @param description transaction description
     * @param amount positive amount
     * @param currency ISO 4217 currency code
     * @param date transaction date
     * @param type EXPENSE or INCOME (defaults to EXPENSE if null)
     * @param stability FIXED or VARIABLE (defaults to VARIABLE if null)
     * @param category linked category (required)
     * @return new transaction instance
     */
    public static Transaction create(
        String description,
        BigDecimal amount,
        String currency,
        ZonedDateTime date,
        TransactionType type,
        TransactionStability stability,
        Category category
    ) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return new Transaction(
            null,
            description,
            amount,
            currency,
            date.withZoneSameInstant(ZoneOffset.UTC),
            type,
            stability,
            category,
            0,
            now,
            now
        );
    }

    /**
     * Create a transaction from migrated expense data.
     * Uses default values: type=EXPENSE, stability=VARIABLE.
     *
     * @param description transaction description
     * @param amount positive amount
     * @param currency ISO 4217 currency code
     * @param date transaction date
     * @param category category (will use "Others" for migrated data)
     * @return new transaction instance
     */
    public static Transaction createFromMigration(
        String description,
        BigDecimal amount,
        String currency,
        ZonedDateTime date,
        Category category
    ) {
        return create(description, amount, currency, date, TransactionType.EXPENSE, TransactionStability.VARIABLE, category);
    }

    /**
     * Update transaction fields.
     *
     * @param description new description
     * @param amount new amount
     * @param currency new currency
     * @param date new date
     * @param type new type (EXPENSE/INCOME)
     * @param stability new stability (FIXED/VARIABLE)
     * @param category new category
     */
    public void update(
        String description,
        BigDecimal amount,
        String currency,
        ZonedDateTime date,
        TransactionType type,
        TransactionStability stability,
        Category category
    ) {
        this.description = description;
        this.amount = amount;
        this.currency = currency;
        this.date = date.withZoneSameInstant(ZoneOffset.UTC);
        this.type = type != null ? type : this.type;
        this.stability = stability != null ? stability : this.stability;
        this.category = category;
        this.updatedAt = ZonedDateTime.now(ZoneOffset.UTC);
    }

    // Getters
    public UUID id() {
        return id;
    }

    public String description() {
        return description;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public ZonedDateTime date() {
        return date;
    }

    public TransactionType type() {
        return type;
    }

    public TransactionStability stability() {
        return stability;
    }

    public Category category() {
        return category;
    }

    public Integer version() {
        return version;
    }

    public ZonedDateTime createdAt() {
        return createdAt;
    }

    public ZonedDateTime updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction transaction)) return false;
        return Objects.equals(id, transaction.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
