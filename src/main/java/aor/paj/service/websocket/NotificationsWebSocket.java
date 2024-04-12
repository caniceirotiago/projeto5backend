package aor.paj.service.websocket;



import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

@ServerEndpoint("/notification/{token}")
public class NotificationsWebSocket {
    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    Gson gson = GsonSetup.createGson();
    private MessageBean messageBean;
    private UserBean userBean;

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        System.out.println("Notification WebSocket connection opened");
        try {
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
