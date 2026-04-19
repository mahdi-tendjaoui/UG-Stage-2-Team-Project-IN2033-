package com.prototype.ipossa.systems.ACC;

import java.sql.*;

/**
 * The class Account sql.
 */
public class AccountSQL {

    /**
     * Validate user boolean.
     *
     * @param conn     the connection
     * @param username the username
     * @param password the password
     * @return the boolean
     * @throws SQLException the sql exception
     */
    public static boolean validateUser(Connection conn, String username, String password) throws SQLException {
        String query = "SELECT * FROM logins WHERE username = ? AND password = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.setString(2, password);
            return st.executeQuery().next();
        }
    }

    /**
     * Gets user role.
     *
     * @param conn     the connection
     * @param username the username
     * @return the user role
     * @throws SQLException the sql exception
     */
    public static String getUserRole(Connection conn, String username) throws SQLException {
        String query = "SELECT role FROM logins WHERE username = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return rs.getString("role");
        }
        return null;
    }

    /**
     * Create user account.
     *
     * @param conn     the connection
     * @param username the username
     * @param password the password
     * @param role     the role
     * @throws SQLException the sql exception
     */
    public static void createUserAccount(Connection conn, String username, String password, String role) throws SQLException {
        String query = "INSERT INTO logins (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.setString(2, password);
            st.setString(3, role);
            st.executeUpdate();
        }
    }

    /**
     * Delete user account.
     *
     * @param conn     the connection
     * @param username the username
     * @throws SQLException the sql exception
     */
    public static void deleteUserAccount(Connection conn, String username) throws SQLException {
        String query = "DELETE FROM logins WHERE username = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.executeUpdate();
        }
    }

    /**
     * Change user role.
     *
     * @param conn     the connection
     * @param username the username
     * @param newRole  the new role
     * @throws SQLException the sql exception
     */
    public static void changeUserRole(Connection conn, String username, String newRole) throws SQLException {
        String query = "UPDATE logins SET role = ? WHERE username = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, newRole);
            st.setString(2, username);
            st.executeUpdate();
        }
    }

    /**
     * Gets all users.
     *
     * @param conn the connection
     * @return the all users
     * @throws SQLException the sql exception
     */
    public static ResultSet getAllUsers(Connection conn) throws SQLException {
        String query = "SELECT username, role FROM logins";
        return conn.prepareStatement(query).executeQuery();
    }

    /**
     * Authenticate merchant boolean.
     *
     * @param conn     the connection
     * @param login    the login
     * @param password the password
     * @return the boolean
     * @throws SQLException the sql exception
     */
    public static boolean authenticateMerchant(Connection conn, String login, String password) throws SQLException {
        String query = "SELECT 1 FROM merchants " +
                       "WHERE LOWER(TRIM(login)) = LOWER(TRIM(?)) AND password = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, login == null ? "" : login);
            st.setString(2, password == null ? "" : password);
            return st.executeQuery().next();
        }
    }

    /**
     * Create merchant account.
     *
     * @param conn              the connection
     * @param accountHolderName the account holder name
     * @param accountNumber     the account number
     * @param contactName       the contact name
     * @param address           the address
     * @param phoneNumber       the phone number
     * @param creditLimit       the credit limit
     * @param agreedDiscount    the agreed discount
     * @param login             the login
     * @param password          the password
     * @throws SQLException the sql exception
     */
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

    /**
     * Delete merchant account.
     *
     * @param conn       the connection
     * @param merchantID the merchant id
     * @throws SQLException the sql exception
     */
    public static void deleteMerchantAccount(Connection conn, int merchantID) throws SQLException {

        String[] queries = {
                "DELETE FROM order_items WHERE order_ID IN (SELECT order_ID FROM orders WHERE merchant_ID = ?)",
                "DELETE FROM orders WHERE merchant_ID = ?",
                "DELETE FROM payments WHERE merchant_ID = ?",
                "DELETE FROM merchants_discounts WHERE merchant_ID = ?",
                "DELETE FROM merchants WHERE merchant_ID = ?"
        };
        for (String q : queries) {
            try (PreparedStatement st = conn.prepareStatement(q)) {
                st.setInt(1, merchantID);
                st.executeUpdate();
            } catch (SQLException ignored) {

            }
        }
    }

    /**
     * Update merchant account.
     *
     * @param conn        the connection
     * @param merchantID  the merchant id
     * @param contactName the contact name
     * @param address     the address
     * @param phoneNumber the phone number
     * @throws SQLException the sql exception
     */
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

    /**
     * Sets credit limit.
     *
     * @param conn        the connection
     * @param merchantID  the merchant id
     * @param creditLimit the credit limit
     * @throws SQLException the sql exception
     */
    public static void setCreditLimit(Connection conn, int merchantID, double creditLimit) throws SQLException {
        String query = "UPDATE merchants SET credit_limit = ? WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setDouble(1, creditLimit);
            st.setInt(2, merchantID);
            st.executeUpdate();
        }
    }

    /**
     * Update merchant status.
     *
     * @param conn       the connection
     * @param merchantID the merchant id
     * @param status     the status
     * @throws SQLException the sql exception
     */
    public static void updateMerchantStatus(Connection conn, int merchantID, String status) throws SQLException {
        String query = "UPDATE merchants SET account_state = ? WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, status);
            st.setInt(2, merchantID);
            st.executeUpdate();
        }
    }

    /**
     * Gets merchant status.
     *
     * @param conn       the connection
     * @param merchantID the merchant id
     * @return the merchant status
     * @throws SQLException the sql exception
     */
    public static String getMerchantStatus(Connection conn, int merchantID) throws SQLException {
        String query = "SELECT account_state FROM merchants WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, merchantID);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return rs.getString("account_state");
        }
        return null;
    }

    /**
     * Gets merchant id.
     *
     * @param conn  the connection
     * @param login the login
     * @return the merchant id
     * @throws SQLException the sql exception
     */
    public static int getMerchantID(Connection conn, String login) throws SQLException {
        String query = "SELECT merchant_ID FROM merchants " +
                       "WHERE LOWER(TRIM(login)) = LOWER(TRIM(?))";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, login == null ? "" : login);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return rs.getInt("merchant_ID");
        }
        return -1;
    }

    /**
     * Gets all merchants.
     *
     * @param conn the connection
     * @return the all merchants
     * @throws SQLException the sql exception
     */
    public static ResultSet getAllMerchants(Connection conn) throws SQLException {
        String query = "SELECT * FROM merchants";
        return conn.prepareStatement(query).executeQuery();
    }

    /**
     * Add discount tier.
     *
     * @param conn          the connection
     * @param merchantID    the merchant id
     * @param minOrderValue the min order value
     * @param maxOrderValue the max order value
     * @param discountRate  the discount rate
     * @throws SQLException the sql exception
     */
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

    /**
     * Delete discount tiers.
     *
     * @param conn       the connection
     * @param merchantID the merchant id
     * @throws SQLException the sql exception
     */
    public static void deleteDiscountTiers(Connection conn, int merchantID) throws SQLException {
        String query = "DELETE FROM merchants_discounts WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, merchantID);
            st.executeUpdate();
        }
    }

}
