package com.prototype.ipossa.ui;

import com.prototype.ipossa.MyJDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Implements the §8.1 payment-state rules:
 *
 *  - End of calendar month after order placement → payment due
 *  - 1–15 days late → still 'normal', show reminder on login
 *  - 15–30 days late → 'suspended' (no new orders)
 *  - >30 days late  → 'in_default' (Director must reactivate)
 *  - Payment that fully clears the balance restores 'suspended' → 'normal'
 *
 * Days-late is computed from the OLDEST unpaid order's payment-due date,
 * not from a single static field. New orders correctly trigger transitions
 * even after old balances have been cleared.
 */
public class MerchantStateUpdater {

    public static void refreshAll() {
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement(
                    "SELECT merchant_ID, account_state FROM merchants").executeQuery();
            while (rs.next()) refresh(conn, rs.getInt(1), rs.getString(2));
        } catch (Exception e) {
            System.err.println("[MerchantStateUpdater] " + e.getMessage());
        }
    }

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
            // No outstanding overdue debt
            // Restore from 'suspended' but never auto-clear 'in_default'
            // (per §8.1 that requires explicit Director of Operations action)
            if ("suspended".equalsIgnoreCase(currentState)) desired = "normal";
            else return;
        } else if (daysLate <= 15) {
            // Within reminder grace period
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

    public static long daysLate(int merchantId) {
        try (Connection conn = MyJDBC.getConnection()) {
            return daysLate(conn, merchantId);
        } catch (Exception e) { return 0; }
    }

    /**
     * Walk orders oldest-first allocating cumulative payments. The first order
     * whose cumulative total exceeds cumulative paid is the oldest unpaid
     * order, and its end-of-next-month due date drives the days-late count.
     */
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
