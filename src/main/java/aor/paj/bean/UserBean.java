package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.LoginDto;
import aor.paj.dto.User;
import aor.paj.dto.UserInfoCard;
import aor.paj.dto.UserWithNoPassword;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.exception.DuplicateUserException;
import aor.paj.service.status.userRoleManager;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import util.HashUtil;




import java.io.*;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * UserBean is a managed bean responsible for managing user data within the application. It provides functionality
 * to read and write user information from and to a JSON file, 'allUser.json'. This bean supports operations such as
 * user registration, login verification, retrieving user information by username, and converting User objects to
 * UserWithNoPassword objects for security purposes. It also includes methods to check user existence, update user
 * information, and manage user passwords. UserBean ensures that user data is persistently stored and accessible
 * throughout the application lifecycle.
 */

@Stateless
public class UserBean implements Serializable {
    @EJB
    UserDao userDao;
    @EJB
    TaskDao taskDao;
    @EJB
    CategoryDao categoryDao;
    @EJB
    ConfigurationBean configBean;

    private UserEntity convertUserDtotoUserEntity(User user){
        UserEntity userEntity = new UserEntity();
        if(userEntity != null){
            userEntity.setUsername(user.getUsername());
            userEntity.setPassword(user.getPassword());
            userEntity.setEmail(user.getEmail());
            userEntity.setFirstName(user.getFirstName());
            userEntity.setLastName(user.getLastName());
            userEntity.setPhoneNumber(user.getPhoneNumber());
            userEntity.setToken(null);
            userEntity.setPhotoURL(user.getPhotoURL());
            userEntity.setRole(user.getRole());
            System.out.println(userEntity);
            System.out.println(user);
            return userEntity;
        }
        return null;
    }
    public boolean register(User user){
        if(user == null) return false;
        if (checkIfEmailExists(user.getEmail())) {
            throw new DuplicateUserException("Email already exists");

        } else if(checkIfUsernameExists(user.getUsername())){
            throw new DuplicateUserException("Username already exists");
        }
        if (user.getRole() == null) {
            user.setRole(userRoleManager.DEVELOPER);
        }
        String hashedPassword = HashUtil.toSHA256(user.getPassword());
        user.setPassword(hashedPassword);
        try {
            userDao.persist(convertUserDtotoUserEntity(user));
            return true;
        } catch (NoResultException e ) {
            return false;
        }
    }
    public boolean checkIfUsernameExists(String username){
        if(username !=null){
            return userDao.checkIfUsernameExists(username);
        }
        return false;
    }
    public boolean checkIfEmailExists(String email){
        if(email !=null){
            return userDao.checkIfEmailExists(email);
        }
        return false;
    }

    public String login(LoginDto user){
        UserEntity userEntity = userDao.findUserByUsername(user.getUsername());
        if(userEntity !=null){
            if(!userEntity.getDeleted()){
                String hashedPassword = HashUtil.toSHA256(user.getPassword());
                if (userEntity != null){
                    if (userEntity.getPassword().equals(hashedPassword)){
                        String token = generateNewToken();
                        userEntity.setToken(token);
                        updateLastActivityTimestamp(userEntity);
                        return token;
                    }
                }
            }
        }
        return null;
    }
    public boolean tokenValidator(String token) {
        UserEntity user = userDao.findUserByToken(token);
        if (user != null) {
            Instant now = Instant.now();
            long tokenValidityPeriodInSeconds = Long.parseLong(configBean.findConfigValueByKey("sessionTimeout"));
            Instant tokenExpiration = user.getLastActivityTimestamp().plusSeconds(tokenValidityPeriodInSeconds);
            if (now.isBefore(tokenExpiration)) {
                updateLastActivityTimestamp(user);
                return true;
            } else {
                logout(user.getToken());
                return false;
            }
        }
        return false;
    }

    private void updateLastActivityTimestamp(UserEntity user) {
        user.setLastActivityTimestamp(Instant.now());
        userDao.updateUser(user); // Atualize o usuário no banco de dados.
    }
    public UserEntity getUserByToken(String token){
        UserEntity user=userDao.findUserByToken(token);
        if(user!=null) return user;
        else return null;
    }
    public UserEntity getUserByUsername(String username){
       return userDao.findUserByUsername(username);
    }

    private String generateNewToken() {
        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
    public String getRoleByToken(String token){
        UserEntity user = userDao.findUserByToken(token);
        return user.getRole();
    }
    public List<String> getUserBasicInfo(String token){
        UserEntity user = userDao.findUserByToken(token);
        List<String> userBasicInfo = new ArrayList<>();
        userBasicInfo.add(user.getPhotoURL());
        userBasicInfo.add(user.getFirstName());
        userBasicInfo.add(user.getRole());
        userBasicInfo.add(user.getUsername());
        return userBasicInfo;
    }
    public void createDefaultUsersIfNotExistent(){
        UserEntity productOwner = userDao.findUserByUsername("admin");
        UserEntity scrumMaster =userDao.findUserByUsername("scrumMasterTest");
        UserEntity developer =userDao.findUserByUsername("developerTest");
        UserEntity deletedTasks =userDao.findUserByUsername("deletedUserTasks");
        String hashedAdminPassword = HashUtil.toSHA256("admin");
        if(productOwner == null){
            userDao.persist(new UserEntity("admin", hashedAdminPassword,"admin@admin.com",
                    "admin", "admin", "admin",
                    "https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png",
                    "admin", userRoleManager.PRODUCT_OWNER,false));
        }
        if(scrumMaster == null){
            userDao.persist(new UserEntity("scrumMasterTest", hashedAdminPassword,"srummaster@admin.com",
                    "scrumMasterTest", "test", "123123123",
                    "https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png",
                    "scrum", userRoleManager.SCRUM_MASTER,false));
        }
        if(developer == null){
            userDao.persist(new UserEntity("developerTest", hashedAdminPassword,"developer@admin.com",
                    "DeveloperTest", "test", "123123123",
                    "https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png",
                    "devel", userRoleManager.DEVELOPER,false));
        }
        if(deletedTasks == null){
            userDao.persist(new UserEntity("deletedUserTasks", hashedAdminPassword,"deleted@admin.com",
                    "deleted", "tasks", "deleted",
                    "https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png",
                    "deleted", userRoleManager.PRODUCT_OWNER,false));
        }
    }

    public UserWithNoPassword convertUserEntityToUserWithNoPassword(UserEntity userEntity){
      return new UserWithNoPassword(userEntity.getUsername(),
              userEntity.getPhoneNumber(), // Corrigido: phoneNumber antes de email
              userEntity.getEmail(),
              userEntity.getFirstName(),
              userEntity.getLastName(),
              userEntity.getPhotoURL(),
              userEntity.getRole(),
              userEntity.getDeleted());
    }
    public boolean updateUser(String token, User updatedUser) {
        UserEntity user = getUserByToken(token);
        if(user != null){
            if(updatedUser.getPhoneNumber() != null) user.setPhoneNumber(updatedUser.getPhoneNumber());
            if(updatedUser.getEmail() != null) user.setEmail(updatedUser.getEmail());
            if(updatedUser.getFirstName() != null) user.setFirstName(updatedUser.getFirstName());
            if(updatedUser.getLastName() != null) user.setLastName(updatedUser.getLastName());
            if(updatedUser.getPhotoURL() != null) user.setPhotoURL(updatedUser.getPhotoURL());
            user.setDeleted(updatedUser.isDeleted());
            return userDao.updateUser(user);
        }
      return false;
    }
    public boolean updateUserByUsername(String userToChangeUsername, User updatedUser){
        UserEntity userToChange = getUserByUsername(userToChangeUsername);
        if(userToChange != null){
            if(updatedUser != null){
                if(updatedUser.getPhoneNumber() != null)userToChange.setPhoneNumber(updatedUser.getPhoneNumber());
                if(updatedUser.getEmail() != null) userToChange.setEmail(updatedUser.getEmail());
                if(updatedUser.getFirstName() != null) userToChange.setFirstName(updatedUser.getFirstName());
                if(updatedUser.getLastName() != null) userToChange.setLastName(updatedUser.getLastName());
                if(updatedUser.getPhotoURL() != null) userToChange.setPhotoURL(updatedUser.getPhotoURL());
                if(updatedUser.getRole() != null) userToChange.setRole(updatedUser.getRole());
                userToChange.setDeleted(updatedUser.isDeleted());
                if(updatedUser.isDeleted())userToChange.setToken(null);
                return userDao.updateUser(userToChange);
            }
        }
        return false;
    }

    public boolean oldPasswordConfirmation(String token, String oldPassword, String newPassword){
        UserEntity user = getUserByToken(token);
        if(user != null){
            String hashedOldPassword = HashUtil.toSHA256(oldPassword);

            if(user.getPassword().equals(hashedOldPassword) && !user.getPassword().equals(newPassword)){
                return true;
            }
        }
        return false;
    }

    public boolean updatePassWord(String token, String newPassword){
        UserEntity user = getUserByToken(token);
        if(user != null){
            String hashedNewPassword = HashUtil.toSHA256(newPassword);

            user.setPassword(hashedNewPassword);
            return userDao.updateUser(user);
        }
        return false;
    }


    public boolean deleteUserTemporarily(String username){
        UserEntity userEntity = userDao.findUserByUsername(username);
        if(!userEntity.getDeleted()) {
            userEntity.setDeleted(true);
            userDao.merge(userEntity);
            return true;
        }
        else return false;
    }
    public void transferTasks(String username){
        UserEntity admin=getUserByUsername("deletedUserTasks");
        List<TaskEntity> tasks=taskDao.getTasksByFilter(false, username, null);
        for(TaskEntity task:tasks){
            task.setUser(admin);
            taskDao.merge(task);
        }
    }
    public void transferCategories(String username){
        UserEntity userEntity=getUserByUsername(username);
        UserEntity admin=getUserByUsername("deletedUserTasks");
        ArrayList<CategoryEntity> categoryEntities=categoryDao.getCategoriesByUser(userEntity);
        for(CategoryEntity category:categoryEntities){
            category.setAuthor(admin);
            categoryDao.merge(category);
        }
    }
    public UserInfoCard convertUserEntityToUserInfoCard(UserEntity userEntity){
        return new UserInfoCard(userEntity.getUsername(),
                userEntity.getFirstName(),
                userEntity.getPhotoURL(),
                userEntity.getDeleted(),
                userEntity.getRole());
    }
    public List<UserInfoCard> getAllUsersInfo(){
        List<UserEntity> userEntities = userDao.findAllUsers();
        List<UserInfoCard> users = new ArrayList<>();
        if(userEntities != null){
            for(UserEntity userEntity : userEntities){
                users.add(convertUserEntityToUserInfoCard(userEntity));
            }
        }
        return users;
    }
    public boolean deleteUserPermanently(String username){
        return userDao.deleteUser(username);
    }
    public void logout(String token){
        UserEntity user = getUserByToken(token);
        if(user != null){
            user.setToken(null);
            userDao.updateUser(user);
        }
    }
    public List<UserInfoCard> getUsersWithTasks(){
        List<UserEntity> userEntities = userDao.findUsersWithNonDeletedTasks();
        List<UserInfoCard> users = new ArrayList<>();
        if(userEntities != null){
            for(UserEntity userEntity : userEntities){
                users.add(convertUserEntityToUserInfoCard(userEntity));
            }
        }
        return users;
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}