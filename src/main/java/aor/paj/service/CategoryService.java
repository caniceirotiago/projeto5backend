package aor.paj.service;

import aor.paj.bean.CategoryBean;
import aor.paj.bean.TaskBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.CategoryDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.UserEntity;
import aor.paj.exception.CriticalDataDeletionAttemptException;
import aor.paj.exception.DatabaseOperationException;
import aor.paj.exception.EntityValidationException;
import aor.paj.exception.UserConfirmationException;
import aor.paj.service.status.Function;
import aor.paj.service.status.userRoleManager;
import filters.RequiresPermission;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Path("/category")
public class CategoryService {
    @EJB
    CategoryBean categoryBean;
    /**
     *  retrieves information about a specific category identified by its ID
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.GET_ALL_CATEGORIES)
    public List<CategoryDto> getAllCategories() {return categoryBean.getAllCategoriesDtos();}
    /**
     * method to add a new category
     */
    @POST
    @Path("/add/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.ADD_NEW_CATEGORY)
    public void addCategory(@PathParam("type")String type, @HeaderParam("Authorization") String authHeader) throws UserConfirmationException, EntityValidationException, UnknownHostException, DatabaseOperationException {
        String token = authHeader.substring(7);
        categoryBean.addCategory(type, token);
    }
    /**
     * method to edit the type of the category
     */
    @PATCH
    @Path("/edit/{oldType}/{newType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.EDIT_CATEGORY)
    public void editCategory (@PathParam("newType") String newType,@PathParam("oldType")String oldType) throws EntityValidationException, UnknownHostException, DatabaseOperationException {
        categoryBean.editCategory(newType, oldType);
    }
    /**
     * method to delete a category
     */
    @DELETE
    @Path("/delete/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.DELETE_CATEGORY)
    public void deleteCaTEGORY(@PathParam("type")String type) throws CriticalDataDeletionAttemptException, EntityValidationException, DatabaseOperationException {
        categoryBean.deleteCategory(type);
    }
    /**
     * method that retrieves the number of tasks of the specified category
     */
    @GET
    @Path("/categoriesWithTasks")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CategoryDto> getCategoriesWithTasks() throws DatabaseOperationException {
        return categoryBean.getCategoriesWithTasks();
    }
}
