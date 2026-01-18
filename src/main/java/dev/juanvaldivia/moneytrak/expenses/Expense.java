package dev.juanvaldivia.moneytrak.expenses;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "expenses")
public class Expense {

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

    @Version
    private Integer version;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    protected Expense() {
        // JPA requires no-arg constructor
    }

    private Expense(
        UUID id,
        String description,
        BigDecimal amount,
        String currency,
        ZonedDateTime date,
        Integer version,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.currency = currency;
        this.date = date;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Expense create(
        String description,
        BigDecimal amount,
        String currency,
        ZonedDateTime date
    ) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return new Expense(
            null,
            description,
            amount,
            currency,
            date.withZoneSameInstant(ZoneOffset.UTC),
            0,
            now,
            now
        );
    }

    public void update(
        String description,
        BigDecimal amount,
        String currency,
        ZonedDateTime date
    ) {
        this.description = description;
        this.amount = amount;
        this.currency = currency;
        this.date = date.withZoneSameInstant(ZoneOffset.UTC);
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
        if (!(o instanceof Expense expense)) return false;
        return Objects.equals(id, expense.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
