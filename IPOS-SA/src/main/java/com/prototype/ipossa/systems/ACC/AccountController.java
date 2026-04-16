package com.prototype.ipossa.systems.ACC;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AccountController {

    private final AccountService service = new AccountService();

    public UserAccount staffLogin(String username, String password) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            return service.loginStaff(conn, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public MerchantAccount merchantLogin(String login, String password) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            return service.loginMerchant(conn, login, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }

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

    public MerchantAccount.AccountState refreshAccountState(int merchantID, LocalDate paymentDueDate) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            return service.updateAccountStateForPayment(conn, merchantID, paymentDueDate);
        } catch (Exception e) {
            e.printStackTrace();
            return MerchantAccount.AccountState.NORMAL;
        }
    }

    public boolean shouldShowPaymentReminder(LocalDate paymentDueDate) {
        return service.shouldShowPaymentReminder(paymentDueDate);
    }

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

    public void onPaymentReceived(int merchantID, boolean balanceCleared) {
        try (Connection conn = com.prototype.ipossa.MyJDBC.getConnection()) {
            service.handlePaymentReceived(conn, merchantID, balanceCleared);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean currentUserCanManageStaff() {
        return SessionManager.getInstance().hasPermission(Role::canManageUserAccounts);
    }

    public boolean currentUserCanManageMerchants() {
        return SessionManager.getInstance().hasPermission(Role::canManageMerchantAccounts);
    }

    public boolean currentUserCanReactivateAccounts() {
        return SessionManager.getInstance().hasPermission(Role::canReactivateDefaultAccount);
    }
}
