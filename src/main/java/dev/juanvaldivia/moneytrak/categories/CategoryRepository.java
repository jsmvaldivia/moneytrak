package dev.juanvaldivia.moneytrak.categories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Category entity.
 * Provides standard CRUD operations and custom query methods.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Find category by name (case-insensitive).
     * Used for uniqueness validation and default category lookup.
     *
     * @param name category name
     * @return Optional containing category if found
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Check if category with given name exists (case-insensitive).
     * Used for duplicate name validation during create/update.
     *
     * @param name category name
     * @return true if category exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);
}