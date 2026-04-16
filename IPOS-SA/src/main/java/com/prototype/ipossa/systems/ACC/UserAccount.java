package com.prototype.ipossa.systems.ACC;



public class UserAccount {

    private String username;
    private String password;
    private Role role;

    public UserAccount(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role     = role;
    }
    //Convenience constructord , converts db roles to the Role enum automatically
    public UserAccount(String username, String password, String roleStr) {
        this(username, password, Role.fromDbValue(roleStr));
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    //Returns role as a raw string stored in the database
    public String getRoleDbValue() { return role.getDbValue(); }

    public boolean canManageUserAccounts()      { return role.canManageUserAccounts(); }
    public boolean canManageMerchantAccounts()  { return role.canManageMerchantAccounts(); }
    public boolean canSetCreditLimit()          { return role.canSetCreditLimit(); }
    public boolean canManageDiscountPlans()     { return role.canManageDiscountPlans(); }
    public boolean canReactivateDefaultAccount(){ return role.canReactivateDefaultAccount(); }
    public boolean canManageCatalogue()         { return role.canManageCatalogue(); }
    public boolean canManageOrders()            { return role.canManageOrders(); }
    public boolean canRecordPayments()          { return role.canRecordPayments(); }
    public boolean canGenerateReports()         { return role.canGenerateReports(); }

    @Override
    public String toString() {
        return "UserAccount{username='" + username + "', role=" + role + "}";
    }
}