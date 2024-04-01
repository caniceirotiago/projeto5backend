package aor.paj.exception.mapper;

import aor.paj.dto.Error;
import aor.paj.exception.DuplicateUserException;
import aor.paj.exception.InvalidLoginException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DuplicateUserExceptionMapper implements ExceptionMapper<DuplicateUserException> {
    @Override
    public Response toResponse(DuplicateUserException e) {
        return Response.status(Response.Status.CONFLICT).entity(new Error(e.getMessage())).build();
    }

}
