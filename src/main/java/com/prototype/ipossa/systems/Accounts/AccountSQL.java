package com.prototype.ipossa.systems.Accounts;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class AccountSQL {

    //STAFF ACCOUNT

    // Logs in a staff member
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

    // Returns all staff accounts as a list
    public static List<UserAccount> getAllStaff(Connection conn) throws SQLException {
        List<UserAccount> staff = new ArrayList<>();
        String query = "SELECT username, role FROM logins";
        ResultSet rs = conn.prepareStatement(query).executeQuery();
        while (rs.next()) {
            staff.add(new UserAccount(rs.getString("username"), "", rs.getString("role")));
        }
        return staff;
    }

    // Creates a new staff account
    public static void createStaffAccount(Connection conn, String username,
                                          String password, String role) throws SQLException {
        String query = "INSERT INTO logins (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.setString(2, password);
            st.setString(3, role);
            st.executeUpdate();
        }
    }

    // Deletes a staff account
    public static void deleteStaffAccount(Connection conn, String username) throws SQLException {
        String query = "DELETE FROM logins WHERE username = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, username);
            st.executeUpdate();
        }
    }

    //Changes the role of an existing staff account
    public static void changeStaffRole(Connection conn, String username,
                                       String newRole) throws SQLException {
        String query = "UPDATE logins SET role = ? WHERE username = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, newRole);
            st.setString(2, username);
            st.executeUpdate();
        }
    }

    // MERCHANT ACCOUNTS

    // Logs in a merchant.
    public static MerchantAccount loginMerchant(Connection conn, String login, String password)
            throws SQLException {
        String query = "SELECT * FROM merchants WHERE login = ? AND password = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, login);
            st.setString(2, password);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return mapToMerchant(rs);
        }
        return null;
    }

    // Returns one merchant by their ID
    public static MerchantAccount getMerchant(Connection conn, int merchantID)
            throws SQLException {
        String query = "SELECT * FROM merchants WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, merchantID);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return mapToMerchant(rs);
        }
        return null;
    }

    // Returns all merchant accounts as a list
    public static List<MerchantAccount> getAllMerchants(Connection conn) throws SQLException {
        List<MerchantAccount> merchants = new ArrayList<>();
        String query = "SELECT * FROM merchants";
        ResultSet rs = conn.prepareStatement(query).executeQuery();
        while (rs.next()) {
            merchants.add(mapToMerchant(rs));
        }
        return merchants;
    }

    // Creates a new merchant account
    public static void createMerchantAccount(Connection conn, String accountHolderName, String accountNumber, String contactName, String address, String phoneNumber, double creditLimit, String agreedDiscount, String login, String password) throws SQLException {
        String query = "INSERT INTO merchants (account_holder_name, account_number, " +
                "contact_name, address, phone_number, credit_limit, " +
                "agreed_discount, login, password) " +
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

    // Updates the contact details of an existing merchant
    public static void updateMerchantDetails(Connection conn, int merchantID, String contactName, String address, String phoneNumber) throws SQLException {
        String query = "UPDATE merchants SET contact_name = ?, address = ?, " +
                "phone_number = ? WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, contactName);
            st.setString(2, address);
            st.setString(3, phoneNumber);
            st.setInt(4, merchantID);
            st.executeUpdate();
        }
    }

    // Deletes a merchant account
    public static void deleteMerchantAccount(Connection conn, int merchantID) throws SQLException {
        // Delete discount tiers (foreign key constraint)
        String deleteDiscounts = "DELETE FROM merchants_discounts WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(deleteDiscounts)) {
            st.setInt(1, merchantID);
            st.executeUpdate();
        }
        // Then delete the merchant
        String deleteMerchant = "DELETE FROM merchants WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(deleteMerchant)) {
            st.setInt(1, merchantID);
            st.executeUpdate();
        }
    }

    // CREDIT LIMIT

    // Sets or changes the credit limit for a merchant
    public static void setCreditLimit(Connection conn, int merchantID,
                                      double creditLimit) throws SQLException {
        String query = "UPDATE merchants SET credit_limit = ? WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setDouble(1, creditLimit);
            st.setInt(2, merchantID);
            st.executeUpdate();
        }
    }

    // DISCOUNT PLANS

    //Returns all discount tiers for a merchant.
    public static ResultSet getDiscountTiers(Connection conn, int merchantID) throws SQLException {
        String query = "SELECT * FROM merchants_discounts WHERE merchant_ID = ?";
        PreparedStatement st = conn.prepareStatement(query);
        st.setInt(1, merchantID);
        return st.executeQuery();
    }

     // Adds a single discount tier for a merchant
     // For a fixed plan: call once with minOrderValue=0, maxOrderValue=null
     // For a variable plan: call once per tier with the appropriate value ranges.
    public static void addDiscountTier(Connection conn, int merchantID, Double minOrderValue, Double maxOrderValue, double discountRate) throws SQLException {
        String query = "INSERT INTO merchants_discounts " +
                "(merchant_ID, min_order_value, max_order_value, discount_rate) " +
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

     // Deletes all discount tiers for a merchant
     // Called before adding new tiers when modifying a discount plan,
     // or on its own when deleting the plan entirely.
    public static void deleteDiscountTiers(Connection conn, int merchantID) throws SQLException {
        String query = "DELETE FROM merchants_discounts WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, merchantID);
            st.executeUpdate();
        }
    }

    // ACCOUNT STATE MANAGEMENT

    //   0–15 days late = stays normal, show reminder on screen
    //   15–30 days late = suspended, no new orders accepted
    //   30+ days late = in_default

    // Checks how many days late a merchant's payment is and updates their
    // account state in the DB accordingly.
    // Call this every time a merchant logs in or their account is accessed.
    // Returns the new state as a String so the UI can show the right message.
    public static String checkAndUpdateAccountState(Connection conn, int merchantID, LocalDate paymentDueDate) throws SQLException {
        long daysLate = ChronoUnit.DAYS.between(paymentDueDate, LocalDate.now());

        String newState;
        if (daysLate <= 15) {
            newState = "normal";
        } else if (daysLate <= 30) {
            newState = "suspended";
        } else {
            newState = "in_default";
        }

        String update = "UPDATE merchants SET account_state = ? WHERE merchant_ID = ?";
        try (PreparedStatement st = conn.prepareStatement(update)) {
            st.setString(1, newState);
            st.setInt(2, merchantID);
            st.executeUpdate();
        }
        return newState;
    }

    // Returns true if the merchant is 1–15 days late on payment.
    // The UI should show a payment reminder on screen in this case.
    public static boolean shouldShowPaymentReminder(LocalDate paymentDueDate) {
        long daysLate = ChronoUnit.DAYS.between(paymentDueDate, LocalDate.now());
        return daysLate > 0 && daysLate <= 15;
    }

    // Automatically restores a SUSPENDED account to NORMAL when a payment is received.
    // Only works if the account is currently suspended — in_default accounts
    // require manual reactivation by the Manager
    public static void restoreToNormalAfterPayment(Connection conn, int merchantID) throws SQLException {
        String query = "UPDATE merchants SET account_state = 'normal' " +
                "WHERE merchant_ID = ? AND account_state = 'suspended'";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, merchantID);
            st.executeUpdate();
        }
    }

    // Manually reactivates an IN DEFAULT account back to NORMAL.
    // Can only be called from a button visible to Manager/Director of Operations only.
    public static void reactivateFromDefault(Connection conn, int merchantID) throws SQLException {
        String query = "UPDATE merchants SET account_state = 'normal' " +
                "WHERE merchant_ID = ? AND account_state = 'in_default'";
        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setInt(1, merchantID);
            st.executeUpdate();
        }
    }

    // HELPER

    // Maps a single row from the merchants ResultSet into a MerchantAccount object.
    // Used by loginMerchant, getMerchant and getAllMerchants to avoid code repetition.
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