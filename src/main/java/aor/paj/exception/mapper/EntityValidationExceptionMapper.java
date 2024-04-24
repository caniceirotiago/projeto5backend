

package aor.paj.exception.mapper;

import aor.paj.bean.UserBean;
import aor.paj.dto.Error;
import aor.paj.exception.DuplicateUserException;
import aor.paj.exception.EntityValidationException;
import aor.paj.exception.InvalidLoginException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.time.LocalDateTime;

@Provider
public class EntityValidationExceptionMapper implements ExceptionMapper<EntityValidationException> {
    private static final Logger LOGGER = LogManager.getLogger(EntityValidationExceptionMapper.class);

    @Override
    public Response toResponse(EntityValidationException e) {
        Error error = new Error(e.getMessage());
        LOGGER.warn("Attempt to acess an invalid entity " + LocalDateTime.now() + ": " + error.getErrorMessage());
        return Response
                .status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
