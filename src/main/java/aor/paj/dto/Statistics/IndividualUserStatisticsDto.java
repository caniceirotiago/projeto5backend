package aor.paj.dto.Statistics;

public class IndividualUserStatisticsDto {
    private String username;
    private int todoTasks;
    private int doingTasks;
    private int doneTasks;

    public IndividualUserStatisticsDto() {

    }

    IndividualUserStatisticsDto(String username, int todoTasks, int doingTasks, int doneTasks) {
        this.username = username;
        this.todoTasks = todoTasks;
        this.doingTasks = doingTasks;
        this.doneTasks = doneTasks;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTodoTasks() {
        return todoTasks;
    }

    public void setTodoTasks(int todoTasks) {
        this.todoTasks = todoTasks;
    }

    public int getDoingTasks() {
        return doingTasks;
    }

    public void setDoingTasks(int doingTasks) {
        this.doingTasks = doingTasks;
    }

    public int getDoneTasks() {
        return doneTasks;
    }

    public void setDoneTasks(int doneTasks) {
        this.doneTasks = doneTasks;
    }

    @Override
    public String toString() {
        return "IndividualUserStatisticsDto{" +
                "username='" + username + '\'' +
                ", todoTasks=" + todoTasks +
                ", doingTasks=" + doingTasks +
                ", doneTasks=" + doneTasks +
                '}';
    }
}
