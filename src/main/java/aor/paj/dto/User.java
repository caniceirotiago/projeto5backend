package aor.paj.dto;
import jakarta.validation.constraints.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.UUID;

/**
 * User is a DTO representing a user within the system. It has essential user information including identification,
 * authentication, contact details, and a photo URL. It is designed to fully represent a
 * user entity in various operations such as registration, profile
 * management, and authentication processes.
 */

@XmlRootElement
public class User {
    @NotNull(message = "Username is required")
    @Size(min = 2, max = 25, message = "Username must be between 2 and 20 characters")
    private String username;
    @NotNull
    @Size(min = 4, message = "Password must be greater than 6 characters")
    private String password;
    @NotNull
    @Email
    private String email;
    @NotNull
    @Size(min = 2, max = 25, message = "First name must be between 2 and 25 characters")
    private String firstName;
    @NotNull
    @Size(min = 2, max = 25, message = "Last name must be between 2 and 25 characters")
    private String lastName;
    @NotNull
    @Pattern(regexp = "^\\+?\\d{9,15}$", message = "Invalid phone number")
    private String phoneNumber;
    @NotBlank
    private String photoURL;
    @NotBlank
    private String role;
    private String confirmationToken;
    private boolean isConfirmed;

    private boolean deleted;

    public User() {
    }

    public User(String username, String password, String phoneNumber,
                String email, String firstName, String lastName, String photoURL, String role, boolean deleted) {
        // todos os atributos são preenchidos pelo construtor a void
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.photoURL = photoURL;
        this.role=role;
        this.deleted = deleted;
    }

    // getters e setters como xmlElement


    public String getConfirmationToken() {
        return confirmationToken;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmationToken(String confirmationToken) {
        this.confirmationToken = confirmationToken;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }

    @XmlElement
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @XmlElement
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    @XmlElement
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    @XmlElement
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    @XmlElement
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    @XmlElement
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    @XmlElement
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    @XmlElement
    public String getPhotoURL() {
        return photoURL;
    }
    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "User{" +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", photoURL='" + photoURL + '\'' +
                ", role='" + role + '\'' +
                ", confirmationToken='" + confirmationToken + '\'' +
                ", isConfirmed=" + isConfirmed +
                '}';
    }
}