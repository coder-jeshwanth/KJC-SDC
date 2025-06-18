package day3task.model;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Student {
    public String id;
    public String name;
    public String email;

    // Constructor
    public Student() {}

    public Student(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Convert Student object to MongoDB Document
    public Document toDocument() {
        Document doc = new Document("name", name)
                .append("email", email);
        if (id != null) {
            doc.append("_id", new ObjectId(id));
        }
        return doc;
    }

    // Create Student object from MongoDB Document
    public static Student fromDocument(Document doc) {
        Student student = new Student();
        student.id = doc.getObjectId("_id").toHexString();
        student.name = doc.getString("name");
        student.email = doc.getString("email");
        return student;
    }
}
