package com.prototype.ipossa.systems.Accounts;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AccountSQL {

    //validate staff accounts
    public static boolean validateUser(Connection conn, String username, String password) throws SQLException {
        String query = "SELECT * FROM logins WHERE username = ? AND password = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.setString(2, password);
            return st.executeQuery().next();
        }
    }

    //get role from staff member
    public static String getUserRole(Connection conn, String username) throws SQLException {
        String query = "SELECT role FROM logins WHERE username = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return rs.getString("role");
        }
        return null;
    }

    // Log in a staff member — returns a UserAccount or null if credentials wrong
    public static UserAccount loginStaff(Connection conn, String username, String password) throws SQLException {
        String query = "SELECT * FROM logins WHERE username = ? AND password = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.setString(2, password);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new UserAccount(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"));
            }
        }
        return null;
    }

    // Get all staff accounts
    public static List<UserAccount> getAllStaff(Connection conn) throws SQLException {
        List<UserAccount> users = new ArrayList<>();
        String query = "SELECT username, role FROM logins";
        ResultSet rs = conn.prepareStatement(query).executeQuery();
        while (rs.next()) {
            users.add(new UserAccount(rs.getString("username"), "", rs.getString("role")));
        }
        return users;
    }

    // Create a new staff account
    public static void createUserAccount(Connection conn, String username, String password, String role) throws SQLException {
        String query = "INSERT INTO logins (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.setString(2, password);
            st.setString(3, role);
            st.executeUpdate();
        }
    }

    // Delete a staff account
    public static void deleteUserAccount(Connection conn, String username) throws SQLException {
        String query = "DELETE FROM logins WHERE username = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.executeUpdate();
        }
    }

    // Change a staff member's role
    public static void changeUserRole(Connection conn, String username, String newRole) throws SQLException {
        String query = "UPDATE logins SET role = ? WHERE username = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, newRole);
            st.setString(2, username);
            st.executeUpdate();
        }
    }

    // =========================================================================
    // MERCHANT ACCOUNTS (merchants table)
    // =========================================================================

    // Log in a merchant — returns a MerchantAccount or null if credentials wrong
    public static MerchantAccount loginMerchant(Connection conn, String login, String password) throws SQLException {
        String query = "SELECT * FROM merchants WHERE login = ? AND password = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, login);
            st.setString(2, password);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return mapToMerchant(rs);
            }
        }
        return null;
    }

    // Get a single merchant by their ID
    public static MerchantAccount getMerchant(Connection conn, int merchantID) throws SQLException {
        String query = "SELECT * FROM merchants WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, merchantID);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return mapToMerchant(rs);
        }
        return null;
    }

    // Get all merchant accounts
    public static List<MerchantAccount> getAllMerchants(Connection conn) throws SQLException {
        List<MerchantAccount> merchants = new ArrayList<>();
        String query = "SELECT * FROM merchants";
        ResultSet rs = conn.prepareStatement(query).executeQuery();
        while (rs.next()) {
            merchants.add(mapToMerchant(rs));
        }
        return merchants;
    }

    // Create a new merchant account
    public static void createMerchantAccount(Connection conn, String accountHolderName, String accountNumber,
                                             String contactName, String address, String phoneNumber,
                                             double creditLimit, String agreedDiscount,
                                             String login, String password) throws SQLException {
        String query = "INSERT INTO merchants (account_holder_name, account_number, contact_name, " +
                "address, phone_number, credit_limit, agreed_discount, login, password) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, accountHolderName);
            st.setString(2, accountNumber);
            st.setString(3, contactName);
            st.setString(4, address);
            st.setString(5, phoneNumber);
            st.setDouble(6, creditLimit);
            st.setString(7, agreedDiscount);
            st.setString(8, login);
            st.setString(9, password);
            st.executeUpdate();
        }
    }

    // Update merchant contact details
    public static void updateMerchantAccount(Connection conn, int merchantID, String contactName,
                                             String address, String phoneNumber) throws SQLException {
        String query = "UPDATE merchants SET contact_name = ?, address = ?, phone_number = ? WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, contactName);
            st.setString(2, address);
            st.setString(3, phoneNumber);
            st.setInt(4, merchantID);
            st.executeUpdate();
        }
    }

    // Delete a merchant account (also deletes their discount tiers first)
    public static void deleteMerchantAccount(Connection conn, int merchantID) throws SQLException {
        String deleteDiscounts = "DELETE FROM merchants_discounts WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(deleteDiscounts)) {
            st.setInt(1, merchantID);
            st.executeUpdate();
        }
        String deleteMerchant = "DELETE FROM merchants WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(deleteMerchant)) {
            st.setInt(1, merchantID);
            st.executeUpdate();
        }
    }

    // Set a merchant's credit limit
    public static void setCreditLimit(Connection conn, int merchantID, double creditLimit) throws SQLException {
        String query = "UPDATE merchants SET credit_limit = ? WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setDouble(1, creditLimit);
            st.setInt(2, merchantID);
            st.executeUpdate();
        }
    }

    // Update a merchant's account state (normal / suspended / in_default)
    public static void updateAccountState(Connection conn, int merchantID, String state) throws SQLException {
        String query = "UPDATE merchants SET account_state = ? WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, state);
            st.setInt(2, merchantID);
            st.executeUpdate();
        }
    }

    // =========================================================================
    // ACCOUNT STATE — automatic normal → suspended → in_default transitions
    // =========================================================================

    /**
     * Checks if a merchant's account state needs updating based on their
     * payment due date, and updates the DB if so.
     *
     * Rules from the brief:
     *   Payment due at end of month
     *   > 15 days late  → suspended
     *   > 30 days late  → in_default
     *   Paid on time    → stays normal
     */
    public static String checkAndUpdateAccountState(Connection conn, int merchantID, LocalDate paymentDueDate) throws SQLException {
        long daysLate = java.time.temporal.ChronoUnit.DAYS.between(paymentDueDate, LocalDate.now());

        String newState;
        if (daysLate <= 15) {
            newState = "normal";
        } else if (daysLate <= 30) {
            newState = "suspended";
        } else {
            newState = "in_default";
        }

        updateAccountState(conn, merchantID, newState);
        return newState;
    }

    // =========================================================================
    // DISCOUNT TIERS (merchants_discounts table)
    // =========================================================================

    // Get all discount tiers for a merchant
    public static ResultSet getDiscountTiers(Connection conn, int merchantID) throws SQLException {
        String query = "SELECT * FROM merchants_discounts WHERE merchant_ID = ?";
        PreparedStatement st = conn.prepareStatement(query);
        st.setInt(1, merchantID);
        return st.executeQuery();
    }

    // Add a discount tier for a merchant
    public static void addDiscountTier(Connection conn, int merchantID,
                                       Double minOrderValue, Double maxOrderValue,
                                       double discountRate) throws SQLException {
        String query = "INSERT INTO merchants_discounts (merchant_ID, min_order_value, max_order_value, discount_rate) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, merchantID);
            if (minOrderValue == null) st.setNull(2, Types.DECIMAL); else st.setDouble(2, minOrderValue);
            if (maxOrderValue == null) st.setNull(3, Types.DECIMAL); else st.setDouble(3, maxOrderValue);
            st.setDouble(4, discountRate);
            st.executeUpdate();
        }
    }

    // Delete all discount tiers for a merchant (used before replacing them)
    public static void deleteDiscountTiers(Connection conn, int merchantID) throws SQLException {
        String query = "DELETE FROM merchants_discounts WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, merchantID);
            st.executeUpdate();
        }
    }

    // =========================================================================
    // HELPER
    // =========================================================================

    // Maps a ResultSet row from the merchants table to a MerchantAccount object
    private static MerchantAccount mapToMerchant(ResultSet rs) throws SQLException {
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
}