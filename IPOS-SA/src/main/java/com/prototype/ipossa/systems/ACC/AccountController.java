package com.prototype.ipossa.systems.ACC;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * The class Account controller.
 */
public class AccountController {

    private final AccountService service = new AccountService();

    /**
     * Staff login user account.
     *
     * @param username the username
     * @param password the password
     * @return the user account
     */
    public UserAccount staffLogin(String username, String password) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            return service.loginStaff(conn, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Merchant login merchant account.
     *
     * @param login    the login
     * @param password the password
     * @return the merchant account
     */
    public MerchantAccount merchantLogin(String login, String password) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            return service.loginMerchant(conn, login, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Logout.
     */
    public void logout() {
        SessionManager.getInstance().logout();
    }

    /**
     * Create staff account boolean.
     *
     * @param username the username
     * @param password the password
     * @param role     the role
     * @return the boolean
     */
    public boolean createStaffAccount(String username, String password, String role) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            service.createUserAccount(conn, username, password, role);
            return true;
        } catch (SessionManager.AccessDeniedException e) {
            System.err.println("Access denied: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete staff account boolean.
     *
     * @param username the username
     * @return the boolean
     */
    public boolean deleteStaffAccount(String username) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            service.deleteUserAccount(conn, username);
            return true;
        } catch (SessionManager.AccessDeniedException e) {
            System.err.println("Access denied: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Change staff role boolean.
     * @param username the username
     *
     * @param newRole  the new role
     * @return the boolean
     */
    public boolean changeStaffRole(String username, String newRole) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            service.changeUserRole(conn, username, newRole);
            return true;
        } catch (SessionManager.AccessDeniedException e) {
            System.err.println("Access denied: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all staff accounts.
     * @return the all staff accounts
     */
    public List<UserAccount> getAllStaffAccounts() {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            return service.getAllStaffAccounts(conn);
        } catch (SessionManager.AccessDeniedException e) {
            System.err.println("Access denied: " + e.getMessage());
            return List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Create merchant account boolean.
     *
     * @param accountHolderName the account holder name
     * @param accountNumber     the account number
     * @param contactName       the contact name
     * @param address           the address
     * @param phoneNumber       the phone number
     * @param creditLimit       the credit limit
     * @param agreedDiscount    the agreed discount
     * @param login             the login
     * @param password          the password
     * @return the boolean
     */
    public boolean createMerchantAccount(String accountHolderName, String accountNumber,
                                         String contactName, String address,
                                         String phoneNumber, double creditLimit,
                                         String agreedDiscount, String login, String password) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            service.createMerchantAccount(conn, accountHolderName, accountNumber,
                    contactName, address, phoneNumber, creditLimit,
                    agreedDiscount, login, password);
            return true;
        } catch (SessionManager.AccessDeniedException e) {
            System.err.println("Access denied: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update merchant details boolean.
     *
     * @param merchantID  the merchant id
     * @param contactName the contact name
     * @param address     the address
     * @param phoneNumber the phone number
     * @return the boolean
     */
    public boolean updateMerchantDetails(int merchantID, String contactName,
                                         String address, String phoneNumber) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            service.updateMerchantDetails(conn, merchantID, contactName, address, phoneNumber);
            return true;
        } catch (SessionManager.AccessDeniedException e) {
            System.err.println("Access denied: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete merchant account boolean.
     *
     * @param merchantID the merchant id
     * @return the boolean
     */
    public boolean deleteMerchantAccount(int merchantID) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            service.deleteMerchantAccount(conn, merchantID);
            return true;
        } catch (SessionManager.AccessDeniedException e) {
            System.err.println("Access denied: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all merchants.
     *
     * @return the all merchants
     */
    public List<MerchantAccount> getAllMerchants() {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            return service.getAllMerchants(conn);
        } catch (SessionManager.AccessDeniedException e) {
            System.err.println("Access denied: " + e.getMessage());
            return List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Sets credit limit.
     *
     * @param merchantID  the merchant id
     * @param creditLimit the credit limit
     * @return the credit limit
     */
    public boolean setCreditLimit(int merchantID, double creditLimit) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            service.setCreditLimit(conn, merchantID, creditLimit);
            return true;
        } catch (SessionManager.AccessDeniedException e) {
            System.err.println("Access denied: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sets discount plan.
     *
     * @param merchantID the merchant id
     * @param tiers      the tiers
     * @return the discount plan
     */
    public boolean setDiscountPlan(int merchantID, List<DiscountTier> tiers) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            service.setDiscountPlan(conn, merchantID, tiers);
            return true;
        } catch (SessionManager.AccessDeniedException e) {
            System.err.println("Access denied: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete discount plan boolean.
     *
     * @param merchantID the merchant id
     * @return the boolean
     */
    public boolean deleteDiscountPlan(int merchantID) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            service.deleteDiscountPlan(conn, merchantID);
            return true;
        } catch (SessionManager.AccessDeniedException e) {
            System.err.println("Access denied: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Refresh account state merchant account . account state.
     *
     * @param merchantID     the merchant id
     * @param paymentDueDate the payment due date
     * @return the merchant account . account state
     */
    public MerchantAccount.AccountState refreshAccountState(int merchantID, LocalDate paymentDueDate) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            return service.updateAccountStateForPayment(conn, merchantID, paymentDueDate);
        } catch (Exception e) {
            e.printStackTrace();
            return MerchantAccount.AccountState.NORMAL;
        }
    }

    /**
     * Should show payment reminder boolean.
     *
     * @param paymentDueDate the payment due date
     * @return the boolean
     */
    public boolean shouldShowPaymentReminder(LocalDate paymentDueDate) {
        return service.shouldShowPaymentReminder(paymentDueDate);
    }

    /**
     * Reactivate default account boolean.
     *
     * @param merchantID the merchant id
     * @return the boolean
     */
    public boolean reactivateDefaultAccount(int merchantID) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            service.reactivateDefaultAccount(conn, merchantID);
            return true;
        } catch (SessionManager.AccessDeniedException e) {
            System.err.println("Access denied: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * On payment received.
     *
     * @param merchantID     the merchant id
     * @param balanceCleared the balance cleared
     */
    public void onPaymentReceived(int merchantID, boolean balanceCleared) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            service.handlePaymentReceived(conn, merchantID, balanceCleared);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Current user can manage staff boolean.
     *
     * @return the boolean
     */
    public boolean currentUserCanManageStaff() {
        return SessionManager.getInstance().hasPermission(Role::canManageUserAccounts);
    }

    /**
     * Current user can manage merchants boolean.
     *
     * @return the boolean
     */
    public boolean currentUserCanManageMerchants() {
        return SessionManager.getInstance().hasPermission(Role::canManageMerchantAccounts);
    }

    /**
     * Current user can reactivate accounts boolean.
     *
     * @return the boolean
     */
    public boolean currentUserCanReactivateAccounts() {
        return SessionManager.getInstance().hasPermission(Role::canReactivateDefaultAccount);
    }
}
