package aor.paj.dto;

public class ResetPasswordRequestDTO {
    private String email;

    // Constructors
    public ResetPasswordRequestDTO() {}

    public ResetPasswordRequestDTO(String email) {
        this.email = email;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
