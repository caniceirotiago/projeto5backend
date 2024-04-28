package aor.paj.dto.Statistics;

import aor.paj.dto.CategoryDto;

import java.util.List;
import java.util.Map;

public class CategoryStatisticsDTO {
    private Map<String, Long> categories;

    public CategoryStatisticsDTO() {}

    public CategoryStatisticsDTO(Map<String, Long> categories) {
        this.categories = categories;
    }

    public Map<String, Long> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, Long> categories) {
        this.categories = categories;
    }
}
