import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebSocket
public class WebSocketChat extends WebSocketAdapter {
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[" + timestamp + "] " + message);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        sessions.add(session);
        log("New connection established:");
        log("  - Remote: " + session.getRemoteAddress());
        log("  - Total active connections: " + sessions.size());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        log("Received message from " + session.getRemoteAddress() + ":");
        log("  - Message content: " + message);
        
        // Broadcast message to all connected clients
        for (Session s : sessions) {
            if (s.isOpen()) {
                s.getRemote().sendString(message);
                log("  - Message forwarded to: " + s.getRemoteAddress());
            }
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        sessions.remove(session);
        log("Connection closed:");
        log("  - Remote: " + session.getRemoteAddress());
        log("  - Status Code: " + statusCode);
        log("  - Reason: " + reason);
        log("  - Remaining active connections: " + sessions.size());
    }

    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session);
        log("Error in session " + session.getRemoteAddress() + ":");
        log("  - Error message: " + throwable.getMessage());
        log("  - Stack trace:");
        throwable.printStackTrace();
    }
}
