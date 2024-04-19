
package aor.paj.service;
import aor.paj.bean.PermissionBean;
import aor.paj.bean.TaskBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.TaskDto;
import aor.paj.entity.TaskEntity;
import aor.paj.service.status.Function;
import filters.RequiresPermission;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;


@Path("/tasks")
public class TaskService {
    @EJB
    TaskBean taskBean;
    @EJB
    UserBean userBean;
    @EJB
    PermissionBean permissionBean;



    /**
     * This endpoint is responsible for creating a new task in the system. It accepts JSON-formatted requests
     * containing task data and processes the request accordingly.
     * If the provided task data fails validation, it returns a status code of 400 (Bad Request) with the message
     * "Invalid task data".
     * If the task type is invalid, it returns a status code of 400 (Bad Request) with the message
     * "Invalid task type".
     * If the task is successfully created, it returns a status code of 200 (OK) with the message
     * "Task Created".
     */
    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTask(@Valid @HeaderParam("Authorization") String authHeader, TaskDto a) {
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
        String categoryName = a.getCategory_type();
        if(taskBean.addTask(token,categoryName,a)){
            return Response.status(200).entity("{\"message\":\"Task Created\"}").build();
        }return Response.status(400).entity("Invalid task type").build();
    }

    /**
     * This endpoint is responsible for editing an existing task in the system. It accepts JSON-formatted requests
     * containing task data and processes the request accordingly.
     * If the provided task ID is not valid, it returns a status code of 404 (Not Found) with the message
     * "Task with this id not found".
     * If the user does not have permission to edit the task or the task status is not provided, it returns a status code
     * of 403 (Forbidden) with the message "Access Denied".
     * If the task data is successfully edited, it returns a status code of 200 (OK) with the message
     * "Task updated successfully".
     * If the task data provided is incorrect, it returns a status code of 400 (Bad Request) with the message
     * "Wrong data".
     */
    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editTask(@Valid @PathParam("id")int id, @HeaderParam("Authorization") String authorizationHeader, TaskDto taskDto) {
        String token = authorizationHeader.substring("Bearer ".length());

        if (taskBean.taskIdValidator(id)) {
            if(permissionBean.getPermissionByTaskID(token, id) || taskDto.getStatus() != null) {
                if (taskBean.editTask(id, taskDto)) {
                    return Response.status(200).entity("Task updated successfuly.").build();
                } else return Response.status(400).entity("Wrong data.").build();
            } else return Response.status(403).entity("Access Denied").build();
        } else return Response.status(404).entity("Task with this id not found").build();
    }

    /**
     * This endpoint is responsible for retrieving task information by its ID from the system.
     * It accepts a path parameter 'id' to specify the ID of the task to be retrieved.
     * If the task ID is valid and exists in the system, it returns a status code of 200 (OK) along with
     * the JSON representation of the task.
     * If the task ID is not found in the system, it returns a status code of 404 (Not Found) with the message
     * "Task with this id not found".
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTaskById(@PathParam("id") int id) {
        if(taskBean.taskIdValidator(id)){
            return Response.status(200).entity(taskBean.convertTaskEntitytoTaskDto(taskBean.getTaskById(id))).build();
        }else return Response.status(404).entity("Task with this id not found").build();
    }

    /**
     * This endpoint is responsible for retrieving tasks from the system based on specified filters.
     * It accepts query parameters for filtering the tasks.
     * The 'deleted' parameter specifies whether to include deleted tasks.
     * The 'username' parameter filters tasks based on the username associated with them.
     * The 'category' parameter filters tasks based on the category type.
     * It returns a status code of 200 (OK) along with a JSON array containing the task DTOs that match the filters.
     */
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBYFilter(@QueryParam("deleted") boolean deleted,
                                   @QueryParam("username") String username,
                                   @QueryParam("category") String category_type) {
        List<TaskDto> tasksDtos = taskBean.getAllTasksByFilter(deleted, username, category_type);
        return Response.status(200).entity(tasksDtos).build();
    }

    /**
     * This endpoint is responsible for temporarily deleting all tasks associated with a specific user from the system.
     * It accepts the username of the user as a path parameter.
     * If the user has the necessary permission and the specified username exists in the system, it deletes
     * all tasks associated with that user and returns a status code of 200 (OK) with the message
     * "Tasks successfully deleted".
     * If the specified username does not exist in the system, it returns a status code of 404 (Not Found) with the message
     * "User with this username not found".
     * If the user does not have the required permission, it returns a status code of 403 (Forbidden) with the message
     * "User permissions violated".
     */
    @DELETE
    @Path("/temp/all/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.DELETE_ALL_TASKS_BY_USER_TEMPORARILY)
    public Response deleteAllTasksTemporarily(@PathParam("username")String username){
        if(userBean.getUserByUsername(username)!=null){
            taskBean.deleteAllTasksByUser(username);
            return Response.status(200).entity("{\"message\":\"Tasks sucecessfully deleted\"}").build();
        } else return Response.status(404).entity("User with this username not found").build();
    }

    /**
     * This endpoint is responsible for permanently deleting a task from the system.
     * It accepts the ID of the task to be deleted as a path parameter.
     * If the user has the necessary permission and the specified task ID exists in the system, it permanently deletes
     * the task and returns a status code of 200 (OK) with the message "This task permanently deleted".
     * If the specified task ID does not exist in the system, it returns a status code of 400 (Bad Request) with the message
     * "Task with this id not found".
     * If the user does not have the required permission, it returns a status code of 403 (Forbidden) with the message
     * "User permissions violated".
     */
    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.DELETE_TASK_PERMANENTLY)
    public Response deleteTaskPermanently(@PathParam("id")int id){
        if(taskBean.taskIdValidator(id)) {
            boolean deleted = taskBean.deleteTaskPermanently(id);
            if (deleted) return Response.status(200).entity("This task permanently deleted ").build();
            else return Response.status(400).entity("This task is already deleted").build();
        } else return Response.status(400).entity("Task with this id not found").build();
    }
}