package aor.paj.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class UserConfirmationException extends Exception {
    public UserConfirmationException(String message) {
        super(message);
    }
}
