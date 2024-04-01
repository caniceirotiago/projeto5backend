package aor.paj.service.validator;

import aor.paj.dto.TaskDto;
import aor.paj.entity.TaskEntity;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * TaskValidator is responsible for ensuring task data integrity through various validation methods. It checks
 * task titles, descriptions, priorities, status, and date ranges for adherence to business rules. This class
 * is essential for maintaining consistent and valid task data throughout the application lifecycle.
 */

@ApplicationScoped
public class TaskValidator {
    public TaskValidator() {
    }

    public boolean validateTaskTitle(String taskName) {
        if(taskName == null) return true;
        return  taskName.length() >= 3 && taskName.length() <= 50;
    }

    public boolean validateTaskDescription(String taskDescription) {
        if(taskDescription == null) return true;
        return  taskDescription.length() >= 3 && taskDescription.length() <= 400;
    }
    public boolean validateTaskPriority(Integer taskPriority) {
        if(taskPriority == null) return true;
        return taskPriority >= 1 && taskPriority <= 3;
    }
    public boolean validateTaskStatus(Integer taskStatus) {
        if(taskStatus == null) return true;
        return  taskStatus == 100 || taskStatus == 200 || taskStatus == 300;
    }
    // If you want to add more statues tha validation should be updated
    public boolean isStartDateAfterEndDate(TaskDto task) {
        if(task.getStartDate() == null || task.getEndDate() == null) return true;
        return task.getStartDate().isBefore(task.getEndDate()) || task.getStartDate().isEqual(task.getEndDate());
    }

    public boolean isStartDateAfterEndDate(TaskEntity task) {
        if(task.getStartDate() == null || task.getEndDate() == null) return true;
        return task.getStartDate().isBefore(task.getEndDate()) || task.getStartDate().isEqual(task.getEndDate());
    }
    public boolean validateTask(TaskDto task) {

        return validateTaskTitle(task.getTitle()) &&
                validateTaskDescription(task.getDescription()) &&
                validateTaskPriority(task.getPriority()) &&
                validateTaskStatus(task.getStatus()) &&
                isStartDateAfterEndDate(task);
    }

    public boolean validateTask(TaskEntity task) {
        return validateTaskTitle(task.getTitle()) &&
                validateTaskDescription(task.getDescription()) &&
                validateTaskPriority(task.getPriority()) &&
                validateTaskStatus(task.getStatus()) &&
                isStartDateAfterEndDate(task);
    }

}
