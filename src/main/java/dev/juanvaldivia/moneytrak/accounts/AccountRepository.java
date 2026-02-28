package dev.juanvaldivia.moneytrak.accounts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for Account entity.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Find all accounts ordered by name, then by id for deterministic ordering.
     * Ensures consistent results across requests even when account names are duplicated.
     *
     * @return list of all accounts in deterministic order
     */
    @Query("SELECT a FROM Account a ORDER BY a.name, a.id")
    List<Account> findAllOrderedByName();
}
