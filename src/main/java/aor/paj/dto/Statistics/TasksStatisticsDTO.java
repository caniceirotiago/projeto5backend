package aor.paj.dto.Statistics;

import java.time.Duration;
import java.util.HashMap;

public class TasksStatisticsDTO {
    private int numberOfTODO;
    private int numberOfDOING;
    private int numberOfDONE;
    private String averageCompletionTime;
    private HashMap<String , Long> tasksPerWeek;

    public TasksStatisticsDTO() {}

    // Getters and Setters
    public int getNumberOfTODO() {
        return numberOfTODO;
    }

    public void setNumberOfTODO(int numberOfTODO) {
        this.numberOfTODO = numberOfTODO;
    }

    public int getNumberOfDOING() {
        return numberOfDOING;
    }

    public void setNumberOfDOING(int numberOfDOING) {
        this.numberOfDOING = numberOfDOING;
    }

    public int getNumberOfDONE() {
        return numberOfDONE;
    }

    public void setNumberOfDONE(int numberOfDONE) {
        this.numberOfDONE = numberOfDONE;
    }

    public String getAverageCompletionTime() {
        return averageCompletionTime;
    }

    public void setAverageCompletionTime(String averageCompletionTime) {
        this.averageCompletionTime = averageCompletionTime;
    }

    public HashMap<String, Long> getTasksPerWeek() {
        return tasksPerWeek;
    }

    public void setTasksPerWeek(HashMap<String, Long> tasksPerWeek) {
        this.tasksPerWeek = tasksPerWeek;
    }
}
