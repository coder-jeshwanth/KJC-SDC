package day10.service;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import day10.model.Course;
import day10.model.Student;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDbService {
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public MongoDbService(Vertx vertx) {
        String connectionString = "mongodb://localhost:27017";
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .build();
        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase("elective_registration");
    }

    public Future<String> registerStudent(Student student) {
        return Future.future(promise -> {
            Document doc = new Document()
                    .append("email", student.getEmail())
                    .append("password", student.getPassword())
                    .append("name", student.getName());
            database.getCollection("students").insertOne(doc);
            promise.complete(doc.getObjectId("_id").toString());
        });
    }

    public Future<Student> findStudentByEmail(String email) {
        return Future.future(promise -> {
            Document doc = database.getCollection("students").find(new Document("email", email)).first();
            if (doc != null) {
                String id = doc.getObjectId("_id").toString();
                String password = doc.getString("password");
                String name = doc.getString("name");
                Student student = new Student(id, email, password, name);
                promise.complete(student);
            } else {
                promise.complete(null);
            }
        });
    }

    public Future<List<Course>> getAllCourses() {
        return Future.future(promise -> {
            List<Course> courses = new ArrayList<>();
            for (Document doc : database.getCollection("courses").find()) {
                String id = doc.getObjectId("_id").toString();
                String name = doc.getString("name");
                String description = doc.getString("description");
                int totalSeats = doc.getInteger("totalSeats", 0);
                int availableSeats = doc.getInteger("availableSeats", totalSeats);
                Course course = new Course(id, name, description, totalSeats);
                course.setAvailableSeats(availableSeats);
                courses.add(course);
            }
            promise.complete(courses);
        });
    }

    public Future<Boolean> registerStudentToCourse(String email, String password, String courseId) {
        return Future.future(promise -> {
            // Authenticate student
            Document studentDoc = database.getCollection("students").find(new Document("email", email)).first();
            if (studentDoc == null || !studentDoc.getString("password").equals(password)) {
                promise.complete(false);
                return;
            }
            // Check course
            Document courseDoc = database.getCollection("courses").find(new Document("_id", new org.bson.types.ObjectId(courseId))).first();
            if (courseDoc == null) {
                promise.complete(false);
                return;
            }
            int availableSeats = courseDoc.getInteger("availableSeats", 0);
            if (availableSeats <= 0) {
                promise.complete(false);
                return;
            }
            // Register student for course
            List<String> registeredCourses = (List<String>) studentDoc.get("registeredCourses");
            if (registeredCourses == null) registeredCourses = new ArrayList<>();
            if (registeredCourses.contains(courseId)) {
                promise.complete(false);
                return;
            }
            registeredCourses.add(courseId);
            database.getCollection("students").updateOne(
                new Document("_id", studentDoc.getObjectId("_id")),
                new Document("$set", new Document("registeredCourses", registeredCourses))
            );
            // Decrement availableSeats
            database.getCollection("courses").updateOne(
                new Document("_id", courseDoc.getObjectId("_id")),
                new Document("$inc", new Document("availableSeats", -1))
            );
            promise.complete(true);
        });
    }

    public List<String> getRegisteredCoursesByEmail(String email) {
        Document studentDoc = database.getCollection("students").find(new Document("email", email)).first();
        if (studentDoc != null && studentDoc.get("registeredCourses") != null) {
            return (List<String>) studentDoc.get("registeredCourses");
        }
        return new ArrayList<>();
    }

    public List<Course> getCoursesByIds(List<String> courseIds) {
        List<Course> courses = new ArrayList<>();
        for (String id : courseIds) {
            Document doc = database.getCollection("courses").find(new Document("_id", new org.bson.types.ObjectId(id))).first();
            if (doc != null) {
                String name = doc.getString("name");
                String description = doc.getString("description");
                int totalSeats = doc.getInteger("totalSeats", 0);
                int availableSeats = doc.getInteger("availableSeats", totalSeats);
                Course course = new Course(id, name, description, totalSeats);
                course.setAvailableSeats(availableSeats);
                courses.add(course);
            }
        }
        return courses;
    }

    // Additional database operations
}