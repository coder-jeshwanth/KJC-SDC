package day11.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import day11.model.Booking;
import day11.model.Event;
import day11.model.User;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;

public class MongoDbService {
    private final MongoClient mongoClient;

    public MongoDbService(Vertx vertx, JsonObject config) {
        this.mongoClient = MongoClient.createShared(vertx, config.getJsonObject("mongodb"));
    }

    // User operations
    public Future<String> createUser(User user) {
        String hashedPassword = BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray());
        user.setPassword(hashedPassword);
        return mongoClient.insert("users", user.toJson());
    }

    public Future<JsonObject> findUserByEmail(String email) {
        JsonObject query = new JsonObject().put("email", email);
        return mongoClient.findOne("users", query, null);
    }

    public boolean validatePassword(String password, String hashedPassword) {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified;
    }

    // Event operations
    public Future<List<JsonObject>> findAllEvents() {
        return mongoClient.find("events", new JsonObject());
    }

    public Future<String> createEvent(Event event) {
        return mongoClient.insert("events", event.toJson());
    }

    public Future<JsonObject> findEventById(String id) {
        JsonObject query = new JsonObject().put("_id", id);
        return mongoClient.findOne("events", query, null);
    }

    public Future<JsonObject> updateEventTokens(String eventId, int newAvailableTokens) {
        JsonObject query = new JsonObject().put("_id", eventId);
        JsonObject update = new JsonObject().put("$set",
            new JsonObject().put("availableTokens", newAvailableTokens));
        return mongoClient.findOneAndUpdate("events", query, update);
    }

    // Booking operations
    public Future<String> createBooking(Booking booking) {
        booking.setTokenCode(generateTokenCode());
        return mongoClient.insert("bookings", booking.toJson());
    }

    public Future<JsonObject> findBookingByToken(String tokenCode) {
        JsonObject query = new JsonObject().put("tokenCode", tokenCode);
        return mongoClient.findOne("bookings", query, null);
    }

    private String generateTokenCode() {
        return RandomStringUtils.randomAlphanumeric(8).toUpperCase();
    }
}

