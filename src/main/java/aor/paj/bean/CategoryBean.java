package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dto.CategoryDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.UserEntity;
import aor.paj.exception.CriticalDataDeletionAttemptException;
import aor.paj.exception.DatabaseOperationException;
import aor.paj.exception.EntityValidationException;
import aor.paj.exception.UserConfirmationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


@Stateless
public class CategoryBean implements Serializable {
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(CategoryBean.class);

    @EJB
    CategoryDao categoryDao;
    @EJB
    UserBean userBean;
    @EJB
    StatisticsBean statisticsBean;

    public CategoryDto convertCategoryEntitytoCategoryDto(CategoryEntity categoryEntity){
        CategoryDto categoryDto=new CategoryDto();
        categoryDto.setId(categoryEntity.getId());
        categoryDto.setOwner_username(categoryEntity.getAuthor().getUsername());
        categoryDto.setType(categoryEntity.getType());
        return  categoryDto;
    }
    public void createDefaultCategoryIfNotExistent() throws DatabaseOperationException {
        CategoryEntity defaultCategory = categoryDao.findCategoryByType("No_Category");

        if(defaultCategory == null){
            categoryDao.persist(new CategoryEntity("No_Category",userBean.getUserByUsername("admin") ));

        }
    }

    public void addCategory(String type, String token) throws UserConfirmationException, EntityValidationException, UnknownHostException, DatabaseOperationException {
        UserEntity user = userBean.getUserByToken(token);
        CategoryEntity categoryEntity = categoryDao.findCategoryByType(type);
        if(user == null){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Invalid token");
            throw new UserConfirmationException("Invalid token");
        }
        if(categoryEntity != null){
            LOGGER.info(InetAddress.getLocalHost().getHostAddress() + "Category already exists");
            throw new EntityValidationException("Category already exists");
        }
        CategoryEntity newCategoryEntity = new CategoryEntity();
        newCategoryEntity.setType(type);
        newCategoryEntity.setAuthor(user);
        categoryDao.persist(newCategoryEntity);
        statisticsBean.broadcastCategoryStatisticsUpdate();
        convertCategoryEntitytoCategoryDto(newCategoryEntity);
    }

    public ArrayList<CategoryEntity> getAllCategories(){
        return categoryDao.getAllCategories();
    }


    public void editCategory(String newType, String oldType) throws EntityValidationException, UnknownHostException, DatabaseOperationException {
        CategoryEntity categoryEntity = categoryDao.findCategoryByType(oldType);
        CategoryEntity newCategoryEntity = categoryDao.findCategoryByType(newType);
        if(categoryEntity == null){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Invalid category type: " + oldType);
            throw new EntityValidationException("Invalid category type");
        }
        if(newCategoryEntity != null) {
            LOGGER.warn("Category already exists");
            throw new EntityValidationException("Category already exists");
        }
        categoryEntity.setType(newType);
        categoryDao.merge(categoryEntity);
        statisticsBean.broadcastCategoryStatisticsUpdate();
    }

    public void deleteCategory(String category_type) throws CriticalDataDeletionAttemptException, EntityValidationException, DatabaseOperationException {
        CategoryEntity categoryEntity = categoryDao.findCategoryByType(category_type);
        if(categoryEntity == null){
            LOGGER.warn("Invalid category type: " + category_type);
            throw new EntityValidationException("Invalid category type");
        }
        if(hasThisCategoryTasks(category_type)){
            LOGGER.warn("This category has tasks");
            throw new CriticalDataDeletionAttemptException("This category has tasks");
        }
        categoryDao.deleteCategory(category_type);
        statisticsBean.broadcastCategoryStatisticsUpdate();
    }

    public boolean hasThisCategoryTasks(String type) throws DatabaseOperationException {
        List<CategoryDto> catList = getCategoriesWithTasks();
        for(CategoryDto cat: catList){
            if(cat.getType().equals(type)){
                return true;
            }
        }
        return false;
    }
    public List<CategoryDto> getCategoriesWithTasks() throws DatabaseOperationException {
        List<CategoryEntity> categories = categoryDao.findCategoriesWithTasks();
        List<CategoryDto> categoriesWithTasks = new ArrayList<>();
        for(CategoryEntity category: categories){
            categoriesWithTasks.add(convertCategoryEntitytoCategoryDto(category));
        }
        return categoriesWithTasks;
    }
    public List<CategoryDto> convertCategoryEntitiesToCategoryDtos(List<CategoryEntity> categories){
        List<CategoryDto> categoryDtos = new ArrayList<>();
        for(CategoryEntity category: categories){
            categoryDtos.add(convertCategoryEntitytoCategoryDto(category));
        }
        return categoryDtos;
    }
    public List<CategoryDto> getAllCategoriesDtos(){
        ArrayList<CategoryEntity> categoriesEntities = getAllCategories();
        ArrayList<CategoryDto> categoriesDtos = new ArrayList<>();
        for (CategoryEntity category : categoriesEntities) {
            categoriesDtos.add(convertCategoryEntitytoCategoryDto(category));
        }
        return categoriesDtos;
    }
}
