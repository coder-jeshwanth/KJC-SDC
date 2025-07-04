package day10;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import day10.model.Student;
import org.apache.commons.lang3.RandomStringUtils;
import day10.service.EmailService;
import day10.service.MongoDbService;

public class MainVerticle extends AbstractVerticle {
    private MongoDbService mongoService;
    private EmailService emailService;
    private Router router;

    @Override
    public void start(Promise<Void> startPromise) {
        mongoService = new MongoDbService(vertx);
        emailService = new EmailService();
        router = Router.router(vertx);

        // Enable request body parsing
        router.route().handler(BodyHandler.create());

        // Define routes
        setupRoutes();

        // Create HTTP server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, http -> {
                    if (http.succeeded()) {
                        startPromise.complete();
                        System.out.println("HTTP server started on port 8080");
                    } else {
                        startPromise.fail(http.cause());
                    }
                });
    }

    private void setupRoutes() {
        // Student registration
        router.post("/api/register").handler(this::handleRegistration);
        // Student login
        router.post("/api/login").handler(this::handleLogin);
        // Get available courses
        router.get("/api/courses").handler(this::handleGetCourses);
        // Register for a course
        router.post("/api/courses/:courseId/register").handler(this::handleCourseRegistration);
        // Get registered courses for a student
        router.get("/api/students/:email/registered-courses").handler(this::handleGetRegisteredCourses);
    }


    private void handleRegistration(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String email = body.getString("email");
        String name = body.getString("name");

        // Generate random password
        String password = generateRandomPassword();
        String id = java.util.UUID.randomUUID().toString();

        Student student = new Student(id, email, password, name);

        mongoService.registerStudent(student)
                .onSuccess(idResult -> {
                    emailService.sendRegistrationEmail(email, password);
                    ctx.response()
                            .setStatusCode(201)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject().put("message", "Registration successful").encode());
                })
                .onFailure(err -> {
                    ctx.response()
                            .setStatusCode(500)
                            .end(new JsonObject().put("error", err.getMessage()).encode());
                });
    }

    private String generateRandomPassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    private void handleLogin(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String email = body.getString("email");
        String password = body.getString("password");

        mongoService.findStudentByEmail(email).onSuccess(student -> {
            if (student == null) {
                ctx.response().setStatusCode(401)
                    .end(new JsonObject().put("error", "Invalid email or password").encode());
            } else if (!student.getPassword().equals(password)) {
                ctx.response().setStatusCode(401)
                    .end(new JsonObject().put("error", "Invalid email or password").encode());
            } else {
                ctx.response().setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("message", "Login successful").put("studentId", student.getId()).encode());
            }
        }).onFailure(err -> {
            ctx.response().setStatusCode(500)
                .end(new JsonObject().put("error", err.getMessage()).encode());
        });
    }

    private void handleGetCourses(RoutingContext ctx) {
        String email = ctx.request().getParam("email");
        String password = ctx.request().getParam("password");
        if (email == null || password == null) {
            ctx.response().setStatusCode(400)
                .end(new JsonObject().put("error", "Email and password are required as query parameters").encode());
            return;
        }
        mongoService.findStudentByEmail(email).onSuccess(student -> {
            if (student == null || !student.getPassword().equals(password)) {
                ctx.response().setStatusCode(401)
                    .end(new JsonObject().put("error", "Invalid email or password").encode());
            } else {
                mongoService.getAllCourses().onSuccess(courses -> {
                    io.vertx.core.json.JsonArray arr = new io.vertx.core.json.JsonArray();
                    for (day10.model.Course c : courses) {
                        arr.add(new JsonObject()
                            .put("id", c.getId())
                            .put("name", c.getName())
                            .put("description", c.getDescription())
                            .put("totalSeats", c.getTotalSeats())
                            .put("availableSeats", c.getAvailableSeats()));
                    }
                    ctx.response().setStatusCode(200)
                        .putHeader("Content-Type", "application/json")
                        .end(arr.encode());
                }).onFailure(err -> {
                    ctx.response().setStatusCode(500)
                        .end(new JsonObject().put("error", err.getMessage()).encode());
                });
            }
        }).onFailure(err -> {
            ctx.response().setStatusCode(500)
                .end(new JsonObject().put("error", err.getMessage()).encode());
        });
    }

    private void handleCourseRegistration(RoutingContext ctx) {
        String courseId = ctx.pathParam("courseId");
        JsonObject body = ctx.getBodyAsJson();
        String email = body.getString("email");
        String password = body.getString("password");
        if (email == null || password == null || courseId == null) {
            ctx.response().setStatusCode(400)
                .end(new JsonObject().put("error", "Email, password, and courseId are required").encode());
            return;
        }
        mongoService.registerStudentToCourse(email, password, courseId).onSuccess(success -> {
            if (success) {
                ctx.response().setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("message", "Course registration successful").encode());
            } else {
                ctx.response().setStatusCode(400)
                    .end(new JsonObject().put("error", "Registration failed. Check credentials, course availability, or if already registered.").encode());
            }
        }).onFailure(err -> {
            ctx.response().setStatusCode(500)
                .end(new JsonObject().put("error", err.getMessage()).encode());
        });
    }

    private void handleGetRegisteredCourses(RoutingContext ctx) {
        String email = ctx.pathParam("email");
        String password = ctx.request().getParam("password");
        if (email == null || password == null) {
            ctx.response().setStatusCode(400)
                .end(new JsonObject().put("error", "Email and password are required").encode());
            return;
        }
        mongoService.findStudentByEmail(email).onSuccess(student -> {
            if (student == null || !student.getPassword().equals(password)) {
                ctx.response().setStatusCode(401)
                    .end(new JsonObject().put("error", "Invalid email or password").encode());
            } else {
                java.util.List<String> courseIds = mongoService.getRegisteredCoursesByEmail(email);
                java.util.List<day10.model.Course> courses = mongoService.getCoursesByIds(courseIds);
                io.vertx.core.json.JsonArray arr = new io.vertx.core.json.JsonArray();
                for (day10.model.Course c : courses) {
                    arr.add(new JsonObject()
                        .put("id", c.getId())
                        .put("name", c.getName())
                        .put("description", c.getDescription())
                        .put("totalSeats", c.getTotalSeats())
                        .put("availableSeats", c.getAvailableSeats()));
                }
                ctx.response().setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(arr.encode());
            }
        }).onFailure(err -> {
            ctx.response().setStatusCode(500)
                .end(new JsonObject().put("error", err.getMessage()).encode());
        });
    }

    public static void main(String[] args) {
        io.vertx.core.Vertx vertx = io.vertx.core.Vertx.vertx();
        vertx.deployVerticle(new MainVerticle(), res -> {
            if (res.succeeded()) {
                System.out.println("day10.MainVerticle deployed successfully.");
            } else {
                System.err.println("Failed to deploy day10.MainVerticle: " + res.cause());
            }
        });
    }
}