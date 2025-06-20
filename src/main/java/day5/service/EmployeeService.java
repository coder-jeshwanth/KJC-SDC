package day5.service;

import com.mongodb.client.model.Filters;
import day5.dao.EmployeeDAO;
import day5.model.Employee;
import org.bson.Document;

import java.time.LocalDate;
import java.util.*;

public class EmployeeService {
    private final EmployeeDAO dao;

    public EmployeeService() {
        dao = new EmployeeDAO();
    }

    // 1. Add employee after checking email uniqueness
    public boolean addEmployee(Employee employee) {
        List<Employee> existing = dao.searchBySkill(""); // force fetch to see if email exists
        boolean emailExists = existing.stream().anyMatch(e -> e.getEmail().equalsIgnoreCase(employee.getEmail()));
        if (emailExists) {
            System.out.println("Error: Email already exists.");
            return false;
        }
        return dao.insertEmployee(employee);
    }

    // 2. Update fields by email
    public boolean updateEmployeeFields(String email, Map<String, Object> updates) {
        return dao.updateEmployee(email, updates);
    }

    // 3. Delete employee
    public boolean deleteByEmail(String email) {
        return dao.deleteEmployeeByEmail(email);
    }

    public boolean deleteById(String id) {
        return dao.deleteEmployeeById(id);
    }

    // 4. Search
    public List<Employee> searchByName(String name) {
        return dao.searchByName(name);
    }

    public List<Employee> searchByDepartment(String dept) {
        return dao.searchByDepartment(dept);
    }

    public List<Employee> searchBySkill(String skill) {
        return dao.searchBySkill(skill);
    }

    public List<Employee> searchByJoiningDateRange(LocalDate from, LocalDate to) {
        return dao.searchByJoiningDateRange(from, to);
    }

    // 5. Pagination
    public List<Employee> getPaginatedEmployees(int page, int size, String sortBy, boolean asc) {
        return dao.getPaginatedEmployees(page, size, sortBy, asc);
    }

    // 6. Aggregation
    public Map<String, Long> getDepartmentStats() {
        return dao.getDepartmentStatistics();
    }
    public boolean isEmailExists(String email) {
        return dao.isEmailExists(email);
    }


}
