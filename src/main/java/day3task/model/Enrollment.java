package com.day3task.model;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Enrollment {
    public String id;
    public String type; // "embedded" or "referenced"

    // For embedded
    public Student embeddedStudent;
    public Course embeddedCourse;

    // For referenced
    public String studentId;
    public String courseId;

    // Convert to MongoDB document
    public Document toDocument() {
        Document doc = new Document("type", type);

        if (type.equals("embedded")) {
            doc.append("student", embeddedStudent.toDocument());
            doc.append("course", embeddedCourse.toDocument());
        } else if (type.equals("referenced")) {
            doc.append("studentId", new ObjectId(studentId));
            doc.append("courseId", new ObjectId(courseId));
        }

        return doc;
    }

    // Optional: Create from MongoDB document
    public static Enrollment fromDocument(Document doc) {
        Enrollment enrollment = new Enrollment();
        enrollment.id = doc.getObjectId("_id") != null ? doc.getObjectId("_id").toHexString() : null;
        enrollment.type = doc.getString("type");

        if ("embedded".equals(enrollment.type)) {
            Document studentDoc = (Document) doc.get("student");
            Document courseDoc = (Document) doc.get("course");

            Student s = new Student();
            s.id = studentDoc.getObjectId("_id").toHexString();
            s.name = studentDoc.getString("name");
            s.email = studentDoc.getString("email");

            Course c = new Course();
            c.id = courseDoc.getObjectId("_id").toHexString();
            c.title = courseDoc.getString("title");
            c.instructor = courseDoc.getString("instructor");

            enrollment.embeddedStudent = s;
            enrollment.embeddedCourse = c;

        } else if ("referenced".equals(enrollment.type)) {
            enrollment.studentId = doc.getObjectId("studentId").toHexString();
            enrollment.courseId = doc.getObjectId("courseId").toHexString();
        }

        return enrollment;
    }
}
