package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.TaskDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.service.status.userRoleManager;
import aor.paj.service.validator.TaskValidator;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskBean is an application-scoped bean that manages task operations, including reading from and writing to a JSON file,
 * 'allTasks.json'. It supports creating, retrieving, updating, and deleting tasks, as well as retrieving all tasks for a
 * specific user. The bean sorts tasks based on priority, start date, and end date for user-specific queries. It utilizes
 * Jsonb for JSON processing, ensuring tasks are persistently stored and managed efficiently. This bean plays a crucial
 * role in task management within the application, providing a centralized point for task data manipulation and retrieval.
 */

@ApplicationScoped
public class TaskBean{

    @EJB
    TaskDao taskDao;
    @EJB
    UserBean userBean;
    @EJB
    CategoryDao categoryDao;
    @Inject
    TaskValidator taskValidator;
    @EJB
    UserDao userDao;
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
    public boolean addTask(String token,String type,TaskDto taskDto) {
        if(token==null || type==null || taskDto==null) return false;
        UserEntity userEntity=userBean.getUserByToken(token);
        TaskEntity taskEntity=convertTaskDtotoTaskEntity(taskDto);
        CategoryEntity categoryEntity=categoryDao.findCategoryByType(type);
        if(categoryEntity!=null) {
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
            return true;
        }
        else return false;
    }
    public TaskEntity getTaskById(int task_id){
        return taskDao.findTaskById(task_id);
    }

    public boolean taskIdValidator(int task_id){
        if(taskDao.findTaskById(task_id)==null) return false;
        else return true;
    }
    public boolean editTask(int id, TaskDto taskDto){
        if(taskDto == null || id < 0) return false;
        if(taskValidator.validateTask(taskDto)){
            TaskEntity taskEntity=taskDao.findTaskById(id);
            if(taskEntity==null) return false;
            if(taskDto.getCategory_type() != null)taskEntity.setCategory(categoryDao.findCategoryByType(taskDto.getCategory_type()));
            if(taskDto.getTitle() != null)taskEntity.setTitle(taskDto.getTitle());
            if(taskDto.getDescription() != null)taskEntity.setDescription(taskDto.getDescription());
            if(taskDto.getPriority() != null)taskEntity.setPriority(taskDto.getPriority());
            if(taskDto.getStatus() != null)taskEntity.setStatus(taskDto.getStatus());
            if(taskDto.isDeleted() != null)taskEntity.setDeleted(taskDto.isDeleted());
            if(taskDto.getEndDate()!=null){
                taskEntity.setEndDate(taskDto.getEndDate());
            }
            if(taskDto.getStartDate()!=null){
                taskEntity.setStartDate(taskDto.getStartDate());
            }
            taskDao.merge(taskEntity);
            return true;
        }
        return false;
    }
    public boolean deleteTaskPermanently(int id){
        if(taskDao.findTaskById(id)==null) return false;
        taskDao.deleteTask(id);
        return true;
    }
    public boolean deleteTemporarily(int id){
        TaskEntity taskEntity=taskDao.findTaskById(id);
        if(taskEntity==null) return false;
        if(!taskEntity.isDeleted() ){
            taskEntity.setDeleted(true);
            taskDao.merge(taskEntity);
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
    public void deleteAllTasksByUser(String username){
        taskDao.deleteAllTasksByUser(username);
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