package aor.paj.service;

import aor.paj.bean.UserBean;
import aor.paj.dto.*;
import aor.paj.exception.*;
import aor.paj.service.status.Function;
import filters.RequiresPermission;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/users")
public class UserService {
    @EJB
    UserBean userBean;
    /**
     * This endpoint is responsible for adding a new user to the system. It accepts JSON-formatted requests
     * containing user data and processes the request accordingly.
     * If the provided user data fails validation, it returns a status code of 400 (Bad Request) with the message
     * "Invalid Data".
     * If a user with the same username or email already exists in the system, it returns a status code of 409
     * (Conflict) with the message "Username or Email already Exists".
     * If the user is successfully added to the system, it returns a status code of 200 (OK) with the message
     * "A new user was created".
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addUser(@Valid User user) throws DuplicateUserException {userBean.register(user);}

    @POST
    @Path("/confirm")
    public void confirmRegistration(@QueryParam("token") String token) throws UserConfirmationException {
        userBean.confirmUser(token);}
    @POST
    @Path("/request-password-reset")
    @Consumes(MediaType.APPLICATION_JSON)
    public void requestPasswordReset(ResetPasswordRequestDTO requestPasswordResetDto) throws InvalidPasswordRequestException {
        userBean.requestPasswordReset(requestPasswordResetDto.getEmail());
    }
    @POST
    @Path("/reset-password")
    @Consumes(MediaType.APPLICATION_JSON)
    public void resetPassword(@Valid ResetPasswordDTO resetPasswordDto) throws InvalidPasswordRequestException {
        userBean.resetPassword(resetPasswordDto.getToken(), resetPasswordDto.getNewPassword());
    }

    /**
     * This endpoint is responsible for user authentication. It accepts JSON-formatted requests containing
     * user credentials (username and password) as headers. It returns appropriate responses indicating the
     * success or failure of the login attempt.
     * Successful login returns a status code of 200, failed login returns 401, and missing username or password
     * returns 422.
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TokenDto login(LoginDto user) throws InvalidLoginException {return userBean.login(user);}
    /**
     * Retrieves the photo URL and the first name associated with the provided username.
     * If the username and password are not provided in the request headers, returns a status code 401 (Unauthorized)
     * with the error message "User not logged in".
     * If the provided credentials are invalid, returns a status code 403 (Forbidden) with the error message "Access denied".
     * If the photo URL and first name are found for the given username, returns a status code 200 (OK) with the photo URL and first name in JSON format.
     * If no photo URL or first name is found for the given username, returns a status code 404 (Not Found) with the error message "No photo or name found".
     */
    @GET
    @Path("/photoandname")
    @Produces(MediaType.APPLICATION_JSON)
    public InitialInformationDto getPhoto(@HeaderParam("Authorization") String authHeader) throws UserNotFoundException {
        String token = authHeader.substring(7);
        return userBean.getUserBasicInfo(token);
    }

    /**
     * Retrieves user information for the given username.
     * If the username or password is missing in the request headers, returns a status code 401 (Unauthorized)
     * with the error message "User not logged in".
     * If the provided credentials are invalid, returns a status code 403 (Forbidden) with the error message "Access denied".
     * If the user information is successfully retrieved, returns a status code 200 (OK) with the user information
     * (without the password) in JSON format.
     */
    @GET
    @Path("info/{usernameProfile}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.GET_OTHER_USER_INFO)
    public UserWithNoPassword userInfo(@PathParam("usernameProfile") String usernameProfile) throws UserNotFoundException {
        return userBean.getUserWithNoPasswordByUsername(usernameProfile);
    }

    /**
     * Provides a list of all registered users in the system. It requires authentication and checks
     * for the necessary permissions before proceeding. This endpoint is typically used by administrators
     * or users with specific roles that allow viewing all user accounts.
     */
     @GET
     @Path("")
     @Produces(MediaType.APPLICATION_JSON)
     @RequiresPermission(Function.GET_OTHER_USER_INFO)
     public List<UserInfoCard> getAllUsers() {return (userBean.getAllUsersInfo());}
    /**
     * Allows an authenticated user to update their own data. It checks for valid authentication and
     * proper permissions before allowing the update. The method ensures that the user can only update
     * their own information and not that of others unless specifically authorized.
     */
    @PATCH
    @Path("/data")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.EDIT_OWN_USER_INFO)
    public void editUserData(@Valid UserUpdateDTO updatedUser, @HeaderParam("Authorization") String authHeader) throws UserNotFoundException {
        String token = authHeader.substring(7);
        userBean.updateUser(token, updatedUser);}

    /**
     * Allows an administrator to edit another user's data, given a specific username. This endpoint
     * ensures that only users with the appropriate permissions can make changes to other user accounts.
     * It performs checks to ensure that the email and username remain unique and not already in use.
     */
    @PATCH
    @Path("/otheruser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.EDIT_OTHER_USER_INFO)
    public void adminEditUserData(@Valid UserUpdateDTO updatedUser, @HeaderParam("userToChangeUsername") String username) throws UserNotFoundException {
        userBean.updateUserByUsername(username, updatedUser);
    }

    /**
     * Enables a user to update their password. It requires the old password for verification
     * and checks if the new password meets the system's security requirements. This endpoint
     * is crucial for maintaining user account security.
     */
    @POST
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public void editUserPassword(@Valid UserNewPassword updatedPassword, @HeaderParam("Authorization") String authHeader) throws InvalidPasswordRequestException {
        String token = authHeader.substring(7);
        userBean.updatePassWord(token, updatedPassword.getNewPassword(), updatedPassword.getPassword());
    }

    /**
     * Permanently deletes a user from the system based on the specified username. This operation is irreversible
     * and involves transferring any tasks or categories associated with the user to a default state before
     * deletion to ensure data consistency. This endpoint requires authentication and specific permissions,
     * typically reserved for administrators, to execute this action.
     */
    @DELETE
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.PERMANENTLY_USER_DELET)
    public void deleteUserPermanently(@HeaderParam("userToDeleteUsername")String username) throws UserNotFoundException, CriticalDataDeletionAttemptException {
        userBean.transferTasks(username);
        userBean.transferCategories(username);
        userBean.deleteMessages(username);
        userBean.deleteNotifications(username);
        userBean.deleteUserPermanently(username);
    }
    @GET
    @Path("/withtasks")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserInfoCard> getUsersWithTasks() {return userBean.getUsersWithTasks();}
    /**
     * This endpoint makes logging out a user. Since this example does not
     * manage user sessions or authentication tokens explicitly, the endpoint simply returns
     * a response indicating that the user has been logged out successfully.
     *  */
    @POST
    @Path("/logout")
    public void logout(@HeaderParam("Authorization") String authHeader) throws UserNotFoundException {
        String token = authHeader.substring(7);
        userBean.logout(token);
    }
    @POST
    @Path("/request-confirmation-email")
    @Consumes(MediaType.APPLICATION_JSON)
    public void requestConfirmationEmail(EmailDto email) throws InvalidRequestOnRegistConfirmationException {
        userBean.requestNewConfirmationEmail(email);
    }
}