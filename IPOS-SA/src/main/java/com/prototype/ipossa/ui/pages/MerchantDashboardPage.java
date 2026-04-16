package com.prototype.ipossa.ui.pages;

import com.prototype.ipossa.MyJDBC;
import com.prototype.ipossa.systems.ACC.MerchantAccount;
import com.prototype.ipossa.ui.UIUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MerchantDashboardPage {

    private final MerchantAccount merchant;
    public MerchantDashboardPage(MerchantAccount m) { this.merchant = m; }

    public Node build() {
        VBox root = new VBox(16);

        Label title = new Label("Welcome, " + merchant.getAccountHolderName());
        title.getStyleClass().add("h1");
        Label sub = new Label("Account " + merchant.getAccountNumber()
                + "  ·  state: " + merchant.getAccountState().getDbValue());
        sub.getStyleClass().add("dim");
        root.getChildren().addAll(title, sub);

        // Stats
        double balance = balance();
        int orderCount = countOrders();
        int pending = pendingOrders();

        HBox stats = new HBox(14);
        stats.getChildren().addAll(
                stat("Outstanding balance", String.format("£%.2f", balance), balance > 0),
                stat("Credit limit", String.format("£%.2f", merchant.getCreditLimit()), false),
                stat("Available credit", String.format("£%.2f", Math.max(0, merchant.getCreditLimit() - balance)), false),
                stat("Total orders", String.valueOf(orderCount), false),
                stat("Pending orders", String.valueOf(pending), false));
        for (Node n : stats.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);
        root.getChildren().add(stats);

        // Account info card
        VBox info = new VBox(8);
        info.getStyleClass().add("card");
        info.getChildren().add(UIUtil.h2("Account details"));
        info.getChildren().add(kv("Contact", merchant.getContactName()));
        info.getChildren().add(kv("Address", merchant.getAddress()));
        info.getChildren().add(kv("Phone",   merchant.getPhoneNumber()));
        info.getChildren().add(kv("Discount", merchant.getDiscountType() == null
                ? "—" : merchant.getDiscountType().getDbValue()));
        root.getChildren().add(info);

        // §8.1 — show overdue payment reminder every time merchant accesses their account
        com.prototype.ipossa.ui.MerchantStateUpdater.refreshOne(merchant.getMerchantID());
        if (com.prototype.ipossa.ui.MerchantStateUpdater.shouldShowReminder(merchant.getMerchantID())) {
            long late = com.prototype.ipossa.ui.MerchantStateUpdater.daysLate(merchant.getMerchantID());
            Label rem = new Label("⏰  Reminder: your payment is " + late + " day(s) overdue. "
                    + "Please settle the outstanding balance — accounts more than 15 days late are automatically suspended.");
            rem.getStyleClass().add("warning-banner");
            rem.setWrapText(true);
            rem.setMaxWidth(Double.MAX_VALUE);
            root.getChildren().add(rem);
        }
        if (merchant.getAccountState() == com.prototype.ipossa.systems.ACC.MerchantAccount.AccountState.SUSPENDED) {
            Label s = new Label("⚠  Your account is SUSPENDED — no new orders can be placed until "
                    + "the outstanding balance is cleared.");
            s.getStyleClass().add("warning-banner");
            s.setWrapText(true);
            s.setMaxWidth(Double.MAX_VALUE);
            root.getChildren().add(s);
        }
        if (merchant.getAccountState() == com.prototype.ipossa.systems.ACC.MerchantAccount.AccountState.IN_DEFAULT) {
            Label s = new Label("⛔  Your account is IN DEFAULT — please contact InfoPharma's "
                    + "Director of Operations to discuss reactivation.");
            s.getStyleClass().add("warning-banner");
            s.setWrapText(true);
            s.setMaxWidth(Double.MAX_VALUE);
            root.getChildren().add(s);
        }

        if (balance > 0) {
            Label warn = new Label("ℹ  You have an outstanding balance of £" + String.format("%.2f", balance) + ".");
            warn.getStyleClass().add("warning-banner");
            warn.setWrapText(true);
            warn.setMaxWidth(Double.MAX_VALUE);
            root.getChildren().add(warn);
        }

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        return sp;
    }

    private VBox stat(String label, String val, boolean alert) {
        VBox v = new VBox(6);
        v.getStyleClass().add(alert ? "stat-card-accent" : "stat-card");
        Label l = new Label(label); l.getStyleClass().add("stat-label");
        Label vv = new Label(val); vv.getStyleClass().add("stat-value");
        v.getChildren().addAll(l, vv);
        return v;
    }

    private HBox kv(String k, String v) {
        Label kk = new Label(k); kk.getStyleClass().add("dim"); kk.setMinWidth(100);
        Label vv = new Label(v == null ? "—" : v);
        HBox h = new HBox(10, kk, vv);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private double balance() {
        double o = 0, p = 0;
        try (Connection conn = MyJDBC.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE merchant_ID=?")) {
                ps.setInt(1, merchant.getMerchantID());
                ResultSet rs = ps.executeQuery(); if (rs.next()) o = rs.getDouble(1);
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(amount),0) FROM payments WHERE merchant_ID=?")) {
                ps.setInt(1, merchant.getMerchantID());
                ResultSet rs = ps.executeQuery(); if (rs.next()) p = rs.getDouble(1);
            }
        } catch (Exception ignored) {}
        return o - p;
    }

    private int countOrders() {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM orders WHERE merchant_ID=?")) {
            ps.setInt(1, merchant.getMerchantID());
            ResultSet rs = ps.executeQuery(); if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {} return 0;
    }
    private int pendingOrders() {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM orders WHERE merchant_ID=? AND status NOT IN ('delivered','archived')")) {
            ps.setInt(1, merchant.getMerchantID());
            ResultSet rs = ps.executeQuery(); if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {} return 0;
    }
}
