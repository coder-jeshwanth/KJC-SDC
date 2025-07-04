package day11.model;

import io.vertx.core.json.JsonObject;

public class User {
    private String id;
    private String email;
    private String name;
    private String password;

    public User() {}

    public User(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
    }

    public User(JsonObject json) {
        this.id = json.getString("_id");
        this.email = json.getString("email");
        this.name = json.getString("name");
        this.password = json.getString("password");
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("email", email)
                .put("name", name)
                .put("password", password);
        if (id != null) {
            json.put("_id", id);
        }
        return json;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
