package aor.paj.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * UserNewPassword is a simple DTO class used to encapsulate a user's current password along
 * with their desired new password. This class is primarily utilized during password change operations to securely
 * transmit both the old and new passwords from the client to the server, ensuring that password update requests
 * are both authenticated and authorized.
 */

public class UserNewPassword {
    private String password;
    @NotNull
    @Size(min = 4, message = "Password must be greater than 4 characters")
    private String newPassword;

    public UserNewPassword() {
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getPassword() {
        return password;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
