package aor.paj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * DTO class for conveying initial information about a user, specifically for use in the `/photoandname` endpoint.
 * This class provides a simplified view of a user's basic information without personal details like passwords.
 */
@XmlRootElement
public class InitialInformationDto {

    private String photoUrl;

    private String name;

    private String role;

    private String username;

    public InitialInformationDto() {
    }

    public InitialInformationDto(String photoUrl, String name, String role, String username) {
        this.photoUrl = photoUrl;
        this.name = name;
        this.role = role;
        this.username = username;
    }

    @XmlElement
    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "InitialInformationDto{" +
                "photoUrl='" + photoUrl + '\'' +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
