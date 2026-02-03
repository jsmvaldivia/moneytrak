package dev.juanvaldivia.moneytrak.categories;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * JPA Entity representing a transaction category.
 * Categories organize transactions into meaningful groups (e.g., "Food & Drinks", "Bank").
 *
 * <p>Categories can be predefined (system-seeded) or custom (user-created).
 * Predefined categories can be renamed but maintain their isPredefined flag.
 *
 * <p>Uses optimistic locking via @Version to prevent concurrent update conflicts.
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "is_predefined", nullable = false)
    private Boolean isPredefined = false;

    @Version
    @Column(nullable = false)
    private Integer version = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    /**
     * Default constructor for JPA.
     */
    protected Category() {
    }

    /**
     * Constructor for creating a new category.
     *
     * @param name category name (max 100 characters)
     * @param isPredefined true if system-predefined category, false if user-created
     * @param createdAt timestamp when category was created
     * @param updatedAt timestamp when category was last updated
     */
    public Category(String name, Boolean isPredefined, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.name = name;
        this.isPredefined = isPredefined;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory method for creating a predefined category.
     *
     * @param name category name
     * @return new predefined category instance
     */
    public static Category createPredefined(String name) {
        ZonedDateTime now = ZonedDateTime.now();
        return new Category(name, true, now, now);
    }

    /**
     * Factory method for creating a custom (user-created) category.
     *
     * @param name category name
     * @return new custom category instance
     */
    public static Category createCustom(String name) {
        ZonedDateTime now = ZonedDateTime.now();
        return new Category(name, false, now, now);
    }

    /**
     * Updates the category name and bumps the updated timestamp.
     *
     * @param newName new category name
     */
    public void updateName(String newName) {
        this.name = newName;
        this.updatedAt = ZonedDateTime.now();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean getIsPredefined() {
        return isPredefined;
    }

    public Integer getVersion() {
        return version;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category category = (Category) o;
        return id != null && id.equals(category.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}