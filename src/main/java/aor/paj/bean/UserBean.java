package aor.paj.bean;

import aor.paj.dao.*;
import aor.paj.dto.*;
import aor.paj.entity.*;
import aor.paj.exception.*;
import aor.paj.service.websocket.GlobalWebSocket;
import jakarta.ejb.Schedule;
import util.EmailService;
import aor.paj.service.status.userRoleManager;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import org.apache.logging.log4j.*;
import util.HashUtil;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

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
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(UserBean.class);

    @EJB
    UserDao userDao;
    @EJB
    TaskDao taskDao;
    @EJB
    CategoryDao categoryDao;
    @EJB
    ConfigurationBean configBean;
    @EJB
    EmailService emailService;
    @EJB
    StatisticsBean statistiscsBean;
    @EJB
    MessageDao messageDao;
    @EJB
    NotificationDao notificationDao;

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
            userEntity.setDeleted(false);
            userEntity.setConfirmed(false);
            userEntity.setConfirmationToken(user.getConfirmationToken());

            return userEntity;
        }
        return null;
    }
    public boolean register(User user) throws DuplicateUserException, UnknownHostException {
        if(user == null) return false;
        if (checkIfEmailExists(user.getEmail())) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to register with existing email: " + user.getEmail());
            throw new DuplicateUserException("Email already exists");
        }
        if(checkIfUsernameExists(user.getUsername())){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to register with existing username: " + user.getUsername());
            throw new DuplicateUserException("Username already exists");
        }

        if (user.getRole() == null) {
            user.setRole(userRoleManager.DEVELOPER);
        }
        String hashedPassword = HashUtil.toSHA256(user.getPassword());
        user.setPassword(hashedPassword);
        try {
            String confirmationToken = UUID.randomUUID().toString();
            user.setConfirmationToken(confirmationToken);
            user.setConfirmed(false);
            emailService.sendConfirmationEmail(user.getEmail(), confirmationToken);

            userDao.persist(convertUserDtotoUserEntity(user));
            statistiscsBean.broadcastUserStatisticsUpdate();
            return true;
        } catch (NoResultException e ) {
            LOGGER.error(InetAddress.getLocalHost().getHostAddress() + " - Error while persisting user at: " + e.getMessage());
            return false;
        }
    }

    public void confirmUser(String token) throws UserConfirmationException, UnknownHostException {
        if(token == null) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to confirm user with null token");
            throw new UserConfirmationException("Invalid token");
        }
        UserEntity user = userDao.findUserByConfirmationToken(token);
        if(user == null){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to confirm user with invalid token at: " + token);
            throw new UserConfirmationException("Invalid token");
        }
        user.setConfirmationToken(null);
        user.setConfirmed(true);
        user.setConfirmationTimestamp(Instant.now());
        userDao.updateUser(user);
        statistiscsBean.broadcastUserStatisticsUpdate();
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

    public TokenDto login(LoginDto user) throws InvalidLoginException, UnknownHostException {
        UserEntity userEntity = userDao.findUserByUsername(user.getUsername());
        if(userEntity !=null){
            if (userEntity.getDeleted()) {
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " - Attempt to login with deleted user at: " + user.getUsername());
                throw new InvalidLoginException("User is deleted - contact the administrator");
            }
            if(!userEntity.isConfirmed()){
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to login with unconfirmed user at: " + user.getUsername());
                throw new InvalidLoginException("User is not confirmed - check your email for confirmation link or resend confirmation email");
            }
            String hashedPassword = HashUtil.toSHA256(user.getPassword());
            if (!userEntity.getPassword().equals(hashedPassword)){
                LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to login with invalid password at: " + user.getUsername());
                throw new InvalidLoginException("Invalid Credentials");
            }
            String token = generateNewToken();
            userEntity.setToken(token);
            updateLastActivityTimestamp(userEntity);
            return new TokenDto(token);
        }
        LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to login with invalid username at: " + user.getUsername());
        throw new InvalidLoginException("Invalid Credentials");
    }
    public boolean tokenValidator(String token) throws UserNotFoundException, UnknownHostException {

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
        userDao.updateUser(user);
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
    public InitialInformationDto getUserBasicInfo(String token) throws UserNotFoundException, UnknownHostException {
        UserEntity user = userDao.findUserByToken(token);
        if(user == null) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " Attempt to get user basic info with invalid token at: " + token);
            throw new UserNotFoundException("Invalid Credentials");

        }
        return new InitialInformationDto(user.getPhotoURL(), user.getFirstName(), user.getRole(), user.getUsername());
    }
    public UserWithNoPassword getUserWithNoPasswordByUsername(String username) throws UserNotFoundException, UnknownHostException {
        UserEntity userEntity = getUserByUsername(username);
        if(userEntity == null) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " Attempt to get user with invalid username at: " + username);
            throw new UserNotFoundException("User not found");

        }
        return convertUserEntityToUserWithNoPassword(userEntity);
    }
    public void createDefaultUsersIfNotExistent() throws DatabaseOperationException {
        UserEntity productOwner = userDao.findUserByUsername("admin");
        UserEntity scrumMaster =userDao.findUserByUsername("scrumMasterTest");
        UserEntity developer =userDao.findUserByUsername("developerTest");
        UserEntity deletedTasks =userDao.findUserByUsername("deletedUserTasks");
        String hashedAdminPassword = HashUtil.toSHA256("admin");
        if(productOwner == null){
            userDao.persist(new UserEntity("admin", hashedAdminPassword,"admin@admin.com",
                    "admin", "admin", "admin",
                    "https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png",
                    "admin", userRoleManager.PRODUCT_OWNER,false, true, "admin"));
        }
        if(scrumMaster == null){
            userDao.persist(new UserEntity("scrumMasterTest", hashedAdminPassword,"srummaster@admin.com",
                    "scrumMasterTest", "test", "123123123",
                    "https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png",
                    "scrum", userRoleManager.SCRUM_MASTER,false, true, "scrumMasterTest"));
        }
        if(developer == null){
            userDao.persist(new UserEntity("developerTest", hashedAdminPassword,"developer@admin.com",
                    "DeveloperTest", "test", "123123123",
                    "https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png",
                    "devel", userRoleManager.DEVELOPER,false, true, "developerTest"));
        }
        if(deletedTasks == null){
            userDao.persist(new UserEntity("deletedUserTasks", hashedAdminPassword,"deleted@admin.com",
                    "deleted", "tasks", "deleted",
                    "https://icons.veryicon.com/png/o/miscellaneous/yuanql/icon-admin.png",
                    "deleted", userRoleManager.PRODUCT_OWNER,false, true, "deletedUserTasks"));
        }
    }

    public UserWithNoPassword convertUserEntityToUserWithNoPassword(UserEntity userEntity){
      return new UserWithNoPassword(userEntity.getUsername(),
              userEntity.getPhoneNumber(),
              userEntity.getEmail(),
              userEntity.getFirstName(),
              userEntity.getLastName(),
              userEntity.getPhotoURL(),
              userEntity.getRole(),
              userEntity.getDeleted());
    }
    public void updateUser(String token, UserUpdateDTO updatedUser) throws UserNotFoundException, UnknownHostException {
        UserEntity user = getUserByToken(token);
        if(user == null){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " Attempt to update user with invalid token at: " + token);
            throw new UserNotFoundException("User not found");
        }
        if(updatedUser.getPhoneNumber() != null) user.setPhoneNumber(updatedUser.getPhoneNumber());
        if(updatedUser.getFirstName() != null) user.setFirstName(updatedUser.getFirstName());
        if(updatedUser.getLastName() != null) user.setLastName(updatedUser.getLastName());
        if(updatedUser.getPhotoURL() != null) user.setPhotoURL(updatedUser.getPhotoURL());
        if(updatedUser.isDeleted() != null)user.setDeleted(updatedUser.isDeleted());
        userDao.updateUser(user);
    }
    public void updateUserByUsername(String userToChangeUsername, UserUpdateDTO updatedUser) throws UserNotFoundException, UnknownHostException {
        UserEntity userToChange = getUserByUsername(userToChangeUsername);
        if(userToChange == null){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " Attempt to update user with invalid username at: " + userToChangeUsername);
            throw new UserNotFoundException("User not found");
        }
        if(updatedUser.getPhoneNumber() != null)userToChange.setPhoneNumber(updatedUser.getPhoneNumber());
        if(updatedUser.getFirstName() != null) userToChange.setFirstName(updatedUser.getFirstName());
        if(updatedUser.getLastName() != null) userToChange.setLastName(updatedUser.getLastName());
        if(updatedUser.getPhotoURL() != null) userToChange.setPhotoURL(updatedUser.getPhotoURL());
        if(updatedUser.getRole() != null) userToChange.setRole(updatedUser.getRole());
        userToChange.setDeleted(updatedUser.isDeleted());
        if(updatedUser.isDeleted())userToChange.setToken(null);
        userDao.updateUser(userToChange);
    }

    public boolean oldPasswordConfirmation(String token, String oldPassword, String newPassword){
        UserEntity user = getUserByToken(token);
        if(user != null){
            String hashedOldPassword = HashUtil.toSHA256(oldPassword);
            String hashedNewPassword = HashUtil.toSHA256(newPassword);
            if(user.getPassword().equals(hashedOldPassword) && !user.getPassword().equals(hashedNewPassword)){
                return true;
            }
        }
        return false;
    }

    public void updatePassWord(String token, String newPassword, String oldPassword) throws InvalidPasswordRequestException, UnknownHostException {
        UserEntity user = getUserByToken(token);
        if(user == null){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + " Attempt to update password with invalid token at: " + token);
            throw new InvalidPasswordRequestException("Invalid token");
        }
        if(!oldPasswordConfirmation(token, oldPassword, newPassword)){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to update password with invalid old password or password should not be the same at: " + token);
            throw new InvalidPasswordRequestException("Invalid old password or password should not be the same");
        }
        String hashedNewPassword = HashUtil.toSHA256(newPassword);
        user.setPassword(hashedNewPassword);
        userDao.updateUser(user);
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
    public void requestPasswordReset(String email) throws InvalidPasswordRequestException, UnknownHostException {
        UserEntity user = userDao.findUserByEmail(email);
        if (user == null) {
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to reset password with invalid email at: " + email);
            throw new InvalidPasswordRequestException("Not found user with this email");
        }
        if(user.getResetPasswordTokenExpiry() != null && user.getResetPasswordTokenExpiry().isAfter(Instant.now())){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to reset password with token not expired at: " + email);
            throw new InvalidPasswordRequestException("You already requested a password reset, please check your email, wait" +
                    " 30 minutes and try again, or contact the administrator");
        }
        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordTokenExpiry(Instant.now().plus(30, ChronoUnit.MINUTES));
        userDao.updateUser(user);
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }
    public  void resetPassword(String token, String newPassword) throws InvalidPasswordRequestException, UnknownHostException {
        UserEntity user = userDao.findUserByResetPasswordToken(token);
        if (user == null) {
           LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to reset password with invalid token at: " + token);
           throw new InvalidPasswordRequestException("Invalid token");
        }
        if(!user.getResetPasswordToken().equals(token)){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to reset password with invalid token at: " + token);
            throw new InvalidPasswordRequestException("Invalid token");
        }
        if(user.getResetPasswordTokenExpiry().isBefore(Instant.now())){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to reset password with expired token at: " + token);
            throw new InvalidPasswordRequestException("Token expired");
        }
        user.setPassword(HashUtil.toSHA256(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userDao.updateUser(user);
    }


    public void deleteUserPermanently(String username) throws UserNotFoundException, CriticalDataDeletionAttemptException, UnknownHostException {
        if(!checkIfUsernameExists(username)){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to delete user with invalid username at: " + username);
            throw new UserNotFoundException("User not found");
        }
        if(username.equals("admin") || username.equals("deletedTasks")){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to delete admin or deletedTasks user at: " + username);
            throw new CriticalDataDeletionAttemptException("Critical data deletion attempt");
        }
       // transferTasks(username);
       // transferCategories(username);
       // deleteMessages(username);
       // deleteNotifications(username);
        userDao.deleteUser(username);
    }
    public void transferTasks(String username){
        UserEntity admin=getUserByUsername("deletedUserTasks");
        List<TaskEntity> tasks=taskDao.getTasksByUser(username);
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
    public void deleteMessages(String username){
        UserEntity userEntity=getUserByUsername(username);
        List<MessageEntity> messages=messageDao.getMessages(userEntity);
        for(MessageEntity message:messages){
            messageDao.deleteMessageById(message.getId());
        }
    }
    public void deleteNotifications(String username){
        UserEntity userEntity=getUserByUsername(username);
        List< NotificationEntity> notifications = notificationDao.getNotifications(userEntity);
        for(NotificationEntity notification:notifications){
            notificationDao.deleteNotificationById(notification.getId());
        }
    }

    public void logout(String token) throws UserNotFoundException, UnknownHostException {
        UserEntity user = getUserByToken(token);
        if(user == null){
            LOGGER.warn(InetAddress.getLocalHost().getHostAddress() + "Attempt to logout with invalid token at " + LocalDateTime.now() + ": " + token);
            throw new UserNotFoundException("User not found");
        }
        user.setToken(null);
        statistiscsBean.broadcastUserStatisticsUpdate();
        userDao.updateUser(user);
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
    public void requestNewConfirmationEmail(EmailDto email) throws InvalidRequestOnRegistConfirmationException {
        UserEntity user = userDao.findUserByEmail(email.getEmail());
        if(user == null){
            LOGGER.warn("Attempt to request new confirmation email at - User not foud: " + email.getEmail());
            throw new InvalidRequestOnRegistConfirmationException("Not found user with this email");
        }
        if(user.isConfirmed()){
            LOGGER.warn("Attempt to request new confirmation email at - User already confirmed: " + email.getEmail());
            throw new InvalidRequestOnRegistConfirmationException("Not possible to request confirmation email please contact the administrator");
        }
        if(user.getLastSentEmailTimestamp() != null){
            Instant now = Instant.now();
            Instant lastSentEmail = user.getLastSentEmailTimestamp();
            long timeDifference = ChronoUnit.MINUTES.between(lastSentEmail, now);
            if(timeDifference < 1){
                LOGGER.warn("Attempt to request new confirmation email at - Time difference less than 1 minute" +
                        LocalDateTime.now() + ": " + email.getEmail());
                throw new InvalidRequestOnRegistConfirmationException("You can't request a new confirmation email now, please wait 1 minute");
            }
        }
        emailService.sendConfirmationEmail(user.getEmail(), user.getConfirmationToken());
        user.setLastSentEmailTimestamp(Instant.now());
    }
    @Schedule(hour = "*", minute = "*/1", persistent = false)
    public void cleanupExpiredTokens() {
        LOGGER.info("Running scheduled task to clean up expired tokens");
        List<UserEntity> users = userDao.findAllUsers();
        Instant now = Instant.now();
        long tokenValidityPeriodInSeconds = Long.parseLong(configBean.findConfigValueByKey("sessionTimeout"));

        for (UserEntity user : users) {
            if (user.getToken() != null) {
                Instant lastActivity = user.getLastActivityTimestamp();
                if (lastActivity == null || lastActivity.plusSeconds(tokenValidityPeriodInSeconds).isBefore(now)) {
                    LOGGER.info("Expiring token for user: " + user.getUsername());
                    user.setToken(null);
                    GlobalWebSocket.sendForcedLogoutRequest(user);
                    userDao.updateUser(user);
                }
            }
        }
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }

}