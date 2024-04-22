package aor.paj.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class UserNotFoundException extends Exception {
    public UserNotFoundException(String message) {
        super(message);
    }
}
