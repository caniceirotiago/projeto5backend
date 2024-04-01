package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dto.CategoryDto;
import aor.paj.dto.TaskDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Stateless
public class CategoryBean implements Serializable {

    @EJB
    CategoryDao categoryDao;
    @EJB
    UserBean userBean;
    @Inject
    TaskBean taskBean;

    public CategoryDto convertCategoryEntitytoCategoryDto(CategoryEntity categoryEntity){
        CategoryDto categoryDto=new CategoryDto();
        categoryDto.setId(categoryEntity.getId());
        categoryDto.setOwner_username(categoryEntity.getAuthor().getUsername());
        categoryDto.setType(categoryEntity.getType());
        return  categoryDto;
    }
    public void createDefaultCategoryIfNotExistent(){
        CategoryEntity defaultCategory = categoryDao.findCategoryByType("No_Category");

        if(defaultCategory == null){
            categoryDao.persist(new CategoryEntity("No_Category",userBean.getUserByUsername("admin") ));

        }
    }

    public CategoryDto addCategory(UserEntity user, String type){
        if(categoryDao.findCategoryByType(type)==null) {
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setType(type);
            categoryEntity.setAuthor(user);
            categoryDao.persist(categoryEntity);
            CategoryDto categoryDto=convertCategoryEntitytoCategoryDto(categoryEntity);
            return categoryDto;
        }
        else return null;
    }

    public ArrayList<CategoryEntity> getAllCategories(){
        return categoryDao.getAllCategories();
    }


    public boolean editCategory(String newType, String oldType){
        if(categoryDao.findCategoryByType(newType)==null) {
            CategoryEntity categoryEntity = categoryDao.findCategoryByType(oldType);
            categoryEntity.setType(newType);

            categoryDao.merge(categoryEntity);
            return true;
        }
        return false;
    }

    public boolean deleteCategory(String category_type){
        if(categoryDao.findCategoryByType(category_type)!=null){
            categoryDao.deleteCategory(category_type);

            return true;
        } else return false;
    }
    public boolean categoryTypeValidator(String type){
        if(categoryDao.findCategoryByType(type)!=null) return true;
        else return false;
    }

    public boolean hasThisCategoryTasks(String type){
        List<CategoryDto> catList = getCategoriesWithTasks();
        for(CategoryDto cat: catList){
            if(cat.getType().equals(type)){
                return true;
            }
        }
        return false;
    }
    public List<CategoryDto> getCategoriesWithTasks(){
        List<CategoryEntity> categories = categoryDao.findCategoriesWithTasks();
        List<CategoryDto> categoriesWithTasks = new ArrayList<>();
        for(CategoryEntity category: categories){
            categoriesWithTasks.add(convertCategoryEntitytoCategoryDto(category));
        }
        return categoriesWithTasks;
    }
}
