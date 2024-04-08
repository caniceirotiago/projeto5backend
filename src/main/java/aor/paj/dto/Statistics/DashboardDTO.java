package aor.paj.dto.Statistics;

public class DashboardDTO {
    private UsersStatisticsDTO usersStatistics;
    private TasksStatisticsDTO tasksStatistics;
    private CategoryStatisticsDTO categoryStatistics;


    public DashboardDTO() {}

    public DashboardDTO(UsersStatisticsDTO usersStatistics , TasksStatisticsDTO tasksStatistics, CategoryStatisticsDTO categoryStatistics) {
        this.usersStatistics = usersStatistics;
        this.tasksStatistics = tasksStatistics;
        this.categoryStatistics = categoryStatistics;
    }

    public TasksStatisticsDTO getTasksStatistics() {
        return tasksStatistics;
    }

    public void setTasksStatistics(TasksStatisticsDTO tasksStatistics) {
        this.tasksStatistics = tasksStatistics;
    }

    public UsersStatisticsDTO getUsersStatistics() {
        return usersStatistics;
    }

    public void setUsersStatistics(UsersStatisticsDTO usersStatistics) {
        this.usersStatistics = usersStatistics;
    }

    public CategoryStatisticsDTO getCategoryStatistics() {
        return categoryStatistics;
    }

    public void setCategoryStatistics(CategoryStatisticsDTO categoryStatistics) {
        this.categoryStatistics = categoryStatistics;
    }
}
