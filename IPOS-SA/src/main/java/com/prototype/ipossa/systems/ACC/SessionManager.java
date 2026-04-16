package com.prototype.ipossa.systems.ACC;


//class tracks who is currently logged into the application
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
    public void loginStaff(UserAccount user) {
        this.currentUser     = user;
        this.currentMerchant = null;
    }

    //Records a successful merchant login
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

    //Returns the role of the currently logged-in user
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

    //checks permission of currently login user
    public boolean hasPermission(PermissionCheck check) {
        Role role = getCurrentRole();
        return check.test(role);
    }

    //Throws an exception if the current user doesn't have required permission
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


    public static class AccessDeniedException extends Exception {
        public AccessDeniedException(String message) { super(message); }
    }
}