package com.prototype.ipossa.systems.Accounts;

import java.sql.*;

public class AccountSQL {

    /*
    User Accounts
    Table used: logins
     */
    //Validate staff login
    public static boolean validateUser(Connection conn, String username, String password) throws SQLException {
        String query = "SELECT * FROM logins WHERE username = ? AND password = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.setString(2, password);
            return st.executeQuery().next();
        }
    }

    //Get role of user
    public static String getUserRole(Connection conn, String username) throws SQLException {
        String query = "SELECT role FROM logins WHERE username = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return rs.getString("role");
        }
        return null;
    }

    //Create new staff account
    public static void createUserAccount(Connection conn, String username, String password, String role) throws SQLException {
        String query = "INSERT INTO logins (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.setString(2, password);
            st.setString(3, role);
            st.executeUpdate();
        }
    }

    //Delete a staff account
    public static void deleteUserAccount(Connection conn, String username) throws SQLException {
        String query = "DELETE FROM logins WHERE username = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.executeUpdate();
        }
    }

    //Change a user's role
    public static void changeUserRole(Connection conn, String username, String newRole) throws SQLException {
        String query = "UPDATE logins SET role = ? WHERE username = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, newRole);
            st.setString(2, username);
            st.executeUpdate();
        }
    }

    //Get all user accounts
    public static ResultSet getAllUsers(Connection conn) throws SQLException {
        String query = "SELECT username, role FROM logins";
        return conn.prepareStatement(query).executeQuery();
    }

    /*
    Merchant accounts
    Tables used: merchants, merchants_discounts
     */

    //Validate merchant login
    public static boolean authenticateMerchant(Connection conn, String login, String password) throws SQLException {
        String query = "SELECT * FROM merchants WHERE login = ? AND password = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, login);
            st.setString(2, password);
            return st.executeQuery().next();
        }
    }

    //Create a new merchant account
    public static void createMerchantAccount(Connection conn, String accountHolderName, String accountNumber, String contactName, String address,
                                             String phoneNumber, double creditLimit, String agreedDiscount, String login, String password) throws SQLException {
        String query = "INSERT INTO merchants (account_holder_name, account_number, contact_name, address, " +
                        "phone_number, credit_limit, agreed_discount, login, password) " +
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

    //Delete a merchant account
    public static void deleteMerchantAccount(Connection conn, int merchantID) throws SQLException {
        String query1 = "DELETE FROM merchants_discounts WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query1)) {
            st.setInt(1, merchantID);
            st.executeUpdate();
        }
        String query2 = "DELETE FROM merchants WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query2)) {
            st.setInt(1, merchantID);
            st.executeUpdate();
        }
    }

    //Update merchant account details
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

    //Set credit limit
    public static void setCreditLimit(Connection conn, int merchantID, double creditLimit) throws SQLException {
        String query = "UPDATE merchants SET credit_limit = ? WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setDouble(1, creditLimit);
            st.setInt(2, merchantID);
            st.executeUpdate();
        }
    }

    //Update merchant account state
    public static void updateMerchantStatus(Connection conn, int merchantID, String status) throws SQLException {
        String query = "UPDATE merchants SET account_state = ? WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, status);
            st.setInt(2, merchantID);
            st.executeUpdate();
        }
    }

    // Get merchant account state
    public static String getMerchantStatus(Connection conn, int merchantID) throws SQLException {
        String query = "SELECT account_state FROM merchants WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, merchantID);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return rs.getString("account_state");
        }
        return null;
    }

    // Get merchant ID
    public static int getMerchantID(Connection conn, String login) throws SQLException {
        String query = "SELECT merchant_ID FROM merchants WHERE login = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, login);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return rs.getInt("merchant_ID");
        }
        return -1;
    }

    // Get all merchants
    public static ResultSet getAllMerchants(Connection conn) throws SQLException {
        String query = "SELECT * FROM merchants";
        return conn.prepareStatement(query).executeQuery();
    }

    /*
    Discount methods
    Tables used: merchants_discounts
     */

    //Add discount tier for a merchant
    public static void addDiscountTier(Connection conn, int merchantID, Double minOrderValue,
                                       Double maxOrderValue, double discountRate) throws SQLException {
        String query = "INSERT INTO merchants_discounts (merchant_ID, min_order_value, max_order_value, discount_rate) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, merchantID);
            if (minOrderValue == null) st.setNull(2, Types.DECIMAL);
            else st.setDouble(2, minOrderValue);
            if (maxOrderValue == null) st.setNull(3, Types.DECIMAL);
            else st.setDouble(3, maxOrderValue);
            st.setDouble(4, discountRate);
            st.executeUpdate();
        }
    }

    // Delete all discount tiers for a merchant (to be used before changing disocunt plan)
    public static void deleteDiscountTiers(Connection conn, int merchantID) throws SQLException {
        String query = "DELETE FROM merchants_discounts WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, merchantID);
            st.executeUpdate();
        }
    }

    // Get all discount tiers for a merchant
    public static ResultSet getDiscountTiers(Connection conn, int merchantID) throws SQLException {
        String query = "SELECT * FROM merchants_discounts WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, merchantID);
            return st.executeQuery();
        }
    }
}
