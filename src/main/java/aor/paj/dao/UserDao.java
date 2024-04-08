package aor.paj.dao;

import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.time.Month;
import java.util.HashMap;
import java.util.List;

@Stateless
public class UserDao extends AbstractDao<UserEntity> {
	private static final long serialVersionUID = 1L;
	public UserDao() {
		super(UserEntity.class);
	}
	@PersistenceContext
	private EntityManager entityManager;
	public boolean checkIfEmailExists(String email) {
		Query query = entityManager.createNamedQuery("User.checkIfEmailExists");
		Long count = (Long) query.setParameter("email", email).getSingleResult();
		return count > 0;
	}

	public boolean checkIfUsernameExists(String username) {
		Query query = entityManager.createNamedQuery("User.checkIfUsernameExists");
		Long count = (Long) query.setParameter("username", username).getSingleResult();
		return count > 0;
	}
	public UserEntity findUserByToken(String token) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByToken").setParameter("token", token)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	public UserEntity findUserByConfirmationToken(String token) {
		try {
			Query query = entityManager.createNamedQuery("User.findUserByConfirmationToken", UserEntity.class);
			query.setParameter("confirmationToken", token);
			return (UserEntity) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	public UserEntity findUserByResetPasswordToken(String resetPasswordToken) {
		try {
			return entityManager.createNamedQuery("User.findByResetPasswordToken", UserEntity.class)
					.setParameter("resetPasswordToken", resetPasswordToken)
					.getSingleResult();
		} catch (NoResultException e) {
			return null; // Token inválido ou expirado
		}
	}

	public boolean updateUser(UserEntity user) {
		try {
			System.out.println("logout" + user);
			em.merge(user);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public List<UserEntity> findAllUsers() {
		try {
			return (List<UserEntity>) em.createNamedQuery("User.findAllUsers").getResultList();

		} catch (NoResultException e) {
			return null;
		}
	}
	public UserEntity findUserByEmail(String email) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByEmail").setParameter("email", email)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}
	public UserEntity findUserByUsername(String username) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByUsername").setParameter("username", username)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}
	public boolean deleteUser(String username){
		try{
			em.createNamedQuery("User.deleteUserById").setParameter("username", username).executeUpdate();
			return true;
		}catch (Exception e){
			return false;
		}
	}
	public List<UserEntity> findUsersWithNonDeletedTasks() {
		try {
			return (List<UserEntity>) em.createNamedQuery("User.findUsersWithNonDeletedTasks").getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}
	public List<UserEntity> getConfirmedUsers() {
		try {
			return (List<UserEntity>) em.createNamedQuery("User.getConfirmedUsers").getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}
	public List<UserEntity> getUnconfirmedUsers() {
		try {
			return (List<UserEntity>) em.createNamedQuery("User.getUnconfirmedUsers").getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}
	public double getAverageTasksPerUser() {
		try {
			return (double) em.createNamedQuery("User.getAverageTasksPerUser").getSingleResult();
		} catch (NoResultException e) {
			return 0;
		}
	}
	public HashMap<String, Integer> getNumberOfConfirmedUsersByMonth() {
		HashMap<String, Integer> map = new HashMap<>();
		List<Object[]> results = em.createNamedQuery("User.getNumberOfConfirmedUsersByMonth").getResultList();
		for (Object[] result : results) {
			int year = ((Number) result[0]).intValue(); // Cast seguro, EXTRACT retorna um Number
			int month = ((Number) result[1]).intValue();
			Long count = (Long) result[2];
			String yearMonthKey = year + "-" + String.format("%02d", month); // Formata a chave como "2023-01"
			map.put(yearMonthKey, count.intValue());
		}
		return map;
	}

}
