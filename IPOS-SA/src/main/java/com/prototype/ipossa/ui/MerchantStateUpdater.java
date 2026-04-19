package com.prototype.ipossa.ui;

import com.prototype.ipossa.MyJDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * The type Merchant state updater.
 */
public class MerchantStateUpdater {

    /**
     * Refresh all.
     */
    public static void refreshAll() {
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement(
                    "SELECT merchant_ID, account_state FROM merchants").executeQuery();
            while (rs.next()) refresh(conn, rs.getInt(1), rs.getString(2));
        } catch (Exception e) {
            System.err.println("[MerchantStateUpdater] " + e.getMessage());
        }
    }

    /**
     * Refresh one.
     *
     * @param merchantId the merchant id
     */
    public static void refreshOne(int merchantId) {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT account_state FROM merchants WHERE merchant_ID=?")) {
            ps.setInt(1, merchantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) refresh(conn, merchantId, rs.getString(1));
        } catch (Exception e) {
            System.err.println("[MerchantStateUpdater] " + e.getMessage());
        }
    }

    private static void refresh(Connection conn, int id, String currentState) {
        long daysLate = daysLate(conn, id);
        String desired;

        if (daysLate <= 0) {

            if ("suspended".equalsIgnoreCase(currentState)) desired = "normal";
            else return;
        } else if (daysLate <= 15) {

            if ("suspended".equalsIgnoreCase(currentState) && balance(conn, id) <= 0) {
                desired = "normal";
            } else return;
        } else if (daysLate <= 30) {
            if ("in_default".equalsIgnoreCase(currentState)) return;
            desired = "suspended";
        } else {
            desired = "in_default";
        }

        if (!desired.equalsIgnoreCase(currentState)) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE merchants SET account_state=? WHERE merchant_ID=?")) {
                ps.setString(1, desired); ps.setInt(2, id); ps.executeUpdate();
                System.out.println("[MerchantStateUpdater] " + id + ": " + currentState + " → " + desired);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Days late long.
     *
     * @param merchantId the merchant id
     * @return the long
     */
    public static long daysLate(int merchantId) {
        try (Connection conn = MyJDBC.getConnection()) {
            return daysLate(conn, merchantId);
        } catch (Exception e) { return 0; }
    }

    private static long daysLate(Connection conn, int merchantId) {
        double totalPaid = 0;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(amount),0) FROM payments WHERE merchant_ID=?")) {
            ps.setInt(1, merchantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) totalPaid = rs.getDouble(1);
        } catch (Exception e) { return 0; }

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT order_date, total_amount FROM orders " +
                "WHERE merchant_ID=? ORDER BY order_date ASC, order_ID ASC")) {
            ps.setInt(1, merchantId);
            ResultSet rs = ps.executeQuery();
            double cumulative = 0;
            while (rs.next()) {
                java.sql.Date orderDate = rs.getDate(1);
                cumulative += rs.getDouble(2);
                if (cumulative > totalPaid && orderDate != null) {
                    LocalDate due = endOfNextMonth(orderDate.toLocalDate());
                    return Math.max(0, ChronoUnit.DAYS.between(due, LocalDate.now()));
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private static double balance(Connection conn, int id) {
        double o = 0, p = 0;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE merchant_ID=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery(); if (rs.next()) o = rs.getDouble(1);
        } catch (Exception ignored) {}
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(amount),0) FROM payments WHERE merchant_ID=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery(); if (rs.next()) p = rs.getDouble(1);
        } catch (Exception ignored) {}
        return o - p;
    }

    private static LocalDate endOfNextMonth(LocalDate orderDate) {
        LocalDate firstOfNextMonth = orderDate.plusMonths(1).withDayOfMonth(1);
        return firstOfNextMonth.withDayOfMonth(firstOfNextMonth.lengthOfMonth());
    }

    /**
     * Should show reminder boolean.
     *
     * @param merchantId the merchant id
     * @return the boolean
     */
    public static boolean shouldShowReminder(int merchantId) {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT account_state FROM merchants WHERE merchant_ID=?")) {
            ps.setInt(1, merchantId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;
            String state = rs.getString(1);
            long late = daysLate(merchantId);
            return late > 0 && late <= 15 && "normal".equalsIgnoreCase(state);
        } catch (Exception e) { return false; }
    }
}
