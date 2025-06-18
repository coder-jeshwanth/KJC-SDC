package com.day3task.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoUtil {
    private static final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    public static final MongoDatabase db = mongoClient.getDatabase("test3day3");

    public static MongoCollection<Document> getCollection(String name) {
        return db.getCollection(name);
    }
}
