package aor.paj.dto;

public class Error {

    private final String errorMessage;

    public Error(String message) {
        this.errorMessage = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
