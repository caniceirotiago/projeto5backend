package aor.paj.dto;

import jakarta.ejb.EJB;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ResetPasswordDTO {
    private String token;
    @NotNull
    @Size(min = 4, message = "Password must be greater than 4 characters")
    private String newPassword;

    // Constructors
    public ResetPasswordDTO() {}

    public ResetPasswordDTO(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
