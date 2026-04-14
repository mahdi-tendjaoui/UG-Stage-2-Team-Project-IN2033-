package com.prototype.ipossa.systems.Accounts;

// Represents a staff login account from the logins table
public class UserAccount {

    private String username;
    private String password;
    private String role;

    public UserAccount(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Simple role checks — used in JavaFX to show/hide buttons
    public boolean isAdmin() {
        return role.equals("Administrator");
    }

    public boolean isManager() {
        return role.equals("Director of Operations") || role.equals("Administrator");
    }

    public boolean isAccountant() {
        return role.equals("Senior accountant") || role.equals("Accountant") || role.equals("Administrator");
    }

    public boolean isWarehouse() {
        return role.equals("Warehouse employee") || role.equals("Administrator");
    }

    public boolean isDelivery() {
        return role.equals("Delivery department employee") || role.equals("Administrator");
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}