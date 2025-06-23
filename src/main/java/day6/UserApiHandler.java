package day6;

import io.vertx.ext.mongo.MongoClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonArray;

public class UserApiHandler {

    private static MongoClient mongoClient;

    public static void init(MongoClient client) {
        mongoClient = client;
    }

    public static void addUser(RoutingContext ctx) {
        JsonObject user = ctx.getBodyAsJson();
        String email = user.getString("email");

        // Check if a user with the same email exists
        JsonObject query = new JsonObject().put("email", email);
        mongoClient.findOne("users", query, null, lookup -> {
            if (lookup.succeeded()) {
                if (lookup.result() != null) {
                    // Email already exists
                    ctx.response().setStatusCode(409).end("Email already exists");
                } else {
                    // Email is unique, insert the user
                    mongoClient.insert("users", user, res -> {
                        if (res.succeeded()) {
                            ctx.response().setStatusCode(201).end("User added with id: " + res.result());
                        } else {
                            ctx.response().setStatusCode(500).end("Failed to add user");
                        }
                    });
                }
            } else {
                ctx.response().setStatusCode(500).end("Error checking existing user");
            }
        });
    }


    public static void getUser(RoutingContext ctx) {
        String email = ctx.pathParam("email");  // Get email from path
        JsonObject query = new JsonObject().put("email", email);

        mongoClient.findOne("users", query, null, res -> {
            if (res.succeeded()) {
                JsonObject user = res.result();
                if (user == null) {
                    ctx.response().setStatusCode(404).end("User not found");
                } else {
                    ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(user.encodePrettily());
                }
            } else {
                ctx.response().setStatusCode(500).end("Failed to fetch user");
            }
        });
    }

    public static void updateUser(RoutingContext ctx) {
        String email = ctx.pathParam("email");  // Get email from path param
        JsonObject update = ctx.getBodyAsJson(); // New data to update

        JsonObject query = new JsonObject().put("email", email); // Match by email
        JsonObject updateQuery = new JsonObject().put("$set", update);

        mongoClient.updateCollection("users", query, updateQuery, res -> {
            if (res.succeeded()) {
                if (res.result().getDocModified() == 0) {
                    ctx.response().setStatusCode(404).end("User not found");
                } else {
                    ctx.response().end("User updated");
                }
            } else {
                ctx.response().setStatusCode(500).end("Update failed");
            }
        });
    }


    public static void deleteUser(RoutingContext ctx) {
        String email = ctx.pathParam("email");  // Get email from path
        JsonObject query = new JsonObject().put("email", email);  // Match by email

        mongoClient.removeDocument("users", query, res -> {
            if (res.succeeded()) {
                if (res.result().getRemovedCount() == 0) {
                    ctx.response().setStatusCode(404).end("User not found");
                } else {
                    ctx.response().end("User deleted");
                }
            } else {
                ctx.response().setStatusCode(500).end("Failed to delete user");
            }
        });
    }

}
