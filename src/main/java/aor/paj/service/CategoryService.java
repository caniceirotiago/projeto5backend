package aor.paj.service;

import aor.paj.bean.CategoryBean;
import aor.paj.bean.TaskBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.CategoryDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.UserEntity;
import aor.paj.service.status.Function;
import aor.paj.service.status.userRoleManager;
import filters.RequiresPermission;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("/category")
public class CategoryService {

    @EJB
    UserBean userBean;
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
    public Response getAllCategories() {
        ArrayList<CategoryEntity> categoriesEntities = categoryBean.getAllCategories();
        ArrayList<CategoryDto> categoriesDtos = new ArrayList<>();
        for (CategoryEntity category : categoriesEntities) {
            categoriesDtos.add(categoryBean.convertCategoryEntitytoCategoryDto(category));
        }
        return Response.status(200).entity(categoriesDtos).build();
    }


    /**
     * method to add a new category
     */

    @POST
    @Path("/add/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.ADD_NEW_CATEGORY)
    public Response addCategory(@PathParam("type")String type, @HeaderParam("Authorization") String authHeader){
        String token = authHeader.substring(7);
        UserEntity user = userBean.getUserByToken(token);
        CategoryDto categoryDto=categoryBean.addCategory(user, type);
        if (categoryDto!=null) {
            return Response.status(200).entity("{\"message\":\"Category added\"}").build();
        } else return Response.status(400).entity("That category type already exists").build();
    }


    /**
     * method to edit the type of the category
     */

    @PATCH
    @Path("/edit/{oldType}/{newType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.EDIT_CATEGORY)
    public Response editCategory (@PathParam("newType") String newType,@PathParam("oldType")String oldType){
        if (categoryBean.categoryTypeValidator(oldType)) {
            if (categoryBean.editCategory(newType, oldType)) {
                return Response.status(200).entity("The category was edited successfully").build();
            } else return Response.status(400).entity("That category type already exists").build();
        } else return Response.status(404).entity("That category doesn't exists").build();
    }

    /**
     * method to delete a category
     */
    @DELETE
    @Path("/delete/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCaTEGORY(@PathParam("type")String type){
        if(!categoryBean.hasThisCategoryTasks(type)) {
            if (categoryBean.deleteCategory(type)) {
                return Response.status(200).entity("The category was successfully deleted").build();
            } else return Response.status(404).entity("That category doesn't exists").build();
        } else return Response.status(400).entity("This category has tasks").build();
    }

    /**
     * method that retrieves the number of tasks of the specified category
     */
    @GET
    @Path("/categoriesWithTasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategoriesWithTasks() {
        List<CategoryDto> categoriesWithTasks = categoryBean.getCategoriesWithTasks();
        return Response.status(200).entity(categoriesWithTasks).build();
    }
}
