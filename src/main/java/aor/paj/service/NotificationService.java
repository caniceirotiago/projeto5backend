package aor.paj.service;

import aor.paj.bean.MessageBean;
import aor.paj.bean.NotificationBean;
import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import aor.paj.exception.UserNotFoundException;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.UnknownHostException;
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
    public HashMap<String, List<NotificationDto>> getNotifications(@HeaderParam("Authorization") String authorizationHeader) throws UserNotFoundException, UnknownHostException {
        String token = authorizationHeader.substring(7);
        return notificationBean.getAgrupatedNotifications(token);
    }
    @PUT
    @Path("/markAsRead/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void markAsRead(@HeaderParam("Authorization") String authorizationHeader, @PathParam("id") String id) throws UserNotFoundException, UnknownHostException {
        String token = authorizationHeader.substring(7);
        notificationBean.markAsRead(token, id);
    }
}