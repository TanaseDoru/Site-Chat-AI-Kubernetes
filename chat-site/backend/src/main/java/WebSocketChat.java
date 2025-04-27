import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WebSocketChat extends WebSocketAdapter {
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final Map<Session, String> usernames = new ConcurrentHashMap<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[" + timestamp + "] " + message);
    }

    public WebSocketChat() {
        log("WebSocketChat instance created.");
        log("  - Active connections: " + sessions.size());
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        sessions.add(session);
        log("New connection established:");
        log("  - Remote: " + session.getRemoteAddress());
        log("  - Total active connections: " + sessions.size());

        // Extract username from the connection URL
        String username = "User-" + session.getRemoteAddress().toString().replaceAll("[^a-zA-Z0-9]", "");
        String queryString = session.getUpgradeRequest().getQueryString();
        if (queryString != null && queryString.contains("username=")) {
            username = queryString.split("username=")[1].split("&")[0];
        }
        usernames.put(session, username);
        
        // Broadcast "user joined" message to all clients
        String joinMessage = username + " joined the chat";
        broadcastMessage(joinMessage, null); // null sender means it's a system message
        
        // Broadcast updated user list
        broadcastUserList();
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        Session session = getSession();
        log("Received message from " + session.getRemoteAddress() + ":");
        log("  - Message content: " + message);

        // Update username if message contains a username
        if (message.contains(":")) {
            String newUsername = message.split(":")[0].trim();
            usernames.put(session, newUsername);
        }

        // Broadcast message to all connected clients except the sender
        broadcastMessage(message, session);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        Session session = getSession();
        if (session != null) {
            String username = usernames.get(session);
            String leaveMessage = username + " left the chat";
            // Send leave message before removing the session
            broadcastMessage(leaveMessage, null);
            
            // Remove the session before broadcasting user list
            sessions.remove(session);
            usernames.remove(session);
            
            // Broadcast updated user list after removing the disconnected user
            broadcastUserList();
            
            log("Connection closed:");
            log("  - Remote: " + session.getRemoteAddress());
            log("  - Status Code: " + statusCode);
            log("  - Reason: " + reason);
            log("  - Remaining active connections: " + sessions.size());
        } else {
            log("Connection closed with null session");
            log("  - Status Code: " + statusCode);
            log("  - Reason: " + reason);
        }
        super.onWebSocketClose(statusCode, reason);
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        super.onWebSocketError(throwable);
        Session session = getSession();
        if (session != null) {
            sessions.remove(session);
            usernames.remove(session);
        }
        log("Error in session " + (session != null ? session.getRemoteAddress() : "unknown") + ":");
        log("  - Error message: " + throwable.getMessage());
        log("  - Stack trace:");
        throwable.printStackTrace();
    }

    // Helper method to broadcast messages, excluding the sender if specified
    private void broadcastMessage(String message, Session sender) {
        try {
            for (Session s : sessions) {
                if (s.isOpen() && (sender == null || !s.equals(sender))) {
                    s.getRemote().sendString(message);
                    log("  - Message forwarded to: " + s.getRemoteAddress());
                }
            }
        } catch (IOException e) {
            log("Error broadcasting message: " + e.getMessage());
        }
    }

    // Helper method to broadcast the list of connected users
    private void broadcastUserList() {
        StringBuilder userList = new StringBuilder("USERLIST:");
        for (String username : usernames.values()) {
            userList.append(username).append(",");
        }
        // Remove trailing comma
        if (userList.length() > 8) { // "USERLIST:".length()
            userList.setLength(userList.length() - 1);
        }
        String userListStr = userList.toString();
        log("Broadcasting user list: " + userListStr);
        broadcastMessage(userListStr, null);
    }
}