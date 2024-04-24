
package aor.paj.service;
import aor.paj.bean.TaskBean;
import aor.paj.dto.TaskDto;
import aor.paj.exception.EntityValidationException;
import aor.paj.exception.UserConfirmationException;
import aor.paj.service.status.Function;
import filters.RequiresPermission;
import filters.RequiresPermissionByTaskId;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/tasks")
public class TaskService {
    @EJB
    TaskBean taskBean;
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
    public void createTask(@Valid @HeaderParam("Authorization") String authHeader, TaskDto task) throws EntityValidationException, UserConfirmationException {
        String token = authHeader.substring(7);
        taskBean.addTask(token,task);
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
    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresPermissionByTaskId
    public void editTask(@Valid @PathParam("id")int id, @HeaderParam("Authorization") String authorizationHeader, TaskDto taskDto) throws EntityValidationException {
        taskBean.editTask(id, taskDto);
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
    public TaskDto getTaskById(@PathParam("id") int id) throws EntityValidationException {
        return taskBean.getTaskDtoById(id);
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
    public List<TaskDto> getAllBYFilter(@QueryParam("deleted") boolean deleted,
                                        @QueryParam("username") String username,
                                        @QueryParam("category") String category_type) {
        return taskBean.getAllTasksByFilter(deleted, username, category_type);
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
    public void deleteAllTasksTemporarily(@PathParam("username")String username) throws UserConfirmationException {
        taskBean.deleteAllTasksByUser(username);
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
    public void deleteTaskPermanently(@PathParam("id")int id) throws EntityValidationException {
        taskBean.deleteTaskPermanently(id);
    }
}