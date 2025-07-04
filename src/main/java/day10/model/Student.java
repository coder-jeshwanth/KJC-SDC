package day10.model;

import java.util.List;
import java.util.ArrayList;

public class Student {
    private String id;
    private String email;
    private String password;
    private String name;
    private List<String> registeredCourses;

    public Student(String id, String email, String password, String name) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.registeredCourses = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRegisteredCourses() {
        return registeredCourses;
    }

    public void setRegisteredCourses(List<String> registeredCourses) {
        this.registeredCourses = registeredCourses;
    }
}
