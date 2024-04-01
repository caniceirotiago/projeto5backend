package aor.paj.exception.mapper;

import aor.paj.dto.Error;
import aor.paj.exception.InvalidLoginException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidLoginExceptionMapper implements ExceptionMapper<InvalidLoginException> {
    @Override
    public Response toResponse(InvalidLoginException e) {
        return Response.status(Response.Status.UNAUTHORIZED).entity(new Error(e.getMessage())).build();
    }

}
