package com.prototype.ipossa.systems.ACC;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// acts as bridge ui& services


public class AccountController {

    private final AccountService service = new AccountService();

    //Handles a staff login attempt
    public UserAccount staffLogin(String username, String password) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            return service.loginStaff(conn, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Handles the merchant's login attempt

    public MerchantAccount merchantLogin(String login, String password) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            return service.loginMerchant(conn, login, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Logs out
    public void logout() {
        SessionManager.getInstance().logout();
    }

    //Creates a new staff account
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

    //Deletes an existing staff account n returns
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

    //Changes the role of staff account
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

    //Returns all staff accounts for display in the admin UI
    //returns list of UserAccounts
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

    //Creates a new merchant account with all required details
    //returns true on success.
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

    //Updates a merchant's editable contact details
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

    //Deletes a merchant account
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

    //Returns all merchants accounts
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

    //Sets or changes the credit limit for a merchant
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

    //alters discount plan with the supplied tiers
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

    //Removes all discount tiers for a merchant.
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

    //Updates a merchant's account state based on their payment due date
    public MerchantAccount.AccountState refreshAccountState(int merchantID, LocalDate paymentDueDate) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            return service.updateAccountStateForPayment(conn, merchantID, paymentDueDate);
        } catch (Exception e) {
            e.printStackTrace();
            return MerchantAccount.AccountState.NORMAL;
        }
    }

    //payment overdue reminder, if returns true
    public boolean shouldShowPaymentReminder(LocalDate paymentDueDate) {
        return service.shouldShowPaymentReminder(paymentDueDate);
    }

    //Reactivates an in default merchant account to "normal"
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

    //payment recieved method
    public void onPaymentReceived(int merchantID, boolean balanceCleared) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            service.handlePaymentReceived(conn, merchantID, balanceCleared);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Returns true if the current user can manage staff accounts
    public boolean currentUserCanManageStaff() {
        return SessionManager.getInstance().hasPermission(Role::canManageUserAccounts);
    }

    //Returns true if the current user can manage merchant accounts
    public boolean currentUserCanManageMerchants() {
        return SessionManager.getInstance().hasPermission(Role::canManageMerchantAccounts);
    }

    //Returns true if the current user can reactivate in default accounts
    public boolean currentUserCanReactivateAccounts() {
        return SessionManager.getInstance().hasPermission(Role::canReactivateDefaultAccount);
    }
}