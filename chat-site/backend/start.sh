#!/bin/sh
# Launch the Java WebSocket server in background
java -jar /app/chat-server.jar &

# Then start nginx in foreground
nginx -g "daemon off;"
