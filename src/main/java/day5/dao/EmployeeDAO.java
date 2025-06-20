package day5.dao;

import com.mongodb.client.result.UpdateResult;
import day5.config.MongoDBConnection;
import day5.model.Employee;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EmployeeDAO {
    private final MongoCollection<Document> collection;

    public EmployeeDAO() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        collection = db.getCollection("employees");

        // Create a unique index on email
        collection.createIndex(Indexes.ascending("email"), new IndexOptions().unique(true));
    }

    // 1. Add Employee
    public boolean insertEmployee(Employee emp) {

        try {
            Document doc = new Document("name", emp.getName())
                    .append("email", emp.getEmail())
                    .append("department", emp.getDepartment())
                    .append("skills", emp.getSkills())
                    .append("joiningDate", emp.getJoiningDate().toString());
            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.out.println("Error inserting employee: " + e.getMessage());
            return false;
        }
    }

    // 2. Update Employee (only specified fields)
    public boolean updateEmployee(String email, Map<String, Object> updates) {
        Bson filter = Filters.eq("email", email);
        List<Bson> updateList = new ArrayList<>();

        updates.forEach((k, v) -> {
            if (k.equals("skills") && v instanceof List) {
                updateList.add(Updates.set("skills", v));
            } else if (k.equals("joiningDate") && v instanceof LocalDate) {
                updateList.add(Updates.set("joiningDate", v.toString()));
            } else {
                updateList.add(Updates.set(k, v));
            }
        });

        try {
            UpdateResult result = collection.updateOne(filter, Updates.combine(updateList));
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.out.println("Update error: " + e.getMessage());
            return false;
        }
    }

    // 3. Delete Employee
    public boolean deleteEmployeeByEmail(String email) {
        return collection.deleteOne(Filters.eq("email", email)).getDeletedCount() > 0;
    }

    public boolean deleteEmployeeById(String id) {
        return collection.deleteOne(Filters.eq("_id", new ObjectId(id))).getDeletedCount() > 0;
    }

    // 4. Search Operations
    public List<Employee> searchByName(String keyword) {
        Pattern regex = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
        FindIterable<Document> docs = collection.find(Filters.regex("name", regex));
        return mapToEmployees(docs);
    }

    public List<Employee> searchByDepartment(String department) {
        return mapToEmployees(collection.find(Filters.eq("department", department)));
    }

    public List<Employee> searchBySkill(String skill) {
        return mapToEmployees(collection.find(Filters.in("skills", skill)));
    }

    public List<Employee> searchByJoiningDateRange(LocalDate start, LocalDate end) {
        Bson filter = Filters.and(
                Filters.gte("joiningDate", start.toString()),
                Filters.lte("joiningDate", end.toString())
        );
        return mapToEmployees(collection.find(filter));
    }

    // 5. Pagination & Sorting
    public List<Employee> getPaginatedEmployees(int page, int size, String sortBy, boolean asc) {
        Bson sort = asc ? Sorts.ascending(sortBy) : Sorts.descending(sortBy);
        return mapToEmployees(collection.find().sort(sort).skip((page - 1) * size).limit(size));
    }

    // 6. Aggregation: Count per Department
    public Map<String, Long> getDepartmentStatistics() {
        List<Bson> pipeline = Arrays.asList(
                Aggregates.group("$department", Accumulators.sum("count", 1))
        );

        AggregateIterable<Document> result = collection.aggregate(pipeline);
        Map<String, Long> stats = new LinkedHashMap<>();
        for (Document doc : result) {
            String dept = doc.getString("_id");
            Number countNumber = doc.get("count", Number.class);  // fixes Integer/Long issue
            stats.put(dept, countNumber.longValue());
        }
        return stats;
    }


    // Helper: Convert Mongo documents to Java objects
    private List<Employee> mapToEmployees(FindIterable<Document> docs) {
        List<Employee> list = new ArrayList<>();
        for (Document doc : docs) {
            Employee emp = new Employee();
            emp.setId(doc.getObjectId("_id"));
            emp.setName(doc.getString("name"));
            emp.setEmail(doc.getString("email"));
            emp.setDepartment(doc.getString("department"));
            emp.setSkills((List<String>) doc.get("skills"));
            emp.setJoiningDate(LocalDate.parse(doc.getString("joiningDate")));
            list.add(emp);
        }
        return list;
    }
}
