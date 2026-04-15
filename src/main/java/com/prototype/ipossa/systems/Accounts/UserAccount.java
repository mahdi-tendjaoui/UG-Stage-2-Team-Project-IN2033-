package com.prototype.ipossa.systems.Accounts;
public class UserAccount {

    private String username;
    private String password;
    private String role;

    public UserAccount(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role     = role;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void   setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void   setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void   setRole(String role) { this.role = role; }

    // Check roles

    // Only Administrator can manage staff accounts
    public boolean isAdmin() {
        return role.equals("Administrator");
    }

    // Administrator and Director of Operations can manage merchant accounts
    public boolean isManager() {
        return role.equals("Director of Operations") || role.equals("Administrator");
    }

    // Senior accountant, Accountant, and Administrator can record payments
    public boolean isAccountant() {
        return role.equals("Senior accountant") || role.equals("Accountant")
                || role.equals("Administrator");
    }

    // Warehouse employee and Administrator can manage catalogue and order status
    public boolean isWarehouse() {
        return role.equals("Warehouse employee") || role.equals("Administrator");
    }

    // Delivery employee and Administrator can mark orders as delivered
    public boolean isDelivery() {
        return role.equals("Delivery department employee") || role.equals("Administrator");
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}