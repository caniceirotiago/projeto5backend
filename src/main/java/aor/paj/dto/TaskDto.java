package aor.paj.dto;

import jakarta.ejb.EJB;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Task is a core entity representing a task in the system, including details such as title, description, priority,
 * status, and associated username. It also includes start and end dates, with logic to handle date parsing exceptions.
 * The class is designed with data validation annotations to ensure that essential fields are not null. The username and
 * ID are immutable post-creation, reflecting task ownership and identity.
 */

@XmlRootElement
public class TaskDto {
    private static final String STATUS_REGEX = "(100|200|300)";

    @XmlElement
    private LocalDate startDate;
    @XmlElement
    private LocalDate endDate;
    @XmlElement
    private int id;
    @NotNull
    @Size(min = 3, max = 50, message = "Title must be between 3 and 50 characters")
    @XmlElement
    private String title;
    @NotNull
    @Size(min = 3, max = 400, message = "Description must be between 3 and 400 characters")
    @XmlElement
    private String description;
    @NotNull
    @Size(min = 1, max = 3, message = "Priority must be between 1 and 3")
    @XmlElement
    private Integer priority;
    @NotNull
    @Pattern(regexp = STATUS_REGEX, message = "Status must be 100, 200, or 300")
    @XmlElement
    private Integer status;
    @NotNull
    @Size(min = 2, max = 25, message = "Username must be between 2 and 25 characters")
    @XmlElement
    private String username_author;
    @XmlElement
    private String category_type;
    @XmlElement
    private Boolean deleted;

    public TaskDto() {
    }


    public Boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getCategory_type() {
        return category_type;
    }

    public void setCategory_type(String category_type) {
        this.category_type = category_type;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        try{
            this.endDate = endDate;
        }catch (DateTimeParseException e){
            this.endDate = null;
        }
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        try{
            this.startDate = startDate;
        }catch (DateTimeParseException e){
            this.startDate = null;
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPriority() {
        return priority;
    }
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public  Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUsername_author() {
        return username_author;
    }

    public void setUsername_author(String username_author) {
        this.username_author = username_author;
    }
    @AssertTrue(message = "End date must be after start date")
    private boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            // Se uma das datas não estiver definida, a validação passa
            return true;
        }
        // Retorna true se a data de término for após a data de início
        return endDate.isAfter(startDate);
    }
    @Override
    public String toString() {
        return "TaskDto{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", username_author='" + username_author + '\'' +
                ", category_type='" + category_type + '\'' +
                '}';
    }
}