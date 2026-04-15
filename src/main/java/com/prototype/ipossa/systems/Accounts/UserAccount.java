package com.prototype.ipossa.systems.Accounts;

/**
 * Represents a staff login account — stored in the logins table.
 * Extends Account for shared credentials.
 * Role is stored as a plain String matching the DB value so that
 * new roles can be assigned without any code changes.
 */
public class UserAccount extends Account {

    private String username;
    private String role;

    public UserAccount(String username, String password, String role) {
        super(username, password);
        this.username = username;
        this.role     = role;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void   setUsername(String username) {
        this.username = username;
        setLogin(username);
    }

    public String getRole() { return role; }
    public void   setRole(String role) { this.role = role; }

    // Role checks — plain string comparisons against DB values.
    // No enum needed: changing a user's role is just an UPDATE in the DB.

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