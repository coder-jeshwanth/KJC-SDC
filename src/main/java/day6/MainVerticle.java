package day6;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

    private MongoClient mongoClient;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }

    @Override
    public void start() {
        // MongoDB config
        JsonObject config = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "testingvertx");


        mongoClient = MongoClient.createShared(vertx, config);

        // Initialize router
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Health check
        router.get("/test").handler(ctx -> {
            ctx.response().end("Vert.x is working!");
        });

        // Pass mongoClient to UserApiHandler
        UserApiHandler.init(mongoClient);

        router.post("/users").handler(UserApiHandler::addUser);
        router.get("/users/email/:email").handler(UserApiHandler::getUser);
        router.put("/users/email/:email").handler(UserApiHandler::updateUser);
        router.delete("/users/email/:email").handler(UserApiHandler::deleteUser);


        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888, http -> {
                    if (http.succeeded()) {
                        System.out.println("HTTP server started on port 8888");
                    } else {
                        System.out.println("Failed to start server: " + http.cause());
                    }
                });
    }
}
