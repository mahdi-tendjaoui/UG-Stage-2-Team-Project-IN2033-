package com.prototype.ipossa.systems.ACC;

/**
 * This file tracks who is currently logged into the application.
 * All parts of the system should reference to the SessionManager:
 *   1. Obtain the current user/merchant
 *   2. Check if they're  allowed to perform an action
 */
public class SessionManager {

    private static SessionManager instance;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    private UserAccount     currentUser;     // set when a staff member logs in
    private MerchantAccount currentMerchant; // set when a merchant logs in

    //Records a successful staff login
    //Clears any existing merchant session
    public void loginStaff(UserAccount user) {
        this.currentUser     = user;
        this.currentMerchant = null;
    }

    //Records a successful merchant login
    //Clears any existing staff session
    public void loginMerchant(MerchantAccount merchant) {
        this.currentMerchant = merchant;
        this.currentUser     = null;
    }

    //Ends the current session
    public void logout() {
        currentUser     = null;
        currentMerchant = null;
    }

    public boolean isLoggedIn() { return currentUser != null || currentMerchant != null; }
    public boolean isStaffLoggedIn() { return currentUser != null; }
    public boolean isMerchantLoggedIn() { return currentMerchant != null; }

    public UserAccount getCurrentUser() { return currentUser; }
    public MerchantAccount getCurrentMerchant() { return currentMerchant; }

    //Returns the role of the currently logged-in staff member
    //Returns merchant if a merchant is logged in or unknown if nobody is logged in.
    public Role getCurrentRole() {
        if (currentUser != null)     return currentUser.getRole();
        if (currentMerchant != null) return Role.MERCHANT;
        return Role.UNKNOWN;
    }

    //Functional interface so callers can pass a method reference
    @FunctionalInterface
    public interface PermissionCheck {
        boolean test(Role role);
    }

    //Returns true if the currently logged-in user's role passes the given permission check.
    public boolean hasPermission(PermissionCheck check) {
        Role role = getCurrentRole();
        return check.test(role);
    }

    //Throws an exception if the current user does NOT have the required permission
    // To be used at the start of sensitive methods to enforce role based access control
    public void requirePermission(PermissionCheck check, String actionDescription)
            throws AccessDeniedException {
        if (!hasPermission(check)) {
            throw new AccessDeniedException(
                    "'" + getCurrentRole() + "' does not have permission to: " + actionDescription);
        }
    }

    public void requireCanManageUserAccounts() throws AccessDeniedException {
        requirePermission(Role::canManageUserAccounts, "manage user accounts");
    }
    public void requireCanManageMerchantAccounts() throws AccessDeniedException {
        requirePermission(Role::canManageMerchantAccounts, "manage merchant accounts");
    }
    public void requireCanSetCreditLimit() throws AccessDeniedException {
        requirePermission(Role::canSetCreditLimit, "set credit limits");
    }
    public void requireCanManageDiscountPlans() throws AccessDeniedException {
        requirePermission(Role::canManageDiscountPlans, "manage discount plans");
    }
    public void requireCanReactivateDefaultAccount() throws AccessDeniedException {
        requirePermission(Role::canReactivateDefaultAccount,
                "reactivate an 'in default' merchant account");
    }
    public void requireCanManageCatalogue() throws AccessDeniedException {
        requirePermission(Role::canManageCatalogue, "manage catalogue");
    }
    public void requireCanManageOrders() throws AccessDeniedException {
        requirePermission(Role::canManageOrders, "manage orders");
    }
    public void requireCanRecordPayments() throws AccessDeniedException {
        requirePermission(Role::canRecordPayments, "record payments");
    }
    public void requireCanGenerateReports() throws AccessDeniedException {
        requirePermission(Role::canGenerateReports, "generate reports");
    }

    //Throws an exception when the current user attempts an action they are not permitted to perform
    public static class AccessDeniedException extends Exception {
        public AccessDeniedException(String message) { super(message); }
    }
}