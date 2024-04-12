package aor.paj.service.websocket;

import aor.paj.entity.MessageEntity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
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

@ServerEndpoint("/chat/{token}")
public class ChatWebSocket {

    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    // Inside your WebSocket service
    Gson gson = GsonSetup.createGson();


    private MessageBean messageBean;
    private UserBean userBean;

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        System.out.println("Chat WebSocket connection opened");
        try {
            InitialContext ctx = new InitialContext();
            userBean = (UserBean) ctx.lookup("java:module/UserBean");
            messageBean = (MessageBean) ctx.lookup("java:module/MessageBean");

            boolean validated = userBean.tokenValidator(token);
            System.out.println("Token received: " + token);
            System.out.println("Token validated: " + validated);
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
            System.out.println("Message received12: " + message);
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();
            if (type.equals("markAsRead")) {
                JsonElement dataElement = json.get("data");
                Type listType = new TypeToken<List<Long>>() {}.getType();
                List<Long> messageIds = gson.fromJson(dataElement, listType);
                boolean success = messageBean.markMessagesAsRead(messageIds);
                if (success) {
                    System.out.println("Messages marked as read: " + messageIds);
                    List<MessageDto> messages = messageBean.getMessagesByIds(messageIds);
                    WebSocketMessage response = new WebSocketMessage("markedAsReadMessages", messages);
                    String jsonResponse = gson.toJson(response);
                    Session receiverSession = userSessions.get(messages.getFirst().getReceiverUsername());
                    Session senderSession = userSessions.get(messages.getFirst().getSenderUsername());
                    if (receiverSession != null && receiverSession.isOpen()) {
                        receiverSession.getBasicRemote().sendText(jsonResponse);
                        System.out.println("Receiver session open: " + receiverSession.isOpen());
                    }
                    if (senderSession != null && senderSession.isOpen()) {
                        senderSession.getBasicRemote().sendText(jsonResponse);
                        System.out.println("Sender session open: " + senderSession.isOpen());
                    }
                }
            }
            else if (type.equals("sendMessage")) {
                JsonObject data = json.getAsJsonObject("data");
                MessageDto msg = gson.fromJson(data, MessageDto.class);
                msg.setSentAt(LocalDateTime.now());
                if (data != null) {
                    MessageEntity savedMessage = messageBean.sendMessage(msg);
                    MessageDto savedMessageDto = messageBean.convertEntityToDto(savedMessage);
                    System.out.println("Message saved: " + savedMessageDto);
                    if (savedMessage != null) {
                        String jsonResponse = gson.toJson(new WebSocketMessage("receivedMessage", savedMessageDto));
                        Session receiverSession = userSessions.get(savedMessage.getReceiver().getUsername());
                        Session senderSession = userSessions.get(savedMessage.getSender().getUsername());
                        if (receiverSession != null && receiverSession.isOpen() ) {
                            receiverSession.getBasicRemote().sendText(jsonResponse);
                        }
                        if (senderSession != null && senderSession.isOpen()) {
                            senderSession.getBasicRemote().sendText(jsonResponse);
                        }
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
