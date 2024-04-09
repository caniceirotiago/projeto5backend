package aor.paj.service;

import aor.paj.bean.MessageBean;
import aor.paj.dto.MessageDto;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/messages")
public class MessageService {

    @EJB
    private MessageBean messageBean;


    /**
     * Endpoint to retrieve all messages between two specific users.
     * Messages are sorted by the sent time.
     */
    @GET
    @Path("/{sender}/{receiver}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getMessagesBetweenUsers(@PathParam("sender") String sender, @PathParam("receiver") String receiver) {
        List<MessageDto> messageDtos = messageBean.getMessagesBetweenUsers(sender, receiver);
        if (messageDtos != null) {
            return Response.status(Response.Status.OK).entity(messageDtos).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"No messages found between the specified users\"}").build();
        }
    }
}
