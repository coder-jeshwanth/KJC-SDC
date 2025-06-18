package com.day3task.model;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Course {
    public String id;
    public String title;
    public String instructor;

    // Constructors
    public Course() {}

    public Course(String title, String instructor) {
        this.title = title;
        this.instructor = instructor;
    }

    // Convert Course object to MongoDB Document
    public Document toDocument() {
        Document doc = new Document("title", title)
                .append("instructor", instructor);
        if (id != null) {
            doc.append("_id", new ObjectId(id));
        }
        return doc;
    }

    // Create Course object from MongoDB Document
    public static Course fromDocument(Document doc) {
        Course course = new Course();
        course.id = doc.getObjectId("_id").toHexString();
        course.title = doc.getString("title");
        course.instructor = doc.getString("instructor");
        return course;
    }
}
