package aor.paj.service;

import aor.paj.bean.MessageBean;
import aor.paj.bean.NotificationBean;
import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/notifications")
public class NotificationService {
    @EJB
    private NotificationBean notificationBean;
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getNotifications(@HeaderParam("Authorization") String authorizationHeader) {
        String token = authorizationHeader != null && authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : null;
        List<NotificationDto> notificationsDtos = notificationBean.getNotifications(token);
        if (notificationsDtos != null) {
            return Response.status(Response.Status.OK).entity(notificationsDtos).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"No notifications found\"}").build();
        }
    }
}