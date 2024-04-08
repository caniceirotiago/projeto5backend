package aor.paj.dto.Statistics;

import aor.paj.dto.CategoryDto;

import java.util.List;

public class CategoryStatisticsDTO {
    private List<CategoryDto> categories;

    public CategoryStatisticsDTO() {}

    public CategoryStatisticsDTO(List<CategoryDto> categories) {
        this.categories = categories;
    }

    public List<CategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDto> categories) {
        this.categories = categories;
    }
}
