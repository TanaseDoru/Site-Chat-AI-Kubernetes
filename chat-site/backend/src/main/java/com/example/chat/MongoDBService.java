package com.example.chat;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MongoDBService {
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> messagesCollection;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MongoDBService() {
        // Connect to MongoDB
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("chat");
        messagesCollection = database.getCollection("messages");
    }

    public void saveMessage(String username, String message) {
        LocalDateTime timestamp = LocalDateTime.now();
        Document document = new Document()
                .append("username", username)
                .append("message", message)
                .append("timestamp", Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant()));
        messagesCollection.insertOne(document);
    }

    public List<String> getRecentMessages() {
        List<Document> messages = messagesCollection
                .find()
                .sort(Sorts.ascending("timestamp"))
                .limit(50)
                .into(new ArrayList<>());

        List<String> formattedMessages = new ArrayList<>();
        for (Document doc : messages) {
            Date date = doc.get("timestamp", Date.class);
            LocalDateTime timestamp = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            String formattedTimestamp = timestamp.format(formatter);
            String formattedMessage = String.format("[%s] %s: %s", 
                formattedTimestamp,
                doc.getString("username"),
                doc.getString("message")
            );
            formattedMessages.add(formattedMessage);
        }
        return formattedMessages;
    }
} 