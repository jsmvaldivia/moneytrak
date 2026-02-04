package dev.juanvaldivia.moneytrak.categories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD Integration Test for Category Seeding - User Story 1 (T016)
 * Tests that 14 predefined categories are seeded on application startup
 */
@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class CategoryIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    // T016: Test for category seeding on startup
    @Test
    void onStartup_Should_seed14PredefinedCategories() {
        // When: Application starts (categories should be seeded by CommandLineRunner)
        long count = categoryRepository.count();

        // Then: Exactly 14 predefined categories should exist
        assertThat(count).isGreaterThanOrEqualTo(14);

        // Verify key predefined categories exist
        assertThat(categoryRepository.findByNameIgnoreCase("Office Renting")).isPresent();
        assertThat(categoryRepository.findByNameIgnoreCase("Public Transport")).isPresent();
        assertThat(categoryRepository.findByNameIgnoreCase("Bank")).isPresent();
        assertThat(categoryRepository.findByNameIgnoreCase("Food & Drinks")).isPresent();
        assertThat(categoryRepository.findByNameIgnoreCase("Subscriptions")).isPresent();
        assertThat(categoryRepository.findByNameIgnoreCase("Others")).isPresent();

        // Verify "Others" category exists (critical for default assignment)
        Category othersCategory = categoryRepository.findByNameIgnoreCase("Others").orElseThrow();
        assertThat(othersCategory.getIsPredefined()).isTrue();
    }
}