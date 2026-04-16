package com.prototype.ipossa.ui.pages;

import com.prototype.ipossa.MyJDBC;
import com.prototype.ipossa.systems.ACC.*;
import com.prototype.ipossa.ui.UIUtil;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SettingsPage {

    private final UserAccount user;
    public SettingsPage(UserAccount user) { this.user = user; }

    public Node build() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(buildTab("Account", accountPane()));
        tabs.getTabs().add(buildTab("System", systemPane()));

        VBox root = new VBox(14);
        root.getChildren().add(UIUtil.h2("Settings"));
        root.getChildren().add(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return root;
    }

    private Tab buildTab(String name, Node content) {
        Tab t = new Tab(name);
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        t.setContent(sp);
        return t;
    }

    private Node accountPane() {
        VBox box = new VBox(12); box.setPadding(new Insets(12));

        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.getChildren().add(UIUtil.h2("Your account"));

        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8);
        TextField username = new TextField(user.getUsername()); username.setDisable(true);
        TextField role = new TextField(user.getRole().getDbValue()); role.setDisable(true);
        TextField email = new TextField(loadEmail(user.getUsername()));
        email.setPromptText("your@email.com");

        PasswordField oldPass = new PasswordField();
        PasswordField newPass = new PasswordField();
        PasswordField confirm = new PasswordField();

        int r = 0;
        g.add(new Label("Username:"), 0, r); g.add(username, 1, r++);
        g.add(new Label("Role:"), 0, r); g.add(role, 1, r++);
        g.add(new Label("Email:"), 0, r); g.add(email, 1, r++);

        Button saveEmail = new Button("Save email");
        saveEmail.getStyleClass().addAll("button", "button-primary");
        saveEmail.setOnAction(e -> {
            saveEmail(user.getUsername(), email.getText().trim());
            UIUtil.info("Saved", "Email updated.");
        });

        g.add(new Label("Current password:"), 0, r); g.add(oldPass, 1, r++);
        g.add(new Label("New password:"), 0, r); g.add(newPass, 1, r++);
        g.add(new Label("Confirm password:"), 0, r); g.add(confirm, 1, r++);

        Button changePw = new Button("Change password");
        changePw.getStyleClass().addAll("button", "button-primary");
        changePw.setOnAction(e -> {
            if (!newPass.getText().equals(confirm.getText())) { UIUtil.warn("Mismatch", "New password and confirmation do not match."); return; }
            if (newPass.getText().isBlank()) { UIUtil.warn("Empty", "Password cannot be empty."); return; }
            try (Connection conn = MyJDBC.getConnection()) {
                if (!AccountSQL.validateUser(conn, user.getUsername(), oldPass.getText())) {
                    UIUtil.error("Incorrect", "Current password is incorrect."); return;
                }
                try (PreparedStatement st = conn.prepareStatement("UPDATE logins SET password=? WHERE username=?")) {
                    st.setString(1, newPass.getText()); st.setString(2, user.getUsername()); st.executeUpdate();
                }
                user.setPassword(newPass.getText());
                oldPass.clear(); newPass.clear(); confirm.clear();
                UIUtil.info("Success", "Password updated.");
            } catch (Exception ex) { UIUtil.error("Error", ex.getMessage()); }
        });

        card.getChildren().addAll(g, new HBox(8, saveEmail), new Separator(), new HBox(8, changePw));
        box.getChildren().add(card);
        return box;
    }

    private Node systemPane() {
        VBox box = new VBox(14); box.setPadding(new Insets(12));

        VBox theme = new VBox(8);
        theme.getStyleClass().add("card");
        theme.getChildren().add(UIUtil.h2("Appearance"));
        CheckBox darkCb = new CheckBox("Dark mode");
        darkCb.selectedProperty().bindBidirectional(com.prototype.ipossa.ui.ThemeManager.get().darkModeProperty());
        theme.getChildren().add(darkCb);

        VBox smtp = new VBox(8);
        smtp.getStyleClass().add("card");
        smtp.getChildren().add(UIUtil.h2("SMTP / email settings"));
        smtp.getChildren().add(UIUtil.dim("Outgoing mail server used to send outcome emails to applicants and reminders to debtors."));
        GridPane sg = new GridPane(); sg.setHgap(10); sg.setVgap(8);
        TextField host = new TextField("smtp.gmail.com");
        TextField port = new TextField("587");
        TextField from = new TextField("noreply@infopharma.example");
        CheckBox tls = new CheckBox("Use TLS"); tls.setSelected(true);
        int r = 0;
        sg.add(new Label("Host:"), 0, r); sg.add(host, 1, r++);
        sg.add(new Label("Port:"), 0, r); sg.add(port, 1, r++);
        sg.add(new Label("From address:"), 0, r); sg.add(from, 1, r++);
        sg.add(tls, 1, r++);
        Button saveSmtp = new Button("Save SMTP settings");
        saveSmtp.getStyleClass().addAll("button", "button-primary");
        saveSmtp.setDisable(!user.canManageUserAccounts());
        saveSmtp.setOnAction(e -> UIUtil.info("Saved", "SMTP settings saved."));
        smtp.getChildren().addAll(sg, saveSmtp);

        box.getChildren().addAll(theme, smtp);
        return box;
    }

    private String loadEmail(String username) {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT email FROM user_emails WHERE username=?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1) == null ? "" : rs.getString(1);
        } catch (Exception ignored) {}
        return "";
    }

    private void saveEmail(String username, String email) {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO user_emails (username, email) VALUES (?,?) " +
                     "ON DUPLICATE KEY UPDATE email=VALUES(email)")) {
            ps.setString(1, username); ps.setString(2, email); ps.executeUpdate();
        } catch (Exception e) { UIUtil.error("Error", e.getMessage()); }
    }
}
