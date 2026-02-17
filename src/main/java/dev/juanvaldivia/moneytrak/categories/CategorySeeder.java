package dev.juanvaldivia.moneytrak.categories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with 14 predefined categories on application startup.
 * Runs only if no categories exist (idempotent).
 */
@Component
public class CategorySeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CategorySeeder.class);

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        // Only seed if the default "Others" category does not exist yet
        if (categoryRepository.existsByNameIgnoreCase("Others")) {
            log.info("Categories already seeded, skipping...");
            return;
        }

        log.info("Seeding 15 predefined categories...");

        List<Category> predefinedCategories = List.of(
            Category.createPredefined("Office Renting"),
            Category.createPredefined("Public Transport"),
            Category.createPredefined("Bank"),
            Category.createPredefined("Car Maintenance"),
            Category.createPredefined("Food & Drinks"),
            Category.createPredefined("Subscriptions"),
            Category.createPredefined("Supermarket"),
            Category.createPredefined("Tolls"),
            Category.createPredefined("Gas"),
            Category.createPredefined("Sport"),
            Category.createPredefined("Gifts"),
            Category.createPredefined("ATM"),
            Category.createPredefined("Video & Films"),
            Category.createPredefined("Transfers"),
            Category.createPredefined("Others")
        );

        categoryRepository.saveAll(predefinedCategories);

        log.info("Successfully seeded {} predefined categories", predefinedCategories.size());
    }
}
