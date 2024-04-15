package aor.paj.service;

import aor.paj.bean.MessageBean;
import aor.paj.bean.NotificationBean;
import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;

@Path("/notification")
public class NotificationService {
    @EJB
    private NotificationBean notificationBean;
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getNotifications(@HeaderParam("Authorization") String authorizationHeader) {
        String token = authorizationHeader != null && authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : null;
        HashMap<String, List<NotificationDto>> agrupedDtos= notificationBean.getAgrupatedNotifications(token);
        System.out.println("Agrupated Notifications found: " + agrupedDtos);
        if (agrupedDtos != null) {
            return Response.status(Response.Status.OK).entity(agrupedDtos).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"No notifications found\"}").build();
        }
    }
    @PUT
    @Path("/markAsRead/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response markAsRead(@HeaderParam("Authorization") String authorizationHeader, @PathParam("id") String id) {
        String token = authorizationHeader != null && authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : null;
        boolean marked = notificationBean.markAsRead(token, id);
        if (marked) {
            return Response.status(Response.Status.OK).entity("{\"message\":\"Notification marked as read\"}").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Notification not found\"}").build();
        }
    }
}