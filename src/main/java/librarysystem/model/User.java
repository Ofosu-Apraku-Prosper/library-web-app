package librarysystem.model;

public class User {
    private int id;
    private String fullName;
    private String username;
    private String email;
    private String role;

    public User(int id, String fullName, String username, String email, String role) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
}
