package aor.paj.service.websocket;

import aor.paj.bean.NotificationBean;
import aor.paj.dto.TaskDto;
import aor.paj.entity.MessageEntity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import aor.paj.dto.MessageDto;
import aor.paj.bean.MessageBean;
import aor.paj.bean.UserBean;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import util.GsonSetup;

import javax.naming.InitialContext;
@ApplicationScoped
@ServerEndpoint("/taskws/{token}")
public class TaskWebSocket {


    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    // Inside your WebSocket service
    static Gson gson = GsonSetup.createGson();

    @EJB
    private UserBean userBean;

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        System.out.println("Chat WebSocket connection opened");
        try {
            boolean validated = userBean.tokenValidator(token);
            System.out.println("Token received: " + token);
            System.out.println("Token validated: " + validated);
            String username = validated ? userBean.getUserByToken(token).getUsername() : null;
            if (username != null) {
                userSessions.put(username, session);
                System.out.println("Task WebSocket connection opened for user: " + username);
            } else {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("token") String token) {
        String username = userBean.getUserByToken(token).getUsername();
        userSessions.remove(username);
        System.out.println("Task WebSocket connection closed for user: " + username);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error: " + throwable.getMessage());
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    public static void broadcast(String actionType, TaskDto task) {
        //actionType could be "createTask", "updatedTask", the temporary delete is a type of update
        userSessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(gson.toJson(new WebSocketMessage(actionType, task)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
