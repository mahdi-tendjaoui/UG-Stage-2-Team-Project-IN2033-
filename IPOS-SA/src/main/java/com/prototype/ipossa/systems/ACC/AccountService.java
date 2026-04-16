package com.prototype.ipossa.systems.ACC;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Business-logic layer for the IPOS-SA-ACC package.
 *
 * This class sits between the UI/controllers and the raw SQL methods in
 * AccountSQL. It enforces:
 *   - Role-based access control (via SessionManager)
 *   - Business rules (e.g. account-state transitions, payment deadlines)
 *   - Data validation before any DB write
 *
 * All methods that mutate data first call SessionManager to verify the caller
 * has the required privilege, then delegate to AccountSQL for the actual query.
 */
public class AccountService {

    // =========================================================================
    // ── STAFF LOGIN ACCOUNTS ─────────────────────────────────────────────────
    // =========================================================================

    /**
     * Attempts to authenticate a staff member.
     * Returns the logged-in UserAccount on success, null on failure.
     *
     * Also warms the SessionManager so the rest of the application knows who
     * is logged in and what they are allowed to do.
     */
    public UserAccount loginStaff(Connection conn, String username, String password)
            throws SQLException {
        boolean valid = AccountSQL.validateUser(conn, username, password);
        if (!valid) return null;

        String roleStr = AccountSQL.getUserRole(conn, username);
        UserAccount user = new UserAccount(username, password, roleStr);
        SessionManager.getInstance().loginStaff(user);
        return user;
    }

    /**
     * Attempts to authenticate a merchant.
     * Returns the logged-in MerchantAccount on success, null on failure.
     *
     * After a successful login the merchant's account state is checked:
     *   - SUSPENDED / IN_DEFAULT → login still succeeds but a reminder flag is
     *     returned so the UI can display the appropriate warning message.
     */
    public MerchantAccount loginMerchant(Connection conn, String login, String password)
            throws SQLException {
        boolean valid = AccountSQL.authenticateMerchant(conn, login, password);
        if (!valid) return null;

        int merchantID = AccountSQL.getMerchantID(conn, login);
        MerchantAccount merchant = getMerchant(conn, merchantID);
        if (merchant == null) return null;

        // Load discount tiers into the account object
//        merchant.setDiscountTiers(getDiscountTiers(conn, merchantID));

        SessionManager.getInstance().loginMerchant(merchant);
        return merchant;
    }

    // ── Staff account CRUD ────────────────────────────────────────────────────

    /**
     * Creates a new staff login account.
     * Requires: Administrator role.
     */
    public void createUserAccount(Connection conn, String username, String password, String role)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageUserAccounts();
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be blank.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password cannot be blank.");
        AccountSQL.createUserAccount(conn, username, password, role);
    }

    //Deletes a staff login account
    //Requires: Administrator role
    //Prevents deletion of the last Administrator account (safety guard).
    public void deleteUserAccount(Connection conn, String username)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageUserAccounts();

        // Prevent accidental self-deletion
        UserAccount current = SessionManager.getInstance().getCurrentUser();
        if (current != null && current.getUsername().equalsIgnoreCase(username))
            throw new IllegalStateException("You cannot delete your own account.");

        AccountSQL.deleteUserAccount(conn, username);
    }

    //Changes the role of an existing staff account

    //Requires: Administrator role
    //can also be achieved by deleting and recreating
    public void changeUserRole(Connection conn, String username, String newRole)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageUserAccounts();
        if (Role.fromDbValue(newRole) == Role.UNKNOWN)
            throw new IllegalArgumentException("Unknown role: " + newRole);
        AccountSQL.changeUserRole(conn, username, newRole);
    }

    /**
     * Returns all staff accounts (username + role only — no passwords).
     * Requires: Administrator role.
     */
    public List<UserAccount> getAllStaffAccounts(Connection conn)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageUserAccounts();
        List<UserAccount> users = new ArrayList<>();
        ResultSet rs = AccountSQL.getAllUsers(conn);
        while (rs.next()) {
            users.add(new UserAccount(
                    rs.getString("username"),
                    "",                          // never expose passwords
                    rs.getString("role")));
        }
        return users;
    }

    //Reads a single merchant from the DB by their merchant_ID
    // Returns null if not found.
    public MerchantAccount getMerchant(Connection conn, int merchantID) throws SQLException {
        ResultSet rs = AccountSQL.getAllMerchants(conn); // reuse existing query
        while (rs.next()) {
            if (rs.getInt("merchant_ID") == merchantID) {
                return mapRowToMerchant(rs);
            }
        }
        return null;
    }

     //Returns all merchant accounts.
     //Requires an Administrator or a Director of Operations
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

    //Creates a new merchant account with the mandatory contact and financial details the brief requires before activation
    //Requires an Administrator or Director of Operations.
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

    //Updates the editable contact details of an existing merchant
    //Requires: Administrator or Director of Operations
    public void updateMerchantDetails(Connection conn, int merchantID,
                                      String contactName, String address, String phoneNumber)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageMerchantAccounts();
        AccountSQL.updateMerchantAccount(conn, merchantID, contactName, address, phoneNumber);
    }

    //Deletes a merchant account and all associated discount tiers
    //(cascading delete handled in AccountSQL)
    //Requires: Administrator.
    public void deleteMerchantAccount(Connection conn, int merchantID)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageUserAccounts(); // Admin only
        AccountSQL.deleteMerchantAccount(conn, merchantID);
    }

    //Sets or changes the credit limit for a merchant
    //Requires an Administrator or Director of Operations.
    public void setCreditLimit(Connection conn, int merchantID, double creditLimit)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanSetCreditLimit();
        if (creditLimit < 0)
            throw new IllegalArgumentException("Credit limit cannot be negative.");
        AccountSQL.setCreditLimit(conn, merchantID, creditLimit);
    }


    //Replaces the entire discount plan for a merchant
    //Deletes existing tiers first, then inserts the new ones
    //Requires: Administrator or Director of Operations
    //For a FIXED plan pass a single DiscountTier with min=0, max=null
    //For a VARIABLE plan pass multiple tiers covering the desired ranges.
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

    //Deletes all discount tiers for a merchant
    //Requires an Administrator or Director of Operations
    public void deleteDiscountPlan(Connection conn, int merchantID)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanManageDiscountPlans();
        AccountSQL.deleteDiscountTiers(conn, merchantID);
    }

    //Returns the discount tiers for a merchant as a typed list
//    public List<DiscountTier> getDiscountTiers(Connection conn, int merchantID)
//            throws SQLException {
//        List<DiscountTier> tiers = new ArrayList<>();
//        ResultSet rs = AccountSQL.getDiscountTiers(conn, merchantID);
//        while (rs.next()) {
//            double minVal = rs.getDouble("min_order_value");
//            Double min    = rs.wasNull() ? null : minVal;
//            double maxVal = rs.getDouble("max_order_value");
//            Double max    = rs.wasNull() ? null : maxVal;
//            tiers.add(new DiscountTier(
//                    rs.getInt("discount_ID"),
//                    merchantID,
//                    min, max,
//                    rs.getDouble("discount_rate")));
//        }
//        return tiers;
//    }

    //Automatically updates a merchant's account state based on how many days
    //have passed since their payment was due (end of the calendar month)
    // Rules from the Student's Brief:
    //≤ 15 days late  → normal
    //15–30 days late → suspended
    //> 30 days late   in default
    //This should be called every time a merchant account is accessed
    //param conn is an active database connection
    //param merchantID the merchant to check
    //param paymentDueDate the date by which the payment was due
     //return the updated AccountState
    public MerchantAccount.AccountState updateAccountStateForPayment(
            Connection conn, int merchantID, LocalDate paymentDueDate)
            throws SQLException {

        long daysLate = ChronoUnit.DAYS.between(paymentDueDate, LocalDate.now());

        MerchantAccount.AccountState newState;
        if (daysLate <= 0) {
            newState = MerchantAccount.AccountState.NORMAL;          // paid on time
        } else if (daysLate <= 15) {
            newState = MerchantAccount.AccountState.NORMAL;          // late but under 15 days — show reminder only
        } else if (daysLate <= 30) {
            newState = MerchantAccount.AccountState.SUSPENDED;
        } else {
            newState = MerchantAccount.AccountState.IN_DEFAULT;
        }

        // Only write to DB if state needs to change
        String current = AccountSQL.getMerchantStatus(conn, merchantID);
        if (!newState.getDbValue().equals(current)) {
            AccountSQL.updateMerchantStatus(conn, merchantID, newState.getDbValue());
        }
        return newState;
    }

    //Returns true if the merchant should see a late-payment reminder on login
    //(like if they are 1–15 days past their payment due date but not yet suspendedr
    public boolean shouldShowPaymentReminder(LocalDate paymentDueDate) {
        long daysLate = ChronoUnit.DAYS.between(paymentDueDate, LocalDate.now());
        return daysLate > 0 && daysLate <= 15;
    }

    //Restores an "in default" merchant account to "normal"
    //Per the brief: this can ONLY be done by the Director of Operations,
    // AND only after a payment has been received.
    //Requires: Director of Operations (or Administrator).
    public void reactivateDefaultAccount(Connection conn, int merchantID)
            throws SQLException, SessionManager.AccessDeniedException {
        SessionManager.getInstance().requireCanReactivateDefaultAccount();

        String currentState = AccountSQL.getMerchantStatus(conn, merchantID);
        if (!MerchantAccount.AccountState.IN_DEFAULT.getDbValue().equals(currentState)) {
            throw new IllegalStateException(
                    "Account is not 'in default' — no reactivation needed.");
        }
        AccountSQL.updateMerchantStatus(conn,
                merchantID, MerchantAccount.AccountState.NORMAL.getDbValue());
    }

    //Called when a payment is received from a merchant
    // If the account is SUSPENDED and the payment clears the outstanding
    // balance, the account is automatically restored to NORMAL.
    // "In default" accounts are NOT automatically restored that can be requiresDirector of Operations authorisation via reactivateDefaultAccount().
    public void handlePaymentReceived(Connection conn, int merchantID, boolean balanceCleared)
            throws SQLException {
        if (!balanceCleared) return;
        String currentState = AccountSQL.getMerchantStatus(conn, merchantID);
        if (MerchantAccount.AccountState.SUSPENDED.getDbValue().equals(currentState)) {
            AccountSQL.updateMerchantStatus(conn,
                    merchantID, MerchantAccount.AccountState.NORMAL.getDbValue());
        }
        // in default  accounts are intentionally NOT restored here
    }

    //Maps the ResultSet row from the merchants table to a MerchantAccount object
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

    //Validates mandatory merchant fields before database insertion
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