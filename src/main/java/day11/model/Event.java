package day11.model;

import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;

public class Event {
    private String id;
    private String name;
    private String description;
    private LocalDateTime eventDate;
    private int totalTokens;
    private int availableTokens;
    private double price;

    public Event() {}

    public Event(String name, String description, LocalDateTime eventDate, int totalTokens, double price) {
        this.name = name;
        this.description = description;
        this.eventDate = eventDate;
        this.totalTokens = totalTokens;
        this.availableTokens = totalTokens;
        this.price = price;
    }

    public Event(JsonObject json) {
        this.id = json.getString("_id");
        this.name = json.getString("name");
        this.description = json.getString("description");
        this.eventDate = LocalDateTime.parse(json.getString("eventDate"));
        this.totalTokens = json.getInteger("totalTokens");
        this.availableTokens = json.getInteger("availableTokens");
        this.price = json.getDouble("price");
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("name", name)
                .put("description", description)
                .put("eventDate", eventDate.toString())
                .put("totalTokens", totalTokens)
                .put("availableTokens", availableTokens)
                .put("price", price);
        if (id != null) {
            json.put("_id", id);
        }
        return json;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    public int getTotalTokens() { return totalTokens; }
    public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
    public int getAvailableTokens() { return availableTokens; }
    public void setAvailableTokens(int availableTokens) { this.availableTokens = availableTokens; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
