package aor.paj.dto;

import jakarta.json.bind.annotation.JsonbProperty;

public class Error {

    @JsonbProperty("errorMessage")
    private final String errorMessage;

    public Error(String message) {
        this.errorMessage = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
