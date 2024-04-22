package aor.paj.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class InvalidLoginException extends Exception {
    public InvalidLoginException(String message) {
        super(message);
    }
}
