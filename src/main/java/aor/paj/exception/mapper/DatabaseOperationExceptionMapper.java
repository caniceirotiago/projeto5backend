package aor.paj.exception.mapper;

import aor.paj.bean.UserBean;
import aor.paj.dto.Error;
import aor.paj.exception.CriticalDataDeletionAttemptException;
import aor.paj.exception.DatabaseOperationException;
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
public class DatabaseOperationExceptionMapper implements ExceptionMapper<DatabaseOperationException> {
    private static final Logger LOGGER = LogManager.getLogger(DatabaseOperationExceptionMapper.class);

    @Override
    public Response toResponse(DatabaseOperationException e) {
        Error error = new Error(e.getMessage());
        LOGGER.error("Database operation failed " + LocalDateTime.now() + ": " + error.getErrorMessage());
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
