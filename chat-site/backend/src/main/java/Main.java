import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(WebSocketChat.class);
            }
        };
        server.setHandler(wsHandler);
        server.start();
        System.out.println("Chat server running on port 8080...");
        server.join();
    }
}
