package com.day3task;

import com.day3task.model.Student;
import com.day3task.model.Course;
import com.day3task.model.Enrollment;
import com.day3task.util.MongoUtil;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        MongoCollection<Document> students = MongoUtil.getCollection("students");
        MongoCollection<Document> courses = MongoUtil.getCollection("courses");
        MongoCollection<Document> enrollments = MongoUtil.getCollection("enrollments");

        // Ensure uniqueness
        students.createIndex(Indexes.ascending("email"), new IndexOptions().unique(true));
        courses.createIndex(Indexes.ascending("title"), new IndexOptions().unique(true));

        Scanner scanner = new Scanner(System.in);

        // Insert Students
        System.out.print("How many students do you want to add? ");
        int numStudents = Integer.parseInt(scanner.nextLine());

        for (int i = 0; i < numStudents; i++) {
            System.out.println("Enter student name: ");
            String name = scanner.nextLine();

            System.out.println("Enter student email: ");
            String email = scanner.nextLine();

            Document existingStudent = students.find(Filters.eq("email", email)).first();
            if (existingStudent == null) {
                Document newStudent = new Student(name, email).toDocument();
                students.insertOne(newStudent);
                System.out.println("✅ Inserted student: " + name);
            } else {
                System.out.println("⚠️ Student with email " + email + " already exists.");
            }
        }

        // Insert Courses
        System.out.print("\nHow many courses do you want to add? ");
        int numCourses = Integer.parseInt(scanner.nextLine());

        for (int i = 0; i < numCourses; i++) {
            System.out.println("Enter course title: ");
            String title = scanner.nextLine();

            System.out.println("Enter instructor name: ");
            String instructor = scanner.nextLine();

            Document existingCourse = courses.find(Filters.eq("title", title)).first();
            if (existingCourse == null) {
                Document newCourse = new Course(title, instructor).toDocument();
                courses.insertOne(newCourse);
                System.out.println("✅ Inserted course: " + title);
            } else {
                System.out.println("⚠️ Course with title " + title + " already exists.");
            }
        }

        // Enrollment
        System.out.print("\nDo you want to enroll a student? (yes/no): ");
        String enrollChoice = scanner.nextLine();

        if (enrollChoice.equalsIgnoreCase("yes")) {
            System.out.println("Enter student email: ");
            String studentEmail = scanner.nextLine();
            Document studentDoc = students.find(Filters.eq("email", studentEmail)).first();

            System.out.println("Enter course title: ");
            String courseTitle = scanner.nextLine();
            Document courseDoc = courses.find(Filters.eq("title", courseTitle)).first();

            if (studentDoc == null || courseDoc == null) {
                System.out.println("❌ Invalid student or course.");
            } else {
                System.out.print("Enter enrollment type (embedded/referenced): ");
                String type = scanner.nextLine().toLowerCase();

                ObjectId sId = studentDoc.getObjectId("_id");
                ObjectId cId = courseDoc.getObjectId("_id");

                if (type.equals("embedded")) {
                    boolean exists = enrollments.find(Filters.and(
                            Filters.eq("type", "embedded"),
                            Filters.eq("student._id", sId),
                            Filters.eq("course._id", cId)
                    )).first() != null;

                    if (!exists) {
                        Enrollment e = new Enrollment();
                        e.type = "embedded";
                        e.embeddedStudent = Student.fromDocument(studentDoc);
                        e.embeddedCourse = Course.fromDocument(courseDoc);
                        enrollments.insertOne(e.toDocument());
                        System.out.println("✅ Enrolled (embedded): " + e.embeddedStudent.name + " - " + e.embeddedCourse.title);
                    } else {
                        System.out.println("⚠️ Enrollment already exists.");
                    }
                } else if (type.equals("referenced")) {
                    boolean exists = enrollments.find(Filters.and(
                            Filters.eq("type", "referenced"),
                            Filters.eq("studentId", sId),
                            Filters.eq("courseId", cId)
                    )).first() != null;

                    if (!exists) {
                        Enrollment e = new Enrollment();
                        e.type = "referenced";
                        e.studentId = sId.toHexString();
                        e.courseId = cId.toHexString();
                        enrollments.insertOne(e.toDocument());
                        System.out.println("✅ Enrolled (referenced): " + studentDoc.getString("name") + " - " + courseDoc.getString("title"));
                    } else {
                        System.out.println("⚠️ Enrollment already exists.");
                    }
                } else {
                    System.out.println("❌ Invalid enrollment type.");
                }
            }
        }

        // update

        System.out.print("\nDo you want to update a student or course? (student/course/none): ");
        String updateType = scanner.nextLine();

        if (updateType.equalsIgnoreCase("student")) {
            System.out.print("Enter the email of the student you want to update: ");
            String email = scanner.nextLine();

            Document studentDoc = students.find(Filters.eq("email", email)).first();

            if (studentDoc != null) {
                System.out.print("Enter new name for the student: ");
                String newName = scanner.nextLine();

                ObjectId studentId = studentDoc.getObjectId("_id");

                // 1. Update students collection
                students.updateOne(Filters.eq("email", email), Updates.set("name", newName));
                System.out.println("✅ Student name updated in students collection.");

                // Detect enrollment type
                long embeddedCount = enrollments.countDocuments(Filters.and(
                        Filters.eq("type", "embedded"),
                        Filters.eq("student.email", email)
                ));

                long referencedCount = enrollments.countDocuments(Filters.and(
                        Filters.eq("type", "referenced"),
                        Filters.eq("studentId", studentId)
                ));

                System.out.println("\n📊 Enrollment Status for " + email + ":");
                System.out.println(" - Embedded enrollments: " + embeddedCount);
                System.out.println(" - Referenced enrollments: " + referencedCount);

                // 2. Ask for switch type
                System.out.print("Do you want to switch from (embedded → referenced / referenced → embedded)? Type 'embedded' or 'referenced': ");
                String switchType = scanner.nextLine();

                if (switchType.equalsIgnoreCase("embedded")) {
                    // Convert all referenced enrollments for this student into embedded
                    List<Document> referencedEnrollments = enrollments.find(
                            Filters.and(Filters.eq("type", "referenced"), Filters.eq("studentId", studentId))
                    ).into(new ArrayList<>());

                    for (Document doc : referencedEnrollments) {
                        ObjectId courseId = doc.getObjectId("courseId");
                        Document courseDoc = courses.find(Filters.eq("_id", courseId)).first();

                        if (courseDoc != null) {
                            Enrollment embeddedEnrollment = new Enrollment();
                            embeddedEnrollment.type = "embedded";
                            embeddedEnrollment.embeddedStudent = Student.fromDocument(studentDoc);
                            embeddedEnrollment.embeddedCourse = Course.fromDocument(courseDoc);

                            enrollments.insertOne(embeddedEnrollment.toDocument());
                        }

                        enrollments.deleteOne(Filters.eq("_id", doc.getObjectId("_id")));
                    }

                    System.out.println("✅ Converted referenced enrollments to embedded for student.");

                } else if (switchType.equalsIgnoreCase("referenced")) {
                    // Convert all embedded enrollments for this student into referenced
                    List<Document> embeddedEnrollments = enrollments.find(
                            Filters.and(Filters.eq("type", "embedded"), Filters.eq("student.email", email))
                    ).into(new ArrayList<>());

                    for (Document doc : embeddedEnrollments) {
                        Document courseDoc = (Document) doc.get("course");
                        String courseTitle = courseDoc.getString("title");

                        Document matchedCourse = courses.find(Filters.eq("title", courseTitle)).first();

                        if (matchedCourse != null) {
                            Enrollment referencedEnrollment = new Enrollment();
                            referencedEnrollment.type = "referenced";
                            referencedEnrollment.studentId = studentId.toHexString();
                            referencedEnrollment.courseId = matchedCourse.getObjectId("_id").toHexString();

                            enrollments.insertOne(referencedEnrollment.toDocument());
                        }

                        enrollments.deleteOne(Filters.eq("_id", doc.getObjectId("_id")));
                    }

                    System.out.println("✅ Converted embedded enrollments to referenced for student.");

                } else {
                    System.out.println("❌ Invalid switch type.");
                }

            } else {
                System.out.println("❌ No student found with that email.");
            }

        } else if (updateType.equalsIgnoreCase("course")) {
            System.out.print("Enter the course title you want to update: ");
            String title = scanner.nextLine();

            Document courseDoc = courses.find(Filters.eq("title", title)).first();

            if (courseDoc != null) {
                ObjectId courseId = courseDoc.getObjectId("_id");

                System.out.print("Do you want to update title, instructor, or both? ");
                String choice = scanner.nextLine().toLowerCase();

                boolean updated = false;

                if (choice.equals("title") || choice.equals("both")) {
                    System.out.print("Enter new title for the course: ");
                    String newTitle = scanner.nextLine();

                    courses.updateOne(Filters.eq("_id", courseId), Updates.set("title", newTitle));
                    System.out.println("✅ Course title updated.");
                    updated = true;

                    // Update embedded enrollments
                    enrollments.updateMany(
                            Filters.and(
                                    Filters.eq("type", "embedded"),
                                    Filters.eq("course.title", title)
                            ),
                            Updates.set("course.title", newTitle)
                    );
                    System.out.println("✅ Embedded enrollments updated with new title.");
                }

                if (choice.equals("instructor") || choice.equals("both")) {
                    System.out.print("Enter new instructor for the course: ");
                    String newInstructor = scanner.nextLine();

                    courses.updateOne(Filters.eq("_id", courseId), Updates.set("instructor", newInstructor));
                    System.out.println("✅ Instructor updated in courses collection.");
                    updated = true;

                    // Update embedded enrollments
                    enrollments.updateMany(
                            Filters.and(
                                    Filters.eq("type", "embedded"),
                                    Filters.eq("course.title", courseDoc.getString("title"))  // still using old title if not changed
                            ),
                            Updates.set("course.instructor", newInstructor)
                    );
                    System.out.println("✅ Embedded enrollments updated with new instructor.");
                }

                if (!updated) {
                    System.out.println("⚠️ No updates performed.");
                }
            } else {
                System.out.println("❌ No course found with that title.");
            }
        }




        // Print Enrollments
        System.out.println("\n--- Embedded Enrollments ---");
        for (Document doc : enrollments.find(Filters.eq("type", "embedded"))) {
            Enrollment e = Enrollment.fromDocument(doc);
            System.out.println("Student: " + e.embeddedStudent.name + ", Course: " + e.embeddedCourse.title);
        }

        System.out.println("\n--- Referenced Enrollments ---");
        for (Document doc : enrollments.find(Filters.eq("type", "referenced"))) {
            ObjectId studentId = doc.getObjectId("studentId");
            ObjectId courseId = doc.getObjectId("courseId");

            Document sDoc = students.find(Filters.eq("_id", studentId)).first();
            Document cDoc = courses.find(Filters.eq("_id", courseId)).first();

            if (sDoc != null && cDoc != null) {
                System.out.println("Student: " + sDoc.getString("name") + ", Course: " + cDoc.getString("title"));
            }
        }


        // find my email

        // Find details by email
        System.out.print("\nDo you want to find details of a student by email? (yes/no): ");
        String findChoice = scanner.nextLine();

        if (findChoice.equalsIgnoreCase("yes")) {
            System.out.print("Enter the student's email: ");
            String searchEmail = scanner.nextLine();

            Document studentDoc = students.find(Filters.eq("email", searchEmail)).first();

            if (studentDoc != null) {
                ObjectId studentId = studentDoc.getObjectId("_id");
                System.out.println("\n📄 Student Details:");
                System.out.println("Name  : " + studentDoc.getString("name"));
                System.out.println("Email : " + searchEmail);

                // Check embedded enrollments
                List<Document> embedded = enrollments.find(Filters.and(
                        Filters.eq("type", "embedded"),
                        Filters.eq("student.email", searchEmail)
                )).into(new ArrayList<>());

                // Check referenced enrollments
                List<Document> referenced = enrollments.find(Filters.and(
                        Filters.eq("type", "referenced"),
                        Filters.eq("studentId", studentId)
                )).into(new ArrayList<>());

                System.out.println("\n📦 Enrollments:");

                if (embedded.isEmpty() && referenced.isEmpty()) {
                    System.out.println("❌ No enrollments found.");
                } else {
                    if (!embedded.isEmpty()) {
                        System.out.println("🔗 Embedded Enrollments:");
                        for (Document doc : embedded) {
                            Document courseDoc = (Document) doc.get("course");
                            System.out.println(" - Course: " + courseDoc.getString("title") + ", Instructor: " + courseDoc.getString("instructor"));
                        }
                    }

                    if (!referenced.isEmpty()) {
                        System.out.println("🔗 Referenced Enrollments:");
                        for (Document doc : referenced) {
                            ObjectId courseId = doc.getObjectId("courseId");
                            Document course = courses.find(Filters.eq("_id", courseId)).first();
                            if (course != null) {
                                System.out.println(" - Course: " + course.getString("title") + ", Instructor: " + course.getString("instructor"));
                            }
                        }
                    }
                }

            } else {
                System.out.println("❌ No student found with that email.");
            }
        }

    }



}
