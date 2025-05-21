package com.example.chat;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        // Set up a servlet context handler
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Add the WebSocket servlet for the /chat endpoint
        context.addServlet(ChatWebSocketServlet.class, "/chat");

        server.setHandler(context);
        server.start();
        server.join();
    }

    // Define the WebSocket servlet
    public static class ChatWebSocketServlet extends WebSocketServlet {
        @Override
        public void configure(WebSocketServletFactory factory) {
            factory.register(WebSocketChat.class);
        }
    }
}