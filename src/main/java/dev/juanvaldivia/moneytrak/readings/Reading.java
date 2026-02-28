package dev.juanvaldivia.moneytrak.readings;

import dev.juanvaldivia.moneytrak.accounts.Account;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA Entity representing a portfolio reading.
 * Records the balance of a financial account at a specific point in time.
 *
 * <p>Supports soft deletion (tombstone pattern) to preserve historical data.
 * The accountId relationship is immutable after creation.</p>
 *
 * <p>Amount precision is DECIMAL(15,8) to support cryptocurrency balances with 8 decimal places.</p>
 */
@Entity
@Table(name = "readings")
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_reading_account"))
    private Account account;

    @Column(nullable = false, precision = 15, scale = 8)
    private BigDecimal amount;

    @Column(name = "reading_date", nullable = false)
    private ZonedDateTime readingDate;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Version
    private Integer version;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    protected Reading() {
        // JPA requires no-arg constructor
    }

    private Reading(
        UUID id,
        Account account,
        BigDecimal amount,
        ZonedDateTime readingDate,
        Boolean deleted,
        Integer version,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.account = account;
        this.amount = amount;
        this.readingDate = readingDate;
        this.deleted = deleted;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Create a new reading.
     *
     * @param account account entity (required, immutable after creation)
     * @param amount balance amount (supports negative values for debts/margins)
     * @param readingDate date and time of the reading
     * @return new reading instance
     */
    public static Reading create(Account account, BigDecimal amount, ZonedDateTime readingDate) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return new Reading(
            null,
            account,
            amount,
            readingDate.withZoneSameInstant(ZoneOffset.UTC),
            false,
            0,
            now,
            now
        );
    }

    /**
     * Update reading fields.
     * Note: accountId is immutable and cannot be changed.
     *
     * @param amount new balance amount
     * @param readingDate new reading date
     */
    public void update(BigDecimal amount, ZonedDateTime readingDate) {
        this.amount = amount;
        this.readingDate = readingDate.withZoneSameInstant(ZoneOffset.UTC);
        this.updatedAt = ZonedDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Soft delete this reading.
     * Sets deleted=true to preserve historical data while marking as inactive.
     */
    public void markDeleted() {
        this.deleted = true;
        this.updatedAt = ZonedDateTime.now(ZoneOffset.UTC);
    }

    // Getters
    public UUID id() {
        return id;
    }

    public Account account() {
        return account;
    }

    public BigDecimal amount() {
        return amount;
    }

    public ZonedDateTime readingDate() {
        return readingDate;
    }

    public Boolean deleted() {
        return deleted;
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
        if (!(o instanceof Reading reading)) return false;
        return Objects.equals(id, reading.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
