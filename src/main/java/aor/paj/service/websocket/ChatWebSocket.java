package aor.paj.service.websocket;

import aor.paj.entity.MessageEntity;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import aor.paj.dto.MessageDto;
import aor.paj.bean.MessageBean;
import aor.paj.bean.UserBean; // Suponha que UserBean possa validar tokens e extrair nomes de usuário
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import util.GsonSetup;

import javax.naming.InitialContext;

@ServerEndpoint("/chat/{token}")
public class ChatWebSocket {

    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    // Inside your WebSocket service
    Gson gson = GsonSetup.createGson();


    private MessageBean messageBean;
    private UserBean userBean;

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            InitialContext ctx = new InitialContext();
            userBean = (UserBean) ctx.lookup("java:module/UserBean");
            messageBean = (MessageBean) ctx.lookup("java:module/MessageBean");

            boolean validated = userBean.tokenValidator(token);
            String username = validated ? userBean.getUserByToken(token).getUsername() : null;
            if (username != null) {
                userSessions.put(username, session);
                System.out.println("Chat WebSocket connection opened for user: " + username);
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
        System.out.println("Chat WebSocket connection closed for user: " + username);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error: " + throwable.getMessage());
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            MessageDto msg = gson.fromJson(message, MessageDto.class);
            if (msg != null) {
                MessageEntity savedMessage = messageBean.sendMessage(msg);
                MessageDto savedMessageDto = messageBean.convertEntityToDto(savedMessage);
                if (savedMessage != null) {
                    System.out.println("Message sent: " + savedMessageDto.getContent());
                    String jsonResponse = gson.toJson(savedMessageDto);
                    System.out.println("Sending message to receiver: " + savedMessage.getReceiver().getUsername());
                    Session receiverSession = userSessions.get(savedMessage.getReceiver().getUsername());
                    if (receiverSession != null && receiverSession.isOpen()) {
                        receiverSession.getBasicRemote().sendText(jsonResponse);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
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
