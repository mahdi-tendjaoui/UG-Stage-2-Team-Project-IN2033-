package com.prototype.ipossa.systems.ACC;

/**
 * The class Session manager.
 */
public class SessionManager {

    private static SessionManager instance;

    private SessionManager() {}

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    private UserAccount     currentUser;
    private MerchantAccount currentMerchant;

    /**
     * Login staff.
     *
     * @param user the user
     */
    public void loginStaff(UserAccount user) {
        this.currentUser     = user;
        this.currentMerchant = null;
    }

    /**
     * Login merchant.
     *
     * @param merchant the merchant
     */
    public void loginMerchant(MerchantAccount merchant) {
        this.currentMerchant = merchant;
        this.currentUser     = null;
    }

    /**
     * Logout.
     */
    public void logout() {
        currentUser     = null;
        currentMerchant = null;
    }

    /**
     * Is logged in
     *
     * @return the boolean
     */
    public boolean isLoggedIn() { return currentUser != null || currentMerchant != null; }

    /**
     * Is staff logged in
     *
     * @return the boolean
     */
    public boolean isStaffLoggedIn() { return currentUser != null; }

    /**
     * Is merchant logged in.
     *
     * @return the boolean
     */
    public boolean isMerchantLoggedIn() { return currentMerchant != null; }

    /**
     * Gets current user.
     *
     * @return the current user
     */
    public UserAccount getCurrentUser() { return currentUser; }

    /**
     * Gets current merchant.
     *
     * @return the current merchant
     */
    public MerchantAccount getCurrentMerchant() { return currentMerchant; }

    /**
     * Gets current role.
     *
     * @return the current role
     */
    public Role getCurrentRole() {
        if (currentUser != null)     return currentUser.getRole();
        if (currentMerchant != null) return Role.MERCHANT;
        return Role.UNKNOWN;
    }

    /**
     * The interface Permission check.
     */
    @FunctionalInterface
    public interface PermissionCheck {
        /**
         * Test boolean.
         *
         * @param role the role
         * @return the boolean
         */
        boolean test(Role role);
    }

    /**
     * Has permission boolean.
     *
     * @param check the check
     * @return the boolean
     */
    public boolean hasPermission(PermissionCheck check) {
        Role role = getCurrentRole();
        return check.test(role);
    }

    /**
     * Require permission.
     *
     * @param check             the check
     * @param actionDescription the action description
     * @throws AccessDeniedException the access denied exception
     */
    public void requirePermission(PermissionCheck check, String actionDescription)
            throws AccessDeniedException {
        if (!hasPermission(check)) {
            throw new AccessDeniedException(
                    "'" + getCurrentRole() + "' does not have permission to: " + actionDescription);
        }
    }

    /**
     * Require can manage user accounts.
     *
     * @throws AccessDeniedException the access denied exception
     */
    public void requireCanManageUserAccounts() throws AccessDeniedException {
        requirePermission(Role::canManageUserAccounts, "manage user accounts");
    }

    /**
     * Require can manage merchant accounts.
     *
     * @throws AccessDeniedException the access denied exception
     */
    public void requireCanManageMerchantAccounts() throws AccessDeniedException {
        requirePermission(Role::canManageMerchantAccounts, "manage merchant accounts");
    }

    /**
     * Require can set credit limit.
     *
     * @throws AccessDeniedException the access denied exception
     */
    public void requireCanSetCreditLimit() throws AccessDeniedException {
        requirePermission(Role::canSetCreditLimit, "set credit limits");
    }

    /**
     * Require can manage discount plans.
     *
     * @throws AccessDeniedException the access denied exception
     */
    public void requireCanManageDiscountPlans() throws AccessDeniedException {
        requirePermission(Role::canManageDiscountPlans, "manage discount plans");
    }

    /**
     * Require can reactivate default account.
     *
     * @throws AccessDeniedException the access denied exception
     */
    public void requireCanReactivateDefaultAccount() throws AccessDeniedException {
        requirePermission(Role::canReactivateDefaultAccount,
                "reactivate an 'in default' merchant account");
    }

    /**
     * Require can manage catalogue.
     *
     * @throws AccessDeniedException the access denied exception
     */
    public void requireCanManageCatalogue() throws AccessDeniedException {
        requirePermission(Role::canManageCatalogue, "manage catalogue");
    }

    /**
     * Require can manage orders.
     *
     * @throws AccessDeniedException the access denied exception
     */
    public void requireCanManageOrders() throws AccessDeniedException {
        requirePermission(Role::canManageOrders, "manage orders");
    }

    /**
     * Require can record payments.
     *
     * @throws AccessDeniedException the access denied exception
     */
    public void requireCanRecordPayments() throws AccessDeniedException {
        requirePermission(Role::canRecordPayments, "record payments");
    }

    /**
     * Require can generate reports.
     *
     * @throws AccessDeniedException the access denied exception
     */
    public void requireCanGenerateReports() throws AccessDeniedException {
        requirePermission(Role::canGenerateReports, "generate reports");
    }

    /**
     * The type Access denied exception.
     */
    public static class AccessDeniedException extends Exception {
        /**
         * Instantiates a new Access denied exception.
         *
         * @param message the message
         */
        public AccessDeniedException(String message) { super(message); }
    }
}
