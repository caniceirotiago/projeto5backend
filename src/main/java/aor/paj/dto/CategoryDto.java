package aor.paj.dto;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;

public class CategoryDto {
    @XmlElement
    @Id
    int id;
    @XmlElement
    @NotNull
    @Size(min = 2, max = 25, message = "Category must be between 2 and 25 characters")
    private String type;
    @XmlElement
    private String owner_username;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public CategoryDto() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOwner_username() {
        return owner_username;
    }

    public void setOwner_username(String owner_username) {
        this.owner_username = owner_username;
    }

    @Override
    public String toString() {
        return "CategoryDto{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", owner_username='" + owner_username + '\'' +
                '}';
    }
}


