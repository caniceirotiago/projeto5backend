package aor.paj.dao;

import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class TaskDao extends AbstractDao<TaskEntity> {
	private static final long serialVersionUID = 1L;
	public TaskDao() {
		super(TaskEntity.class);
	}
	public TaskEntity findTaskById(int id) {
		try {
			return (TaskEntity) em.createNamedQuery("Task.findTaskById").setParameter("id", id)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}

	}
	public List<TaskEntity> getTasksByUser(String username) {
		try {
			return em.createNamedQuery("Task.findAllTasksByUsername", TaskEntity.class)
					.setParameter("username", username)
					.getResultList();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	public List<TaskEntity> getTasksByFilter(boolean deleted, String username, String category_type) {
		List<TaskEntity> tasks = new ArrayList<>();
		try {
			if ( username != null && category_type == null){
				tasks = em.createNamedQuery("Task.findTasksByUser", TaskEntity.class)
						.setParameter("username", username)
						.setParameter("deleted", deleted)
						.getResultList();
			} else if (category_type != null && username == null){
				tasks = em.createNamedQuery("Task.findTasksByCategoryType", TaskEntity.class)
						.setParameter("categoryType", category_type)
						.setParameter("deleted", deleted)
						.getResultList();
			} else if (category_type != null && username != null){
				tasks = em.createNamedQuery("Task.findTasksByCategoryAndUser", TaskEntity.class)
						.setParameter("categoryType", category_type)
						.setParameter("username", username)
						.setParameter("deleted", deleted)
						.getResultList();
			}
			else {
				tasks = em.createNamedQuery("Task.findTasksByDeleted", TaskEntity.class)
						.setParameter("deleted", deleted)
						.getResultList();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return tasks;
	}


	public void deleteTask(int id){
		try{
			em.createNamedQuery("Task.deleteTasksBId").setParameter("id",id).executeUpdate();
		}catch (Exception e){

		}
	}
	public void deleteAllTasksByUser(String username){
		try{
			em.createNamedQuery("Task.deleteAllTasksByUser").setParameter("username",username).executeUpdate();
		}catch (Exception e){

		}
	}
	public int getTasksByStatus(int status){
		try {
			return em.createNamedQuery("Task.findTasksByStatus", TaskEntity.class)
					.setParameter("status", status)
					.getResultList().size();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return 0;
	}
	public List<CategoryEntity> getCategoriesByNumberOfTasks(){
		try {
			return em.createNamedQuery("Task.findCategoriesByNumberOfTasks", CategoryEntity.class)
					.getResultList();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	public List<TaskEntity> findAllCompletedTasksWithTimestamps(int doneStatus) {
		return em.createNamedQuery("Task.findAllCompletedTasksWithTimestamps", TaskEntity.class)
				.setParameter("doneStatus", doneStatus)
				.getResultList();
	}
	public List<LocalDateTime> findAllDoneTimestamps() {
		return em.createNamedQuery("Task.findAllDoneTimestamps", LocalDateTime.class)
				.getResultList();
	}
	public int getNTasksByStatusAndUser(int status, String username) {
		Query query = em.createNamedQuery("Task.findNOfTasksByStatusAndUser")
				.setParameter("status", status)
				.setParameter("username", username);
		return ((Number) query.getSingleResult()).intValue();
	}
}
