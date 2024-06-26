package aor.paj.exception.mapper;

import aor.paj.dto.Error;
import aor.paj.exception.InvalidLoginException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class InvalidLoginExceptionMapper implements ExceptionMapper<InvalidLoginException> {
    private static final Logger LOGGER = LogManager.getLogger(InvalidLoginExceptionMapper.class);

    @Override
    public Response toResponse(InvalidLoginException e) {
        Error error = new Error(e.getMessage());
        LOGGER.warn("Attempt to login with invalid credentials: " + error.getErrorMessage());
        return Response
                .status(Response.Status.UNAUTHORIZED)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
