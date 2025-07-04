package day10.model;

public class Course {
    private String id;
    private String name;
    private String description;
    private int totalSeats;
    private int availableSeats;

    public Course(String id, String name, String description, int totalSeats) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats; // Initially all seats are available
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
}
