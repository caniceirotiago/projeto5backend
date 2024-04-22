package aor.paj.exception.mapper;

import aor.paj.bean.UserBean;
import aor.paj.dto.Error;
import aor.paj.exception.CriticalDataDeletionAttemptException;
import aor.paj.exception.DuplicateUserException;
import aor.paj.exception.InvalidLoginException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.time.LocalDateTime;

@Provider
public class CriticalDataDeletionAttemptExceptionMapper implements ExceptionMapper<CriticalDataDeletionAttemptException> {
    private static final Logger LOGGER = LogManager.getLogger(CriticalDataDeletionAttemptExceptionMapper.class);

    @Override
    public Response toResponse(CriticalDataDeletionAttemptException e) {
        Error error = new Error(e.getMessage());
        LOGGER.warn("Attempt to delete critical data " + LocalDateTime.now() + ": " + error.getErrorMessage());
        return Response
                .status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
