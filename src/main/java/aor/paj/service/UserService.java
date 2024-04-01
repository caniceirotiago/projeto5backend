package aor.paj.service;

import aor.paj.bean.UserBean;
import aor.paj.dto.*;
import aor.paj.entity.UserEntity;
import aor.paj.service.status.Function;
import aor.paj.service.validator.UserValidator;
import filters.RequiresPermission;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;


@Path("/users")

public class UserService {
    @EJB
    UserBean userBean;
    @Inject
    UserValidator userValidator;

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
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUser(@Valid User user) {
        boolean isRegistered = userBean.register(user);
        if (isRegistered) {
            return Response.status(Response.Status.CREATED).entity("{\"message\":\"User registered successfully\"}").build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\":\"User registration failed\"}").build();
        }
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
    public Response login(LoginDto user) {
        String token = userBean.login(user);
       if(token != null)return Response.status(200).entity("{\"token\":\"" + token + "\"}").build();
       else return Response.status(401).entity("Login Failed").build();
    }
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
    public Response getPhoto(@HeaderParam("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        List<String> userInformation = userBean.getUserBasicInfo(token);
        System.out.println(userInformation.getFirst());
        System.out.println(userInformation.get(3));
        if (userInformation.getFirst() != null)return Response.status(200)
                .entity("{\"photoUrl\":\"" + userInformation.getFirst()
                        + "\", \"name\":\"" + userInformation.get(1) + "\", \"role\":\""
                        + userInformation.get(2) +
                        "\", \"username\":\""
                        + userInformation.get(3) + "\"}")
                .build();
        return Response.status(404).entity("Not found").build();
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
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userInfo(@HeaderParam("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        UserEntity userEntity = userBean.getUserByToken(token);
        UserWithNoPassword userWithoutPassword = userBean.convertUserEntityToUserWithNoPassword(userEntity);
        return Response.status(200).entity(userWithoutPassword).build();
    }

    /**
     * Retrieves detailed information for a user based on a provided username. It checks if the request
     * is authenticated and authorized to access the information. This endpoint is useful for obtaining
     * user details without exposing sensitive information like passwords.
     */

    @GET
    @Path("/userinfo/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.GET_OTHER_USER_INFO)
    public Response userInfoByusername(@PathParam("username") String username) {
        if(username == null) return Response.status(400).entity("Invalid Data").build();
        UserEntity userEntity = userBean.getUserByUsername(username);
        UserWithNoPassword userWithoutPassword = userBean.convertUserEntityToUserWithNoPassword(userEntity);
        return Response.status(200).entity(userWithoutPassword).build();
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
     public Response getAllUsers() {
         System.out.println(userBean.getAllUsersInfo());
         return Response.status(200).entity(userBean.getAllUsersInfo()).build();
     }
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
    public Response editUserData(User updatedUser, @HeaderParam("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        if (userValidator.validateUserOnEdit(updatedUser) && updatedUser.getUsername() == null) {
            if (!userBean.checkIfUsernameExists(updatedUser.getUsername())) {
                boolean updateResult = userBean.updateUser(token, updatedUser);
                if (updateResult) return Response.status(200).entity("{\"message\":\"User Updated\"}").build();
                else return Response.status(500).entity("An error occurred while updating user data").build();
            }return Response.status(409).entity("Username or Email already Exists").build();
        }return Response.status(400).entity("Invalid Data").build();

    }

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
    public Response adminEditUserData(User updatedUser, @HeaderParam("userToChangeUsername") String username) {
        if (userValidator.validateUserOnEdit(updatedUser) && updatedUser.getUsername() == null)  {
            if (userBean.checkIfUsernameExists(username)) {
                if(!userBean.checkIfEmailExists(updatedUser.getEmail())) {
                    boolean updateResult = userBean.updateUserByUsername(username, updatedUser);
                    if (updateResult)
                        return Response.status(200).entity("{\"message\":\"User data updated successfully\"}").build();
                    else
                        return Response.status(400).entity("An error occurred while updating user data").build();
                } return Response.status(409).entity("Username or Email already Exists").build();
            }return Response.status(409).entity("Username do not Exists").build();
        }return Response.status(400).entity("Invalid Data").build();
    }

    /**
     * Enables a user to update their password. It requires the old password for verification
     * and checks if the new password meets the system's security requirements. This endpoint
     * is crucial for maintaining user account security.
     */
    @POST
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editUserPassword(UserNewPassword updatedPassword, @HeaderParam("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        if (userBean.oldPasswordConfirmation(token, updatedPassword.getPassword(),
                updatedPassword.getNewPassword())) {
            if (userValidator.validatePassword(updatedPassword.getNewPassword())) {
                boolean updateResult = userBean.updatePassWord(token, updatedPassword.getNewPassword());
                if (updateResult) return Response.status(200).entity("{\"message\":\"User password updated successfully\"}").build();
                else return Response.status(500).entity("An error occurred while updating user password").build();
            }return Response.status(400).entity("Invalid Data").build();
        }return Response.status(401).entity("Login Failed, Passwords do not match or New password must be different from the old password").build();
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
    public Response deleteUserPermanently(@HeaderParam("userToDeleteUsername")String username){
        if(userBean.checkIfUsernameExists(username)) {
            if(!username.equals("admin") && !username.equals("deletedTasks")) {
                userBean.transferTasks(username);
                userBean.transferCategories(username);
                boolean successfullyDeleted = userBean.deleteUserPermanently(username);
                if (successfullyDeleted)
                    return Response.status(200).entity("{\"message\":\"This user permanently deleted\"}").build();
                else return Response.status(400).entity("User not deleted").build();
            }else return Response.status(400).entity("Admin can't be deleted.").build();
        } else return Response.status(400).entity("User with this id not found").build();
    }

    @GET
    @Path("/withtasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersWithTasks() {
        List<UserInfoCard> usersWithTasks = userBean.getUsersWithTasks();
        return Response.status(200).entity(usersWithTasks).build();
    }
    /**
     * This endpoint makes logging out a user. Since this example does not
     * manage user sessions or authentication tokens explicitly, the endpoint simply returns
     * a response indicating that the user has been logged out successfully.
     *  */
    @POST
    @Path("/logout")
    public Response logout(@HeaderParam("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        if (token == null) {
            return Response.status(422).entity("Missing token").build();
        }
        userBean.logout(token);
        return Response.status(200).entity("{\"message\":\"User logged out successfully\"}").build();
    }
}