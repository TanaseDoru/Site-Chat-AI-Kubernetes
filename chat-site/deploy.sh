#!/bin/bash
echo "Building and starting the chat system..."
docker-compose up --build -d
echo "Chat system is running!"
echo "Frontend: http://localhost:90"
echo "Backend WebSocket: ws://localhost:88/chat"
echo "MongoDB: localhost:27017"