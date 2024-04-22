
package aor.paj.exception;
import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class CriticalDataDeletionAttemptException extends Exception {
    public CriticalDataDeletionAttemptException(String message) {
        super(message);
    }
}
