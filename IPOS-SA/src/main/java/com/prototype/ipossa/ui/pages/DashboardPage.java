package com.prototype.ipossa.ui.pages;

import com.prototype.ipossa.MyJDBC;
import com.prototype.ipossa.systems.ACC.UserAccount;
import com.prototype.ipossa.ui.UIUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.ResultSet;

public class DashboardPage {

    private final UserAccount user;

    public DashboardPage(UserAccount user) { this.user = user; }

    public Node build() {
        VBox root = new VBox(16);
        root.getChildren().add(UIUtil.h2("Welcome back, " + user.getUsername()));
        root.getChildren().add(UIUtil.dim("Signed in as " + user.getRole() + ". Overview of your system."));

        HBox stats = new HBox(14);
        stats.getChildren().addAll(
                statCard("Merchants", count("SELECT COUNT(*) FROM merchants")),
                statCard("Catalogue items", count("SELECT COUNT(*) FROM catalogue")),
                statCard("Low-stock items", count("SELECT COUNT(*) FROM catalogue WHERE availability < stock_limit")),
                statCard("Pending orders", safeCount("SELECT COUNT(*) FROM orders WHERE status <> 'delivered'")),
                statCard("Staff accounts", count("SELECT COUNT(*) FROM logins"))
        );
        for (Node n : stats.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);
        root.getChildren().add(stats);

        HBox cols = new HBox(14);
        HBox.setHgrow(cols, Priority.ALWAYS);
        VBox.setVgrow(cols, Priority.ALWAYS);

        VBox leftCol = new VBox(10);
        leftCol.getStyleClass().add("card");
        leftCol.getChildren().add(UIUtil.h2("Recent orders"));
        leftCol.getChildren().add(recentOrdersList());
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        VBox rightCol = new VBox(10);
        rightCol.getStyleClass().add("card");
        rightCol.getChildren().add(UIUtil.h2("Merchant account states"));
        rightCol.getChildren().add(merchantStatesList());
        HBox.setHgrow(rightCol, Priority.ALWAYS);

        cols.getChildren().addAll(leftCol, rightCol);
        root.getChildren().add(cols);

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return sp;
    }

    private VBox statCard(String label, String value) {
        VBox v = new VBox(6);
        v.getStyleClass().add("stat-card");
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        v.getChildren().addAll(lbl, val);
        return v;
    }

    private Node recentOrdersList() {
        VBox box = new VBox(6);
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement("""
                SELECT o.order_ID, o.order_date, o.status, o.total_amount, m.account_holder_name
                FROM orders o LEFT JOIN merchants m ON m.merchant_ID = o.merchant_ID
                ORDER BY o.order_date DESC LIMIT 8
                """).executeQuery();
            boolean any = false;
            while (rs.next()) {
                any = true;
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 4, 6, 4));
                Label name = new Label("#" + rs.getInt("order_ID") + "  ·  " + rs.getString("account_holder_name"));
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label status = new Label(rs.getString("status"));
                status.getStyleClass().addAll("badge", "badge-normal");
                Label amt = new Label(String.format("£%.2f", rs.getDouble("total_amount")));
                amt.getStyleClass().add("dim");
                row.getChildren().addAll(name, spacer, amt, status);
                box.getChildren().add(row);
            }
            if (!any) box.getChildren().add(UIUtil.dim("No orders recorded yet."));
        } catch (Exception e) {
            box.getChildren().add(UIUtil.dim("Unable to load orders."));
        }
        return box;
    }

    private Node merchantStatesList() {
        VBox box = new VBox(6);
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement(
                    "SELECT account_holder_name, account_state FROM merchants ORDER BY account_holder_name"
            ).executeQuery();
            boolean any = false;
            while (rs.next()) {
                any = true;
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 4, 6, 4));
                Label name = new Label(rs.getString("account_holder_name"));
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                String state = rs.getString("account_state");
                if (state == null) state = "normal";
                Label s = new Label(state);
                s.getStyleClass().add("badge");
                s.getStyleClass().add(switch (state.toLowerCase()) {
                    case "suspended" -> "badge-suspended";
                    case "in_default", "in default" -> "badge-default";
                    default -> "badge-normal";
                });
                row.getChildren().addAll(name, spacer, s);
                box.getChildren().add(row);
            }
            if (!any) box.getChildren().add(UIUtil.dim("No merchants on record."));
        } catch (Exception e) {
            box.getChildren().add(UIUtil.dim("Unable to load merchants."));
        }
        return box;
    }

    private String count(String sql) {
        try (Connection conn = MyJDBC.getConnection()) {
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (Exception ignored) {}
        return "–";
    }
    private String safeCount(String sql) { return count(sql); }
}
