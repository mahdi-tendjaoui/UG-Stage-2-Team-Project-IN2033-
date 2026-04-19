package com.prototype.ipossa.systems.ACC;

/**
 * The class User account.
 */
public class UserAccount {

    private String username;
    private String password;
    private Role role;

    /**
     * Instantiates a new User account.
     *
     * @param username the username
     * @param password the password
     * @param role     the role
     */
    public UserAccount(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role     = role;
    }

    /**
     * Instantiates a new User account.
     *
     * @param username the username
     * @param password the password
     * @param roleStr  the role str
     */
    public UserAccount(String username, String password, String roleStr) {
        this(username, password, Role.fromDbValue(roleStr));
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() { return username; }

    /**
     * Sets username.
     *
     * @param username the username
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Gets password.
     *
     * @return the password
     */
    public String getPassword() { return password; }

    /**
     * Sets password.
     *
     * @param password the password
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Gets role.
     *
     * @return the role
     */
    public Role getRole() { return role; }

    /**
     * Sets role.
     *
     * @param role the role
     */
    public void setRole(Role role) { this.role = role; }

    /**
     * Gets role db value.
     *
     * @return the role db value
     */
    public String getRoleDbValue() { return role.getDbValue(); }

    /**
     * Can manage user accounts boolean.
     *
     * @return the boolean
     */
    public boolean canManageUserAccounts()      { return role.canManageUserAccounts(); }

    /**
     * Can manage merchant accounts boolean.
     *
     * @return the boolean
     */
    public boolean canManageMerchantAccounts()  { return role.canManageMerchantAccounts(); }

    /**
     * Can set credit limit boolean.
     *
     * @return the boolean
     */
    public boolean canSetCreditLimit()          { return role.canSetCreditLimit(); }

    /**
     * Can manage discount plans boolean.
     *
     * @return the boolean
     */
    public boolean canManageDiscountPlans()     { return role.canManageDiscountPlans(); }

    /**
     * Can reactivate default account boolean.
     *
     * @return the boolean
     */
    public boolean canReactivateDefaultAccount(){ return role.canReactivateDefaultAccount(); }

    /**
     * Can manage catalogue boolean.
     *
     * @return the boolean
     */
    public boolean canManageCatalogue()         { return role.canManageCatalogue(); }

    /**
     * Can manage orders boolean.
     *
     * @return the boolean
     */
    public boolean canManageOrders()            { return role.canManageOrders(); }

    /**
     * Can generate invoice boolean.
     *
     * @return the boolean
     */
    public boolean canGenerateInvoice()         { return role.canGenerateInvoice(); }

    /**
     * Can record payments boolean.
     *
     * @return the boolean
     */
    public boolean canRecordPayments()          { return role.canRecordPayments(); }

    /**
     * Can generate reports boolean.
     *
     * @return the boolean
     */
    public boolean canGenerateReports()         { return role.canGenerateReports(); }

    @Override
    public String toString() {
        return "UserAccount{username='" + username + "', role=" + role + "}";
    }
}
