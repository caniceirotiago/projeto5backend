
package aor.paj.exception;
import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class EntityValidationException extends Exception {
    public EntityValidationException(String message) {
        super(message);
    }
}
