package aor.paj.service.websocket;



import aor.paj.bean.NotificationBean;
import aor.paj.dto.NotificationDto;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import aor.paj.bean.MessageBean;
import aor.paj.bean.UserBean;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import util.GsonSetup;

import javax.naming.InitialContext;
@ApplicationScoped
@ServerEndpoint("/notification/{token}")
public class NotificationsWebSocket {
    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    static Gson gson = GsonSetup.createGson();
    @Inject
    private MessageBean messageBean;
    @EJB
    private UserBean userBean;
    @Inject
    private NotificationBean notificationBean;

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {

        System.out.println("Notification WebSocket connection opened");
        try {
            boolean validated = userBean.tokenValidator(token);
            System.out.println("Token received: " + token);
            System.out.println("Token validated: " + validated);
            String username = validated ? userBean.getUserByToken(token).getUsername() : null;
            if (username != null) {
                userSessions.put(username, session);
                System.out.println("Notification WebSocket connection opened for user: " + username);
            } else {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @OnClose
    public void onClose(Session session, @PathParam("token") String token) {
        String username = userBean.getUserByToken(token).getUsername(); // Obtém o nome de usuário do token
        userSessions.remove(username);
        System.out.println("Notification WebSocket connection closed for user: " + username);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("Notification error: " + throwable.getMessage());
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            System.out.println("Received message: " + message);
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();
            System.out.println("Type: " + type);

        } catch (Exception e) {
            System.err.println("Error processing Notification: " + e.getMessage());
        }
    }
    public static void sendNotification(NotificationDto notification) {
        String receiverUsername = notification.getUserId();
        Session receiverSession = userSessions.get(receiverUsername);
        if (receiverSession != null && receiverSession.isOpen()) {
            try {
                receiverSession.getBasicRemote().sendText(gson.toJson(new WebSocketMessage("receivedNotification", notification)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void broadcast(String message) {
        userSessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
