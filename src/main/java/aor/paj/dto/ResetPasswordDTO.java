package aor.paj.dto;

import jakarta.ejb.EJB;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ResetPasswordDTO {
    @NotNull
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

    @XmlElement
    public String getToken() {
        return token;
    }
    @XmlElement
    public void setToken(String token) {
        this.token = token;
    }
    @XmlElement
    public String getNewPassword() {
        return newPassword;
    }
    @XmlElement
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
