package dev.juanvaldivia.moneytrak.categories.mapper;

import dev.juanvaldivia.moneytrak.categories.Category;
import dev.juanvaldivia.moneytrak.categories.dto.CategoryDto;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Category entity and CategoryDto.
 * Keeps domain models separate from API contracts.
 */
@Component
public class CategoryMapper {

    /**
     * Convert Category entity to CategoryDto for API response.
     *
     * @param category domain entity
     * @return DTO for API response
     */
    public CategoryDto toDto(Category category) {
        return new CategoryDto(
            category.getId(),
            category.getName(),
            category.getIsPredefined(),
            category.getVersion(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}
