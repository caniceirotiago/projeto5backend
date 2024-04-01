package aor.paj.entity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table (name="task")
@NamedQuery(name="Task.findTaskById", query="SELECT t FROM TaskEntity t " +
        "WHERE t.id=:id")
@NamedQuery(name="Task.findTasksByUser", query="SELECT t FROM TaskEntity t " +
        "WHERE t.user.username=:username " +
        "AND t.deleted=:deleted " +
        "ORDER BY t.priority DESC, t.startDate DESC, t.endDate DESC")
@NamedQuery(name="Task.findTasksByCategoryType", query="SELECT t FROM TaskEntity t " +
        "WHERE t.category.type = :categoryType " +
        "AND t.deleted=:deleted " +
        "ORDER BY t.priority DESC, t.startDate DESC, t.endDate DESC")
@NamedQuery(name="Task.deleteTasksBId", query="DELETE FROM TaskEntity t " +
        "WHERE t.id = :id")
@NamedQuery(name="Task.findTasksByDeleted", query="SELECT t FROM TaskEntity t " +
        "WHERE t.deleted=:deleted " +
        "ORDER BY t.priority DESC, t.startDate DESC, t.endDate DESC")
@NamedQuery(name="Task.findTasksByCategoryAndUser", query="SELECT t FROM TaskEntity t " +
        "WHERE t.user.username = :username " +
        "AND t.category.type = :categoryType AND t.deleted=:deleted   " +
        "ORDER BY t.priority DESC, t.startDate DESC, t.endDate DESC")
@NamedQuery(name="Task.deleteAllTasksByUser", query="DELETE FROM TaskEntity t " +
        "WHERE t.user.username = :username")

public class TaskEntity implements Serializable {
    private static final long longSerialVersionID=1L;
    @Id
    @GeneratedValue (strategy =  GenerationType.IDENTITY)
    @Column(name="id", nullable = false,unique = true,updatable = false)
    private int id;
    @Column (name="title", nullable = false,unique = false,updatable = true)
    private String title;
    @Column (name="description", nullable = false,unique = false,updatable = true, length = 400)
    private String description;
    @ManyToOne
    //@Column (name="user_username", nullable = false, unique = false,updatable = false)
    private UserEntity user;
    @Column (name="priority", nullable = false,unique = false,updatable = true)
    private int priority;
    @Column (name="status", nullable = false, unique = false, updatable = true)
    private int status;
    @Column (name="start_date", nullable = true, unique = false,updatable = true)
    private LocalDate startDate;
    @Column (name="end_date", nullable = true, unique = false,updatable = true)
    private LocalDate endDate;
    @ManyToOne
    //@Column (name="category", nullable = false, unique = false, updatable = true)
    private CategoryEntity category;
    @Column(name="deleted", nullable = false,unique = false,updatable = true)
    private boolean deleted;

    public TaskEntity() {}

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "TaskEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", user=" + user +
                ", priority=" + priority +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", category=" + category +
                ", deleted=" + deleted +
                '}';
    }
}
