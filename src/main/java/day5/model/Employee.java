package day5.model;

import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.util.List;

public class Employee {
    private ObjectId id;
    private String name;
    private String email;
    private String department;
    private List<String> skills;
    private LocalDate joiningDate;

    // Constructors
    public Employee() {}

    public Employee(String name, String email, String department, List<String> skills, LocalDate joiningDate) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.skills = skills;
        this.joiningDate = joiningDate;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    @Override
    public String toString() {
        return "\nEmployee {" +
                "\n  ID = " + id +
                ",\n  Name = '" + name + '\'' +
                ",\n  Email = '" + email + '\'' +
                ",\n  Department = '" + department + '\'' +
                ",\n  Skills = " + skills +
                ",\n  Joining Date = " + joiningDate +
                "\n}";
    }
}
