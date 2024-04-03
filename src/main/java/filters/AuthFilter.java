package filters;

import aor.paj.bean.PermissionBean;
import aor.paj.bean.UserBean;
import aor.paj.entity.UserEntity;
import aor.paj.service.status.Function;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.lang.reflect.Method;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthFilter implements ContainerRequestFilter {
    @Inject
    private UserBean userBean; // Seu bean que gerencia os usuários

    @Context
    private ResourceInfo resourceInfo;
    @Inject
    private PermissionBean permissionBean;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if (path.endsWith("/login")
                || path.endsWith("/register")
                || path.contains("/confirm")
                || path.contains("/request-password-reset")
                || path.contains("/reset-password")) {
            return;
        }
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (userBean.tokenValidator(token)) {
                checkAuthorization(requestContext, token);
            } else {
                abortUnauthorized(requestContext);
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
    }

    private void abortUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("No Permission, invalid or expired token").build());
    }
}
