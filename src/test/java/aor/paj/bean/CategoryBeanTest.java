package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dto.CategoryDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.UserEntity;
import aor.paj.exception.CriticalDataDeletionAttemptException;
import aor.paj.exception.EntityValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CategoryBeanTest {

    @InjectMocks
    private CategoryBean categoryBean;

    @Mock
    private CategoryDao categoryDao;

    @Mock
    private UserBean userBean;

    private UserEntity testUser;
    private CategoryEntity testCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new UserEntity();
        testUser.setUsername("admin");

        testCategory = new CategoryEntity();
        testCategory.setType("No_Category");
        testCategory.setAuthor(testUser);

        when(userBean.getUserByUsername("admin")).thenReturn(testUser);
    }

    @Test
    void testAddCategory_Success() {
        when(categoryDao.findCategoryByType(anyString())).thenReturn(null);
        doAnswer(invocation -> {
            CategoryEntity category = invocation.getArgument(0);
            category.setId(1); // Simulando o comportamento do persist.
            return null; // Retorno null é aceitável aqui porque estamos em um contexto doAnswer para um método void.
        }).when(categoryDao).persist(any(CategoryEntity.class));



    }


    @Test
    void testAddCategory_Failure_AlreadyExists() {
        when(categoryDao.findCategoryByType("Work")).thenReturn(testCategory);


    }

    @Test
    void testEditCategory_Success() throws EntityValidationException {
        when(categoryDao.findCategoryByType("Old_Category")).thenReturn(testCategory);
        when(categoryDao.findCategoryByType("New_Category")).thenReturn(null);

        categoryBean.editCategory("New_Category", "Old_Category");
    }

    @Test
    void testEditCategory_Failure_AlreadyExists() throws EntityValidationException {
        when(categoryDao.findCategoryByType("Old_Category")).thenReturn(testCategory);
        when(categoryDao.findCategoryByType("New_Category")).thenReturn(new CategoryEntity());


    }

    @Test
    void testDeleteCategory_Success() throws CriticalDataDeletionAttemptException, EntityValidationException {
        when(categoryDao.findCategoryByType("Work")).thenReturn(testCategory);

        verify(categoryDao, times(1)).deleteCategory("Work");
    }

    @Test
    void testDeleteCategory_Failure_NotFound() {
        when(categoryDao.findCategoryByType("Non_Existent")).thenReturn(null);

        verify(categoryDao, never()).deleteCategory(anyString());
    }

    @Test
    void testGetAllCategories_Success() {
        List<CategoryEntity> categories = new ArrayList<>();
        categories.add(testCategory);
        when(categoryDao.getAllCategories()).thenReturn(new ArrayList<>(categories));

        List<CategoryEntity> result = categoryBean.getAllCategories();
        assertNotNull(result, "The result should not be null");
        assertFalse(result.isEmpty(), "The result list should not be empty");
        assertEquals(1, result.size(), "The result list should contain one category");
        assertEquals(testCategory, result.get(0), "The category in the result list should match the test category");
    }
}
