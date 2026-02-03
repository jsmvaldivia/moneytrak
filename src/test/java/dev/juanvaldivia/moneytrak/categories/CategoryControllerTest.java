package dev.juanvaldivia.moneytrak.categories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TDD Test for Category Controller - User Story 1
 * Tests REST API endpoints for category management
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    // T009: Test for listing all categories including 14 predefined
    @Test
    void getAllCategories_ShouldReturn14PredefinedCategories() throws Exception {
        // Given: 14 predefined categories seeded by CategorySeeder
        long count = categoryRepository.count();

        // When/Then: GET /v1/categories returns 200 OK with at least 14 categories
        mockMvc.perform(get("/v1/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value((int) count))
            .andExpect(jsonPath("$[?(@.name == 'Office Renting')].isPredefined").value(true))
            .andExpect(jsonPath("$[?(@.name == 'Others')].isPredefined").value(true));
    }

    // T010: Test for creating custom category
    @Test
    void createCategory_WithValidName_ShouldReturn201Created() throws Exception {
        // When/Then: POST /v1/categories returns 201 Created with Location header
        mockMvc.perform(post("/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Medical Expenses\"}"))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.name").value("Medical Expenses"))
            .andExpect(jsonPath("$.isPredefined").value(false));
    }

    // T011: Test for retrieving category by ID
    @Test
    void getCategoryById_WhenExists_ShouldReturn200Ok() throws Exception {
        // Given: Get a predefined category ID
        Category category = categoryRepository.findByNameIgnoreCase("Food & Drinks").orElseThrow();

        // When/Then: GET /v1/categories/{id} returns 200 OK with category
        mockMvc.perform(get("/v1/categories/{id}", category.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(category.getId().toString()))
            .andExpect(jsonPath("$.name").value("Food & Drinks"));
    }

    // T012: Test for updating category name (predefined and custom)
    @Test
    void updateCategory_WithValidData_ShouldReturn200Ok() throws Exception {
        // Given: Get "Gas" category to update
        Category category = categoryRepository.findByNameIgnoreCase("Gas").orElseThrow();

        // When/Then: PUT /v1/categories/{id} returns 200 OK with updated category
        mockMvc.perform(put("/v1/categories/{id}", category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Fuel\",\"version\":" + category.getVersion() + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Fuel"));
    }

    // T013: Test for deleting category with no transactions
    @Test
    void deleteCategory_WithNoTransactions_ShouldReturn204NoContent() throws Exception {
        // Given: Create a new category with no transactions
        Category newCategory = categoryRepository.save(Category.createCustom("Temporary"));

        // When/Then: DELETE /v1/categories/{id} returns 204 No Content
        mockMvc.perform(delete("/v1/categories/{id}", newCategory.getId()))
            .andExpect(status().isNoContent());
    }

    // T014: Test for preventing deletion of category with linked transactions (409 Conflict)
    @Test
    void deleteCategory_WithLinkedTransactions_ShouldReturn409Conflict() throws Exception {
        // Given: Create a custom category
        String categoryResponse = mockMvc.perform(post("/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Temporary Category\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String categoryId = extractId(categoryResponse);

        // Link a transaction to this category via the transactions API
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "description": "Linked transaction",
                        "amount": 10.00,
                        "currency": "EUR",
                        "date": "2026-01-15T00:00:00Z",
                        "transactionType": "EXPENSE",
                        "categoryId": "%s"
                    }
                    """.formatted(categoryId)))
            .andExpect(status().isCreated());

        // When/Then: DELETE should return 409 Conflict
        mockMvc.perform(delete("/v1/categories/{id}", categoryId))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    private String extractId(String jsonResponse) {
        int idStart = jsonResponse.indexOf("\"id\":\"") + 6;
        int idEnd = jsonResponse.indexOf("\"", idStart);
        return jsonResponse.substring(idStart, idEnd);
    }

    // T015: Test for duplicate category name validation (409 Conflict)
    @Test
    void createCategory_WithDuplicateName_ShouldReturn409Conflict() throws Exception {
        // Given: "Bank" category already exists (predefined)

        // When/Then: POST /v1/categories with duplicate name returns 409 Conflict
        mockMvc.perform(post("/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Bank\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"));
    }
}