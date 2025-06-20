package day5;

import day5.model.Employee;
import day5.service.EmployeeService;

import java.time.LocalDate;
import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final EmployeeService service = new EmployeeService();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n======= Employee Management Portal =======");
            System.out.println("1. Add Employee");
            System.out.println("2. Update Employee");
            System.out.println("3. Delete Employee");
            System.out.println("4. Search Employees");
            System.out.println("5. List Employees (Paginated)");
            System.out.println("6. View Department Statistics");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": addEmployee(); break;
                case "2": updateEmployee(); break;
                case "3": deleteEmployee(); break;
                case "4": searchEmployees(); break;
                case "5": listPaginated(); break;
                case "6": viewDeptStats(); break;
                case "0": System.exit(0);
                default: System.out.println("Invalid choice!");
            }
        }
    }

    private static void addEmployee() {
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Department: ");
        String department = scanner.nextLine();
        System.out.print("Enter Skills (comma separated): ");
        List<String> skills = Arrays.asList(scanner.nextLine().split(","));
        System.out.print("Enter Joining Date (yyyy-mm-dd): ");
        LocalDate date = LocalDate.parse(scanner.nextLine());



        Employee emp = new Employee(name, email, department, skills, date);
        boolean success = service.addEmployee(emp);
        System.out.println(success ? "Employee added successfully." : "Failed to add employee.");
    }

    private static void updateEmployee() {
        System.out.print("Enter Email of Employee to Update: ");
        String email = scanner.nextLine();
        Map<String, Object> updates = new HashMap<>();

        System.out.print("Update Name? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            System.out.print("Enter new Name: ");
            updates.put("name", scanner.nextLine());
        }

        System.out.print("Update Department? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            System.out.print("Enter new Department: ");
            updates.put("department", scanner.nextLine());
        }

        System.out.print("Update Skills? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            System.out.print("Enter new Skills (comma separated): ");
            updates.put("skills", Arrays.asList(scanner.nextLine().split(",")));
        }

        System.out.print("Update Joining Date? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            System.out.print("Enter new Joining Date (yyyy-mm-dd): ");
            updates.put("joiningDate", LocalDate.parse(scanner.nextLine()));
        }

        boolean updated = service.updateEmployeeFields(email, updates);
        System.out.println(updated ? "Employee updated successfully." : "Update failed.");
    }

    private static void deleteEmployee() {
        System.out.print("Delete by (1) Email or (2) ID? ");
        String type = scanner.nextLine();

        boolean deleted = false;
        if (type.equals("1")) {
            System.out.print("Enter Email: ");
            deleted = service.deleteByEmail(scanner.nextLine());
        } else if (type.equals("2")) {
            System.out.print("Enter Employee ID: ");
            deleted = service.deleteById(scanner.nextLine());
        }

        System.out.println(deleted ? "Employee deleted." : "Employee not found.");
    }

    private static void searchEmployees() {
        System.out.println("Search by: ");
        System.out.println("1. Name (partial match)");
        System.out.println("2. Department");
        System.out.println("3. Skill");
        System.out.println("4. Joining Date Range");
        System.out.print("Choose: ");
        String opt = scanner.nextLine();

        List<Employee> results = new ArrayList<>();

        switch (opt) {
            case "1":
                System.out.print("Enter name keyword: ");
                results = service.searchByName(scanner.nextLine());
                break;
            case "2":
                System.out.print("Enter department: ");
                results = service.searchByDepartment(scanner.nextLine());
                break;
            case "3":
                System.out.print("Enter skill: ");
                results = service.searchBySkill(scanner.nextLine());
                break;
            case "4":
                System.out.print("Enter start date (yyyy-mm-dd): ");
                LocalDate start = LocalDate.parse(scanner.nextLine());
                System.out.print("Enter end date (yyyy-mm-dd): ");
                LocalDate end = LocalDate.parse(scanner.nextLine());
                results = service.searchByJoiningDateRange(start, end);
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }

        if (results.isEmpty()) {
            System.out.println("No results found.");
        } else {
            results.forEach(System.out::println);
        }
    }

    private static void listPaginated() {
        System.out.print("Page number: ");
        int page = Integer.parseInt(scanner.nextLine());
        System.out.print("Page size (e.g., 5): ");
        int size = Integer.parseInt(scanner.nextLine());
        System.out.print("Sort by (name/joiningDate): ");
        String sortBy = scanner.nextLine();
        System.out.print("Sort order (asc/desc): ");
        boolean asc = scanner.nextLine().equalsIgnoreCase("asc");

        List<Employee> list = service.getPaginatedEmployees(page, size, sortBy, asc);
        if (list.isEmpty()) {
            System.out.println("No employees found for this page.");
        } else {
            list.forEach(System.out::println);
        }
    }

    private static void viewDeptStats() {
        Map<String, Long> stats = service.getDepartmentStats();
        System.out.println("\nDepartment Statistics:");
        stats.forEach((dept, count) ->
                System.out.println("Department: " + dept + ", Count: " + count));
    }
}
