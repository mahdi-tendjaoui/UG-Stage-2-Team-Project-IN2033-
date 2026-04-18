package com.prototype.ipossa.systems.ACC;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class AccountService {

    public UserAccount loginStaff(Connection conn, String username, String password)
            throws SQLException {
        boolean valid = AccountSQL.validateUser(conn, username, password);
        if (!valid) return null;

        String roleStr = AccountSQL.getUserRole(conn, username);
        UserAccount user = new UserAccount(username, password, roleStr);
        SessionManager.getInstance().loginStaff(user);
        return user;
    }

    public MerchantAccount loginMerchant(Connection conn, String login, String password)
            throws SQLException {
        boolean valid = AccountSQL.authenticateMerchant(conn, login, password);
        if (!valid) return null;

        int merchantID = AccountSQL.getMerchantID(conn, login);
        MerchantAccount merchant = getMerchant(conn, merchantID);
        if (merchant == null) return null;

        SessionManager.getInstance().loginMerchant(merchant);
        return merchant;
    }

    public void createUserAccount(Connection conn, String username, String password, String role)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageUserAccounts();
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be blank.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password cannot be blank.");
        AccountSQL.createUserAccount(conn, username, password, role);
    }

    public void deleteUserAccount(Connection conn, String username)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageUserAccounts();

        UserAccount current = SessionManager.getInstance().getCurrentUser();
        if (current != null && current.getUsername().equalsIgnoreCase(username))
            throw new IllegalStateException("You cannot delete your own account.");

        AccountSQL.deleteUserAccount(conn, username);
    }

    public void changeUserRole(Connection conn, String username, String newRole)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageUserAccounts();
        if (Role.fromDbValue(newRole) == Role.UNKNOWN)
            throw new IllegalArgumentException("Unknown role: " + newRole);
        AccountSQL.changeUserRole(conn, username, newRole);
    }

    public List<UserAccount> getAllStaffAccounts(Connection conn)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageUserAccounts();
        List<UserAccount> users = new ArrayList<>();
        ResultSet rs = AccountSQL.getAllUsers(conn);
        while (rs.next()) {
            users.add(new UserAccount(
                    rs.getString("username"),
                    "",
                    rs.getString("role")));
        }
        return users;
    }

    public MerchantAccount getMerchant(Connection conn, int merchantID) throws SQLException {
        ResultSet rs = AccountSQL.getAllMerchants(conn);
        while (rs.next()) {
            if (rs.getInt("merchant_ID") == merchantID) {
                return mapRowToMerchant(rs);
            }
        }
        return null;
    }

    public List<MerchantAccount> getAllMerchants(Connection conn)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageMerchantAccounts();
        List<MerchantAccount> merchants = new ArrayList<>();
        ResultSet rs = AccountSQL.getAllMerchants(conn);
        while (rs.next()) {
            merchants.add(mapRowToMerchant(rs));
        }
        return merchants;
    }

    public void createMerchantAccount(Connection conn,
                                      String accountHolderName, String accountNumber,
                                      String contactName, String address,
                                      String phoneNumber, double creditLimit,
                                      String agreedDiscount, String login, String password)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageMerchantAccounts();
        validateMerchantDetails(accountHolderName, contactName, address,
                phoneNumber, creditLimit, login, password);
        AccountSQL.createMerchantAccount(conn, accountHolderName, accountNumber,
                contactName, address, phoneNumber, creditLimit,
                agreedDiscount, login, password);
    }

    public void updateMerchantDetails(Connection conn, int merchantID,
                                      String contactName, String address, String phoneNumber)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageMerchantAccounts();
        AccountSQL.updateMerchantAccount(conn, merchantID, contactName, address, phoneNumber);
    }

    public void deleteMerchantAccount(Connection conn, int merchantID)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageUserAccounts();
        AccountSQL.deleteMerchantAccount(conn, merchantID);
    }

    public void setCreditLimit(Connection conn, int merchantID, double creditLimit)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanSetCreditLimit();
        if (creditLimit < 0)
            throw new IllegalArgumentException("Credit limit cannot be negative.");
        AccountSQL.setCreditLimit(conn, merchantID, creditLimit);
    }

    public void setDiscountPlan(Connection conn, int merchantID, List<DiscountTier> tiers)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageDiscountPlans();
        if (tiers == null || tiers.isEmpty())
            throw new IllegalArgumentException("At least one discount tier is required.");
        AccountSQL.deleteDiscountTiers(conn, merchantID);
        for (DiscountTier tier : tiers) {
            AccountSQL.addDiscountTier(conn, merchantID,
                    tier.getMinOrderValue(), tier.getMaxOrderValue(), tier.getDiscountRate());
        }
    }

    public void deleteDiscountPlan(Connection conn, int merchantID)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageDiscountPlans();
        AccountSQL.deleteDiscountTiers(conn, merchantID);
    }

    public MerchantAccount.AccountState updateAccountStateForPayment(
            Connection conn, int merchantID, LocalDate paymentDueDate)
            throws SQLException {

        long daysLate = ChronoUnit.DAYS.between(paymentDueDate, LocalDate.now());

        MerchantAccount.AccountState newState;
        if (daysLate <= 0) {
            newState = MerchantAccount.AccountState.NORMAL;
        } else if (daysLate <= 15) {
            newState = MerchantAccount.AccountState.NORMAL;
        } else if (daysLate <= 30) {
            newState = MerchantAccount.AccountState.SUSPENDED;
        } else {
            newState = MerchantAccount.AccountState.IN_DEFAULT;
        }

        String current = AccountSQL.getMerchantStatus(conn, merchantID);
        if (!newState.getDbValue().equals(current)) {
            AccountSQL.updateMerchantStatus(conn, merchantID, newState.getDbValue());
        }
        return newState;
    }

    public boolean shouldShowPaymentReminder(LocalDate paymentDueDate) {
        long daysLate = ChronoUnit.DAYS.between(paymentDueDate, LocalDate.now());
        return daysLate > 0 && daysLate <= 15;
    }

    public void reactivateDefaultAccount(Connection conn, int merchantID)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanReactivateDefaultAccount();

        String currentState = AccountSQL.getMerchantStatus(conn, merchantID);
        if (!MerchantAccount.AccountState.IN_DEFAULT.getDbValue().equals(currentState)) {
            throw new IllegalStateException(
                    "Account is not 'in default' - no reactivation needed.");
        }
        AccountSQL.updateMerchantStatus(conn,
                merchantID, MerchantAccount.AccountState.NORMAL.getDbValue());
    }

    public void handlePaymentReceived(Connection conn, int merchantID, boolean balanceCleared)
            throws SQLException {
        if (!balanceCleared) return;
        String currentState = AccountSQL.getMerchantStatus(conn, merchantID);
        if (MerchantAccount.AccountState.SUSPENDED.getDbValue().equals(currentState)) {
            AccountSQL.updateMerchantStatus(conn,
                    merchantID, MerchantAccount.AccountState.NORMAL.getDbValue());
        }

    }

    private MerchantAccount mapRowToMerchant(ResultSet rs) throws SQLException {
        return new MerchantAccount(
                rs.getInt("merchant_ID"),
                rs.getString("account_holder_name"),
                rs.getString("account_number"),
                rs.getString("contact_name"),
                rs.getString("address"),
                rs.getString("phone_number"),
                rs.getDouble("credit_limit"),
                rs.getString("agreed_discount"),
                rs.getString("login"),
                rs.getString("password"),
                rs.getString("account_state"));
    }

    private void validateMerchantDetails(String accountHolderName, String contactName,
                                         String address, String phoneNumber,
                                         double creditLimit, String login, String password) {
        if (accountHolderName == null || accountHolderName.isBlank())
            throw new IllegalArgumentException("Account holder name is required.");
        if (contactName == null || contactName.isBlank())
            throw new IllegalArgumentException("Contact name is required.");
        if (address == null || address.isBlank())
            throw new IllegalArgumentException("Address is required.");
        if (phoneNumber == null || phoneNumber.isBlank())
            throw new IllegalArgumentException("Phone number is required.");
        if (creditLimit < 0)
            throw new IllegalArgumentException("Credit limit must be non-negative.");
        if (login == null || login.isBlank())
            throw new IllegalArgumentException("Login is required.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password is required.");
    }
}
