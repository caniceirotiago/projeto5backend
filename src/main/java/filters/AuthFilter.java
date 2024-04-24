package filters;

import aor.paj.bean.PermissionBean;
import aor.paj.bean.UserBean;
import aor.paj.entity.UserEntity;
import aor.paj.exception.UserNotFoundException;
import aor.paj.service.status.Function;
import jakarta.annotation.Priority;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.lang.reflect.Method;
import java.util.List;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthFilter implements ContainerRequestFilter {
    @EJB
    private UserBean userBean;

    @Context
    private ResourceInfo resourceInfo;
    @EJB
    private PermissionBean permissionBean;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if (path.endsWith("/login")
                || path.endsWith("/register")
                || path.contains("/confirm")
                || path.contains("/request-password-reset")
                || path.contains("/reset-password")
                || path.contains("/request-confirmation-email")) {
            return;
        }
        String authHeader = requestContext.getHeaderString("Authorization");
        System.out.println(authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (userBean.tokenValidator(token)) {
                    checkAuthorization(requestContext, token);
                } else {
                    abortUnauthorized(requestContext);
                }
            } catch (UserNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            abortUnauthorized(requestContext);
        }
    }
    private void checkAuthorization(ContainerRequestContext requestContext, String token) {
        Method method = resourceInfo.getResourceMethod();
        if (method.isAnnotationPresent(RequiresPermission.class)) {
            Function requiredPermissions = method.getAnnotation(RequiresPermission.class).value();
            boolean hasPermission = permissionBean.getPermission(token, requiredPermissions);
            if (!hasPermission) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            }
        }
        if (method.isAnnotationPresent(RequiresPermissionByTaskId.class)) {
            int id = requestContext.getUriInfo().getPathParameters().get("id") != null ?
                    Integer.parseInt(String.valueOf(requestContext.getUriInfo().getPathParameters().get("id").getFirst())) : -1;
            boolean hasPermission = permissionBean.getPermissionByTaskID(token, id);
            if (!hasPermission) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            }
        }
        if (method.isAnnotationPresent(RequiresPermissionByUserOnMessage.class)) {
            UserEntity user = userBean.getUserByToken(token);
            MultivaluedMap<String, String> usernames = requestContext.getUriInfo().getPathParameters();
            String sender = usernames.getFirst("sender");
            String receiver = usernames.getFirst("receiver");
            boolean hasPermission = (user.getUsername().equals(sender) || user.getUsername().equals(receiver));
            if (!hasPermission) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            }
        }
    }

    private void abortUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("No Permission, invalid or expired token").build());
    }
}
