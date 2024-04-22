package aor.paj.exception;
import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class DuplicateUserException extends Exception {
    public DuplicateUserException(String message) {
        super(message);
    }
}
