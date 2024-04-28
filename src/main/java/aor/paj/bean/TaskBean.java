package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.TaskDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.exception.DatabaseOperationException;
import aor.paj.exception.EntityValidationException;
import aor.paj.exception.UserConfirmationException;
import aor.paj.service.status.userRoleManager;
import aor.paj.service.websocket.TaskWebSocket;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskBean is an application-scoped bean that manages task operations, including reading from and writing to a JSON file,
 * 'allTasks.json'. It supports creating, retrieving, updating, and deleting tasks, as well as retrieving all tasks for a
 * specific user. The bean sorts tasks based on priority, start date, and end date for user-specific queries. It utilizes
 * Jsonb for JSON processing, ensuring tasks are persistently stored and managed efficiently. This bean plays a crucial
 * role in task management within the application, providing a centralized point for task data manipulation and retrieval.
 */

@Stateless
public class TaskBean implements Serializable {
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(TaskBean.class);

    @EJB
    TaskDao taskDao;
    @EJB
    UserBean userBean;
    @EJB
    CategoryDao categoryDao;
    @EJB
    UserDao userDao;
    @EJB
    StatisticsBean statisticsBean;
    private TaskEntity convertTaskDtotoTaskEntity(TaskDto taskDto){
        TaskEntity taskEntity=new TaskEntity();
        taskEntity.setTitle(taskDto.getTitle());
        taskEntity.setDescription(taskDto.getDescription());
        taskEntity.setStartDate(taskDto.getStartDate());
        taskEntity.setEndDate(taskDto.getEndDate());
        taskEntity.setStatus(taskDto.getStatus());
        taskEntity.setPriority(taskDto.getPriority());

        return taskEntity;
    }
    public TaskDto convertTaskEntitytoTaskDto(TaskEntity taskEntity){
        TaskDto taskDto=new TaskDto();
        taskDto.setDescription(taskEntity.getDescription());
        taskDto.setId(taskEntity.getId());
        taskDto.setStatus(taskEntity.getStatus());
        taskDto.setTitle(taskEntity.getTitle());
        taskDto.setCategory_type(taskEntity.getCategory().getType());
        taskDto.setPriority(taskEntity.getPriority());
        taskDto.setUsername_author(taskEntity.getUser().getUsername());
        taskDto.setStartDate(taskEntity.getStartDate());
        taskDto.setEndDate(taskEntity.getEndDate());
        taskDto.setDeleted(taskEntity.isDeleted());
        return taskDto;
    }
    public void addTask(String token, TaskDto taskDto) throws EntityValidationException, UserConfirmationException, UnknownHostException, DatabaseOperationException {
        String categoryName = taskDto.getCategory_type();
        CategoryEntity categoryEntity = categoryDao.findCategoryByType(categoryName);
        UserEntity userEntity=userBean.getUserByToken(token);
        if (categoryEntity == null) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Invalid category type: " + categoryName);
            throw new EntityValidationException("Invalid category type");
        }
        if(userEntity == null){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Invalid token: " + token + " for task creation");
            throw new UserConfirmationException("Invalid token");
        }
        TaskEntity taskEntity=convertTaskDtotoTaskEntity(taskDto);
        taskEntity.setUser(userEntity);
        taskEntity.setCategory(categoryEntity);
        taskEntity.setStatus(100);
        taskEntity.setDeleted(false);
        if(taskDto.getStartDate()!=null) {
            taskEntity.setStartDate(taskDto.getStartDate());
        }
        if(taskDto.getEndDate()!=null) {
            taskEntity.setEndDate(taskDto.getEndDate());
        }
        taskDao.persist(taskEntity);
        statisticsBean.broadcastTaskStatisticsUpdate();
        statisticsBean.broadcastCategoryStatisticsUpdate();
        statisticsBean.broadcastUserStatisticsUpdate();
        TaskDto addedDto = convertTaskEntitytoTaskDto(taskEntity);
        TaskWebSocket.broadcast("createTask", addedDto);
    }
    public TaskEntity getTaskById(int task_id){
        return taskDao.findTaskById(task_id);
    }

    public void editTask(int id, TaskDto taskDto) throws EntityValidationException, UnknownHostException, DatabaseOperationException {
        TaskEntity taskEntity=taskDao.findTaskById(id);
        if(taskEntity==null){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Invalid Task id");
            throw new EntityValidationException("Invalid Task id ");
        }
        if (taskDto.getCategory_type() != null) {
            CategoryEntity category = categoryDao.findCategoryByType(taskDto.getCategory_type());
            if (category == null) {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Invalid category type: " + taskDto.getCategory_type());
                throw new EntityValidationException("Invalid category type");
            }
        }
        boolean isPreviousTaskDeleteded = taskEntity.isDeleted();
        if(taskDto.getCategory_type() != null)taskEntity.setCategory(categoryDao.findCategoryByType(taskDto.getCategory_type()));
        if(taskDto.getTitle() != null)taskEntity.setTitle(taskDto.getTitle());
        if(taskDto.getDescription() != null)taskEntity.setDescription(taskDto.getDescription());
        if(taskDto.getPriority() != null)taskEntity.setPriority(taskDto.getPriority());
        if(taskDto.getStatus() != null){
            taskEntity.setStatus(taskDto.getStatus());
            taskEntity = updateTimeStamps(taskEntity, taskDto.getStatus());
        }
        if(taskDto.isDeleted() != null)taskEntity.setDeleted(taskDto.isDeleted());
        if(taskDto.getEndDate()!=null){
            taskEntity.setEndDate(taskDto.getEndDate());
        }
        if(taskDto.getStartDate()!=null){
            taskEntity.setStartDate(taskDto.getStartDate());
        }
        taskDao.merge(taskEntity);
        statisticsBean.broadcastTaskStatisticsUpdate();
        statisticsBean.broadcastCategoryStatisticsUpdate();
        TaskDto updatedDto = convertTaskEntitytoTaskDto(taskEntity);
        TaskWebSocket.broadcast("updatedTask", updatedDto);
        if(isPreviousTaskDeleteded && !taskEntity.isDeleted()){
            TaskWebSocket.broadcast("recycleTask", updatedDto);
        }
    }
    public TaskDto getTaskDtoById(int id) throws EntityValidationException, UnknownHostException {
        TaskEntity taskEntity=taskDao.findTaskById(id);
        if(taskEntity==null){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Invalid Task id");
            throw new EntityValidationException("Invalid Task id ");
        }
        return convertTaskEntitytoTaskDto(taskEntity);
    }
    public TaskEntity updateTimeStamps(TaskEntity taskEntity, int newStatus){
        if(newStatus == 200) taskEntity.setDoingTimestamp(LocalDateTime.now());
        if(newStatus == 300) taskEntity.setDoneTimestamp(LocalDateTime.now());
        return taskEntity;
    }
    public void deleteTaskPermanently(int id) throws EntityValidationException, UnknownHostException, DatabaseOperationException {
        TaskEntity taskEntity=taskDao.findTaskById(id);
        if(taskEntity==null) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Invalid Task id");
            throw new EntityValidationException("Invalid Task id ");
        }
        taskDao.deleteTask(id);
        statisticsBean.broadcastTaskStatisticsUpdate();
        statisticsBean.broadcastCategoryStatisticsUpdate();
        statisticsBean.broadcastUserStatisticsUpdate();
        TaskDto deletedDto = convertTaskEntitytoTaskDto(taskEntity);
        TaskWebSocket.broadcast("deletedTaskPermanentely", deletedDto);
    }
    public boolean deleteTemporarily(int id){
        TaskEntity taskEntity=taskDao.findTaskById(id);
        if(taskEntity==null) return false;
        if(!taskEntity.isDeleted() ){
            taskEntity.setDeleted(true);
            taskDao.merge(taskEntity);
            TaskDto deletedDto = convertTaskEntitytoTaskDto(taskEntity);
            TaskWebSocket.broadcast("deleteTask", deletedDto);
            return true;
        }
        else return false;
    }
    public List<TaskDto> getAllTasksByFilter(boolean deleted, String username, String category_type){
        List<TaskEntity> tasksEnt = taskDao.getTasksByFilter(deleted, username, category_type);
        List<TaskDto> tasksDtos = new ArrayList<>();
        for (TaskEntity task : tasksEnt) {
            tasksDtos.add(convertTaskEntitytoTaskDto(task));
        }
        return tasksDtos;
    }
    public void deleteAllTasksByUser(String username) throws UserConfirmationException, UnknownHostException, DatabaseOperationException {
        if(userBean.getUserByUsername(username)==null){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Invalid username: " + username + " for task deletion");
            throw new UserConfirmationException("Invalid username");
        }
        List<TaskEntity> tasks = taskDao.getTasksByFilter(false, username, null);
        taskDao.deleteAllTasksByUser(username);
        for (TaskEntity task : tasks) {
            TaskDto deletedDto = convertTaskEntitytoTaskDto(task);
            deletedDto.setDeleted(true);
            TaskWebSocket.broadcast("updatedTask", deletedDto);
        }
        statisticsBean.broadcastTaskStatisticsUpdate();
        statisticsBean.broadcastCategoryStatisticsUpdate();
        statisticsBean.broadcastUserStatisticsUpdate();
    }
    /*Tests setters*/
    public void setTaskDao(TaskDao taskDao) {
        this.taskDao = taskDao;

    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

}