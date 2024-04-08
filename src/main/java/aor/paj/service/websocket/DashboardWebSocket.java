package aor.paj.service.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Set;

@ServerEndpoint("/dashboard")
public class DashboardWebSocket {
    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();
    private static final long HEARTBEAT_INTERVAL = 30000; // 30 seconds

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("New WebSocket connection: " + session.getId());
        startHeartbeat(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("WebSocket connection closed: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error on connection " + session.getId() + ": " + throwable.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Message received from " + session.getId() + ": " + message);
        // You can implement message handling here if needed
    }
    @OnMessage
    public void onPongMessage(PongMessage pongMessage, Session session) {
        System.out.println("Pong received from " + session.getId());
    }

    public static void broadcast(String message) {
        System.out.println(sessions.size() + " sessions");
        for (Session session : sessions) {
            if (session.isOpen()) {
                session.getAsyncRemote().sendText(message);
                System.out.println("Message sent to " + session.getId() + ": " + message);
            }
        }
    }
    private void startHeartbeat(Session session) {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (session.isOpen()) {
                    try {
                        ByteBuffer payload = ByteBuffer.wrap(new byte[]{8, 1}); // Non-empty payload for the ping
                        session.getBasicRemote().sendPing(payload);
                        System.out.println("Ping sent to " + session.getId());
                    } catch (IOException e) {
                        System.err.println("IOException during ping: " + e.getMessage());
                        timer.cancel();
                    } catch (IllegalStateException e) {
                        System.err.println("Session is closed, stopping heartbeat: " + e.getMessage());
                        timer.cancel();
                    }
                } else {
                    System.out.println("Session is closed, stopping heartbeat.");
                    timer.cancel();
                }
            }
        }, 0, HEARTBEAT_INTERVAL);
    }


}
