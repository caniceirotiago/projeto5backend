package aor.paj.bean;

import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.CategoryDto;
import aor.paj.dto.Statistics.*;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.exception.EntityValidationException;
import aor.paj.exception.UserConfirmationException;
import aor.paj.service.status.taskStatusManager;
import aor.paj.service.websocket.DashboardWebSocket;
import aor.paj.service.websocket.WebSocketMessage;
import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.IsoFields;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Stateless
public class StatisticsBean {
    private static final Logger LOGGER = LogManager.getLogger(StatisticsBean.class);
    @EJB
    UserDao userDao;
    @EJB
    TaskDao taskDao;
    @EJB
    CategoryBean categoryBean;
    private final Gson gson = new Gson();
    public int numberOfConfirmedUsers(){
        return userDao.getConfirmedUsers().size();
    }
    public int numberOfUnconfirmedUsers(){
        return userDao.getUnconfirmedUsers().size();
    }
    public double averageTasksPerUser(){
        return userDao.getAverageTasksPerUser();
    }
     public int numberOfTODOTasks() {
         return taskDao.getTasksByStatus(taskStatusManager.TODO);
     }
    public int numberOfDOINGTasks() {
        return taskDao.getTasksByStatus(taskStatusManager.DOING);
    }
    public int numberOfDONETasks() {
        return taskDao.getTasksByStatus(taskStatusManager.DONE);
    }
    public List<CategoryDto> getOrderCategoriesByNumberOfTasks() {
        List<CategoryDto> orderedList = categoryBean.convertCategoryEntitiesToCategoryDtos(taskDao.getCategoriesByNumberOfTasks());
        return orderedList;
    }
    public String calculateAverageCompletionTime() {
        List<TaskEntity> tasks = taskDao.findAllCompletedTasksWithTimestamps(taskStatusManager.DONE);
        long totalDuration = 0;
        for (TaskEntity task : tasks) {
            Duration duration = Duration.between(task.getDoingTimestamp(), task.getDoneTimestamp());
            totalDuration += duration.getSeconds();
        }
        Duration averageDuration = tasks.isEmpty() ? Duration.ZERO : Duration.ofSeconds(totalDuration / tasks.size());

        // Convert the total seconds into days, hours, and minutes
        long days = averageDuration.toDays();
        long hours = averageDuration.toHoursPart();
        long minutes = averageDuration.toMinutesPart();

        return String.format("%d days, %d hours, %d minutes", days, hours, minutes);
    }

    public HashMap<String, Integer> getNumberOfConfirmedUsersByMonth() {
        return userDao.getNumberOfConfirmedUsersByMonth();
    }
    public HashMap<String, Long> getNumberOfCompletedTasksByWeek() {
        List<LocalDateTime> doneTimestamps = taskDao.findAllDoneTimestamps();
        Map<String, Long> tasksByWeek = doneTimestamps.stream()
                .collect(Collectors.groupingBy(
                        timestamp -> timestamp.getYear() + "-W" +
                                String.format("%02d", timestamp.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)),
                        Collectors.counting()
                ));

        return new HashMap<>(tasksByWeek);
    }
    public DashboardDTO createDashboardDto() {
        UsersStatisticsDTO usersDTO = createUserStatisticsDTO();
        TasksStatisticsDTO tasksDTO = createTasksStatisticsDTO();
        CategoryStatisticsDTO categoryDTO = createCategoryStatisticsDTO();
        return new DashboardDTO(usersDTO, tasksDTO, categoryDTO);
    }


    public UsersStatisticsDTO createUserStatisticsDTO() {
        UsersStatisticsDTO dto = new UsersStatisticsDTO();
        dto.setTotalUsers(numberOfConfirmedUsers()+numberOfUnconfirmedUsers());
        dto.setConfirmedUsers(numberOfConfirmedUsers());
        dto.setUnconfirmedUsers(numberOfUnconfirmedUsers());
        dto.setAverageTasksPerUser(averageTasksPerUser());
        dto.setUsersPerMonth(getNumberOfConfirmedUsersByMonth());
        return dto;
    }
    public TasksStatisticsDTO createTasksStatisticsDTO() {
        TasksStatisticsDTO dto = new TasksStatisticsDTO();
        dto.setNumberOfTODO(numberOfTODOTasks());
        dto.setNumberOfDOING(numberOfDOINGTasks());
        dto.setNumberOfDONE(numberOfDONETasks());
        dto.setAverageCompletionTime(calculateAverageCompletionTime());
        dto.setTasksPerWeek(getNumberOfCompletedTasksByWeek());
        return dto;
    }
    public CategoryStatisticsDTO createCategoryStatisticsDTO() {
        return new CategoryStatisticsDTO(getOrderCategoriesByNumberOfTasks());
    }
    public void broadcastUserStatisticsUpdate() {
        UsersStatisticsDTO usersStats = createUserStatisticsDTO();
        String jsonUsersStats = gson.toJson(new WebSocketMessage("userStatistics", usersStats));
        DashboardWebSocket.broadcast(jsonUsersStats);
        System.out.println("Broadcasting user statistics");
    }

    public void broadcastTaskStatisticsUpdate() {
        TasksStatisticsDTO tasksStats = createTasksStatisticsDTO();
        String jsonTasksStats = gson.toJson(new WebSocketMessage("taskStatistics", tasksStats));
        DashboardWebSocket.broadcast(jsonTasksStats);
    }

    public void broadcastCategoryStatisticsUpdate() {
        CategoryStatisticsDTO categoriesStats = createCategoryStatisticsDTO();
        String jsonCategoriesStats = gson.toJson(new WebSocketMessage("categoryStatistics", categoriesStats));
        DashboardWebSocket.broadcast(jsonCategoriesStats);
    }
    public IndividualUserStatisticsDto createIndividualUserStatisticsDTO(String username) throws UserConfirmationException {
        UserEntity user = userDao.findUserByUsername(username);
        if (user == null) {
            LOGGER.warn("Invalid username: " + username + " for individual user statistics");
            throw new UserConfirmationException("Invalid username");
        }
        IndividualUserStatisticsDto dto = new IndividualUserStatisticsDto();
        dto.setDoingTasks(taskDao.getNTasksByStatusAndUser(taskStatusManager.DOING, username));
        dto.setDoneTasks(taskDao.getNTasksByStatusAndUser(taskStatusManager.DONE, username));
        dto.setTodoTasks(taskDao.getNTasksByStatusAndUser(taskStatusManager.TODO, username));
        dto.setUsername(username);
        return dto;
    }
}
