package day11;

import day11.model.Booking;
import day11.model.Event;
import day11.model.User;
import day11.service.EmailService;
import day11.service.MongoDbService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import org.apache.commons.lang3.RandomStringUtils;

public class MainVerticle extends AbstractVerticle {
    private MongoDbService mongoDbService;
    private EmailService emailService;
    private JWTAuth jwtAuth;

    @Override
    public void start(Promise<Void> startPromise) {
        // Configuration
        JsonObject config = new JsonObject()
            .put("mongodb", new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "ticketing"))
            .put("email", new JsonObject()
                .put("host", "smtp.gmail.com")
                .put("port", 587)
                .put("username", "annamalajeshwanth@gmail.com")  // Replace with your Gmail address
                .put("password", "cdvt zerx krgc qtdr")  // Replace with your Gmail App Password
                .put("from", "annamalajeshwanth@gmail.com"));  // Same as username

        try {
            // Initialize services
            mongoDbService = new MongoDbService(vertx, config);
            emailService = new EmailService(config);

            // Setup JWT Auth
            JWTAuthOptions jwtAuthOptions = new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                    .setAlgorithm("HS256")
                    .setSymmetric(true)
                    .setBuffer("jeshy")); // Replace with a secure secret key
            jwtAuth = JWTAuth.create(vertx, jwtAuthOptions);

            // Create Router
            Router router = Router.router(vertx);
            router.route().handler(BodyHandler.create());

            // Public endpoints
            router.post("/api/register").handler(this::handleRegistration);
            router.post("/api/login").handler(this::handleLogin);

            // Protected endpoints
            router.route("/api/events*").handler(JWTAuthHandler.create(jwtAuth));
            router.route("/api/bookings*").handler(JWTAuthHandler.create(jwtAuth));

            router.get("/api/events").handler(this::handleListEvents);
            router.post("/api/events").handler(this::handleCreateEvent);
            router.post("/api/bookings").handler(this::handleCreateBooking);

            // Add a root endpoint for testing
            router.get("/").handler(ctx -> {
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("message", "Server is running").encode());
            });

            // Start the server
            vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, "0.0.0.0", http -> {  // Changed to listen on all interfaces
                    if (http.succeeded()) {
                        System.out.println("HTTP server started on port 8080");
                        System.out.println("Try accessing http://localhost:8080 in your browser");
                        startPromise.complete();
                    } else {
                        System.err.println("Failed to start server: " + http.cause().getMessage());
                        startPromise.fail(http.cause());
                    }
                });
        } catch (Exception e) {
            System.err.println("Error during server setup: " + e.getMessage());
            e.printStackTrace();
            startPromise.fail(e);
        }
    }

    private void handleRegistration(io.vertx.ext.web.RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String email = body.getString("email");
        String name = body.getString("name");
        String password = RandomStringUtils.randomAlphanumeric(10);

        User user = new User(email, name, password);

        mongoDbService.findUserByEmail(email)
            .onSuccess(existingUser -> {
                if (existingUser != null) {
                    ctx.response()
                        .setStatusCode(400)
                        .end(new JsonObject().put("error", "Email already registered").encode());
                    return;
                }

                mongoDbService.createUser(user)
                    .onSuccess(id -> {
                        emailService.sendWelcomeEmail(email, name, password);
                        ctx.response()
                            .setStatusCode(201)
                            .end(new JsonObject().put("message", "User registered successfully").encode());
                    })
                    .onFailure(err -> ctx.fail(500, err));
            })
            .onFailure(err -> ctx.fail(500, err));
    }

    private void handleLogin(io.vertx.ext.web.RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String email = body.getString("email");
        String password = body.getString("password");

        System.out.println("Login attempt for email: " + email); // Debug log

        mongoDbService.findUserByEmail(email)
            .onSuccess(user -> {
                if (user == null) {
                    System.out.println("User not found for email: " + email); // Debug log
                    ctx.response()
                        .setStatusCode(401)
                        .end(new JsonObject().put("error", "Invalid credentials").encode());
                    return;
                }

                System.out.println("Found user: " + user.encode()); // Debug log

                boolean isValidPassword = mongoDbService.validatePassword(password, user.getString("password"));
                System.out.println("Password validation result: " + isValidPassword); // Debug log

                if (!isValidPassword) {
                    ctx.response()
                        .setStatusCode(401)
                        .end(new JsonObject().put("error", "Invalid credentials").encode());
                    return;
                }

                String token = jwtAuth.generateToken(
                    new JsonObject()
                        .put("sub", user.getString("_id"))
                        .put("email", email)
                );

                ctx.response()
                    .end(new JsonObject()
                        .put("token", token)
                        .put("message", "Login successful")
                        .encode());
            })
            .onFailure(err -> {
                System.err.println("Login error: " + err.getMessage()); // Debug log
                ctx.fail(500, err);
            });
    }

    private void handleListEvents(io.vertx.ext.web.RoutingContext ctx) {
        mongoDbService.findAllEvents()
            .onSuccess(events -> {
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("events", events).encode());
            })
            .onFailure(err -> ctx.fail(500, err));
    }

    private void handleCreateEvent(io.vertx.ext.web.RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        Event event = new Event(
            body.getString("name"),
            body.getString("description"),
            java.time.LocalDateTime.parse(body.getString("eventDate")),
            body.getInteger("totalTokens"),
            body.getDouble("price")
        );

        mongoDbService.createEvent(event)
            .onSuccess(id -> {
                ctx.response()
                    .setStatusCode(201)
                    .end(new JsonObject()
                        .put("message", "Event created successfully")
                        .put("id", id)
                        .encode());
            })
            .onFailure(err -> ctx.fail(500, err));
    }

    private void handleCreateBooking(io.vertx.ext.web.RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String eventId = body.getString("eventId");
        String userId = ctx.user().principal().getString("sub");

        mongoDbService.findEventById(eventId)
            .onSuccess(event -> {
                if (event == null) {
                    ctx.response()
                        .setStatusCode(404)
                        .end(new JsonObject().put("error", "Event not found").encode());
                    return;
                }

                int availableTokens = event.getInteger("availableTokens");
                if (availableTokens <= 0) {
                    ctx.response()
                        .setStatusCode(400)
                        .end(new JsonObject().put("error", "No available tokens").encode());
                    return;
                }

                Booking booking = new Booking(userId, eventId, null);
                mongoDbService.createBooking(booking)
                    .onSuccess(bookingId -> {
                        mongoDbService.updateEventTokens(eventId, availableTokens - 1)
                            .onSuccess(updated -> {
                                mongoDbService.findUserByEmail(ctx.user().principal().getString("email"))
                                    .onSuccess(user -> {
                                        emailService.sendBookingConfirmation(
                                            user.getString("email"),
                                            user.getString("name"),
                                            event.getString("name"),
                                            booking.getTokenCode()
                                        );
                                        ctx.response()
                                            .setStatusCode(201)
                                            .end(new JsonObject()
                                                .put("message", "Booking successful")
                                                .put("tokenCode", booking.getTokenCode())
                                                .encode());
                                    });
                            });
                    })
                    .onFailure(err -> ctx.fail(500, err));
            })
            .onFailure(err -> ctx.fail(500, err));
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle())
            .onSuccess(id -> System.out.println("Application started successfully"))
            .onFailure(err -> {
                System.err.println("Failed to start application: " + err.getMessage());
                err.printStackTrace();
            });
    }
}
