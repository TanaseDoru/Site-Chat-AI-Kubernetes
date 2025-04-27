package com.example.chat;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WebSocketChat extends WebSocketAdapter {
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final Map<Session, String> usernames = new ConcurrentHashMap<>();
    private static final MongoDBService mongoDBService = new MongoDBService();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private void log(String message) {
        System.out.println(message);
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

        String username = "User-" + session.getRemoteAddress().toString().replaceAll("[^a-zA-Z0-9]", "");
        String queryString = session.getUpgradeRequest().getQueryString();
        if (queryString != null && queryString.contains("username=")) {
            username = queryString.split("username=")[1].split("&")[0];
        }
        usernames.put(session, username);
        
        try {
            List<String> recentMessages = mongoDBService.getRecentMessages();
            for (String message : recentMessages) {
                if (session.isOpen()) {
                    session.getRemote().sendString(message + "\n");
                }
            }
        } catch (IOException e) {
            log("Error sending recent messages: " + e.getMessage());
        }
        // Send user list to the new user
        sendUserList(session);
        // Broadcast updated user list to all users
        broadcastUserList();
        
        String timestamp = LocalDateTime.now().format(formatter);
        String joinMessage = String.format("[%s] %s joined the chat", timestamp, username);
        broadcastMessage(joinMessage, null);
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        Session session = getSession();
        log("Received message from " + session.getRemoteAddress() + ":");
        log("  - Message content: " + message);

        if (message.contains(":")) {
            String newUsername = message.split(":")[0].trim();
            usernames.put(session, newUsername);
        }

        String username = usernames.get(session);
        String messageContent = message.substring(message.indexOf(":") + 1).trim();
        mongoDBService.saveMessage(username, messageContent);
        
        // Check if message already has a timestamp
        if (!message.matches("^\\[.*\\].*:.*")) {
            String timestamp = LocalDateTime.now().format(formatter);
            message = String.format("[%s] %s: %s", timestamp, username, messageContent);
        }
        
        broadcastMessage(message, session);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        Session session = getSession();
        if (session != null) {
            String username = usernames.get(session);
            
            // Remove session before broadcasting
            sessions.remove(session);
            usernames.remove(session);
            
            // Broadcast leave message and update user list
            String timestamp = LocalDateTime.now().format(formatter);
            String leaveMessage = String.format("[%s] %s left the chat", timestamp, username);
            broadcastMessage(leaveMessage, null);
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

    private void broadcastMessage(String message, Session sender) {
        List<Session> sessionsToRemove = new ArrayList<>();
        synchronized (sessions) {
            log("Broadcasting message to " + sessions.size() + " users: " + message);
            for (Session s : sessions) {
                try {
                    if (s.isOpen() && (sender == null || !s.equals(sender))) {
                        s.getRemote().sendString(message + "\n");
                        log("  - Message forwarded to: " + s.getRemoteAddress());
                    }
                } catch (Exception e) {
                    log("Error sending message to " + s.getRemoteAddress() + ": " + e.getMessage());
                    sessionsToRemove.add(s);
                }
            }
        }
        
        // Remove any sessions that had errors
        for (Session s : sessionsToRemove) {
            sessions.remove(s);
            usernames.remove(s);
            log("Removed dead session: " + s.getRemoteAddress());
        }
    }

    private void broadcastUserList() {
        StringBuilder userList = new StringBuilder("USERLIST:");
        synchronized (usernames) {
            for (String username : usernames.values()) {
                userList.append(username).append(",");
            }
        }
        if (userList.length() > 8) {
            userList.setLength(userList.length() - 1);
        }
        String userListStr = userList.toString();
        log("Broadcasting user list to all users: " + userListStr);
        log("Current active sessions: " + sessions.size());
        broadcastMessage(userListStr, null);
    }

    private void sendUserList(Session session) {
        if (!session.isOpen()) {
            log("Cannot send user list - session is closed");
            return;
        }
        
        StringBuilder userList = new StringBuilder("USERLIST:");
        synchronized (usernames) {
            for (String username : usernames.values()) {
                userList.append(username).append(",");
            }
        }
        if (userList.length() > 8) {
            userList.setLength(userList.length() - 1);
        }
        String userListStr = userList.toString();
        log("Sending user list to new user " + session.getRemoteAddress() + ": " + userListStr);
        try {
            session.getRemote().sendString(userListStr);
        } catch (IOException e) {
            log("Error sending user list: " + e.getMessage());
        }
    }
} 