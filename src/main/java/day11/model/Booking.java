package day11.model;

import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;

public class Booking {
    private String id;
    private String userId;
    private String eventId;
    private String tokenCode;
    private LocalDateTime bookingDate;
    private boolean isUsed;

    public Booking() {}

    public Booking(String userId, String eventId, String tokenCode) {
        this.userId = userId;
        this.eventId = eventId;
        this.tokenCode = tokenCode;
        this.bookingDate = LocalDateTime.now();
        this.isUsed = false;
    }

    public Booking(JsonObject json) {
        this.id = json.getString("_id");
        this.userId = json.getString("userId");
        this.eventId = json.getString("eventId");
        this.tokenCode = json.getString("tokenCode");
        this.bookingDate = LocalDateTime.parse(json.getString("bookingDate"));
        this.isUsed = json.getBoolean("isUsed", false);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("userId", userId)
                .put("eventId", eventId)
                .put("tokenCode", tokenCode)
                .put("bookingDate", bookingDate.toString())
                .put("isUsed", isUsed);
        if (id != null) {
            json.put("_id", id);
        }
        return json;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getTokenCode() { return tokenCode; }
    public void setTokenCode(String tokenCode) { this.tokenCode = tokenCode; }
    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }
}
