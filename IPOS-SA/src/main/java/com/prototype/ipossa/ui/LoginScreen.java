package com.prototype.ipossa.ui;

import com.prototype.ipossa.systems.ACC.AccountController;
import com.prototype.ipossa.systems.ACC.MerchantAccount;
import com.prototype.ipossa.systems.ACC.UserAccount;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginScreen {

    private final Stage stage;
    public LoginScreen(Stage stage) { this.stage = stage; }

    public void show() {
        StackPane root = new StackPane();
        root.getStyleClass().addAll("root", "login-root");

        VBox card = new VBox(14);
        card.setAlignment(Pos.TOP_LEFT);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(420);
        card.setMinWidth(420);

        Label title = new Label("IPOS-SA");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label("InfoPharma Ordering System - sign in to continue");
        subtitle.getStyleClass().add("login-subtitle");
        subtitle.setWrapText(true);

        ToggleGroup mode = new ToggleGroup();
        ToggleButton staffBtn = new ToggleButton("Staff");
        ToggleButton merchantBtn = new ToggleButton("Merchant");
        staffBtn.setToggleGroup(mode); merchantBtn.setToggleGroup(mode);
        staffBtn.setSelected(true);
        staffBtn.getStyleClass().add("toggle-button");
        merchantBtn.getStyleClass().add("toggle-button");
        staffBtn.setMaxWidth(Double.MAX_VALUE);
        merchantBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(staffBtn, Priority.ALWAYS);
        HBox.setHgrow(merchantBtn, Priority.ALWAYS);
        HBox modeRow = new HBox(8, staffBtn, merchantBtn);

        TextField userField = new TextField();
        userField.setPromptText("Username");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        Label status = new Label();
        status.setStyle("-fx-text-fill: -danger;");
        status.setWrapText(true);
        status.setMaxWidth(360);

        Button signIn = new Button("Sign in");
        signIn.setMaxWidth(Double.MAX_VALUE);
        signIn.getStyleClass().addAll("button", "button-primary");

        Runnable doLogin = () -> {
            String u = userField.getText().trim();
            String p = passField.getText();
            boolean asMerchant = merchantBtn.isSelected();
            if (u.isEmpty() || p.isEmpty()) { status.setText("Please enter both username and password."); return; }
            signIn.setDisable(true);
            status.setStyle("-fx-text-fill: -text-dim;");
            status.setText("Signing in…");

            new Thread(() -> {
                AccountController c = new AccountController();
                if (asMerchant) {
                    MerchantAccount m = c.merchantLogin(u, p);
                    javafx.application.Platform.runLater(() -> {
                        signIn.setDisable(false);
                        if (m == null) {
                            status.setStyle("-fx-text-fill: -danger;");
                            status.setText("Invalid merchant credentials.");
                        } else if (m.getAccountState() == MerchantAccount.AccountState.IN_DEFAULT) {
                            status.setStyle("-fx-text-fill: -danger;");
                            status.setText("Account is in default. Contact InfoPharma to reactivate.");
                        } else {
                            ThemeManager.get().unregister(stage.getScene());
                            new MainApp(stage, m).show();
                        }
                    });
                } else {
                    UserAccount acc = c.staffLogin(u, p);
                    javafx.application.Platform.runLater(() -> {
                        signIn.setDisable(false);
                        if (acc == null) {
                            status.setStyle("-fx-text-fill: -danger;");
                            status.setText("Invalid staff credentials.");
                        } else {
                            ThemeManager.get().unregister(stage.getScene());
                            new MainApp(stage, acc).show();
                        }
                    });
                }
            }, "login-worker").start();
        };

        signIn.setOnAction(e -> doLogin.run());
        passField.setOnAction(e -> doLogin.run());
        userField.setOnAction(e -> passField.requestFocus());

        ToggleButton themeBtn = new ToggleButton(ThemeManager.get().isDark() ? "☀ Light" : "🌙 Dark");
        themeBtn.setSelected(ThemeManager.get().isDark());
        themeBtn.getStyleClass().add("toggle-button");
        themeBtn.setOnAction(e -> {
            ThemeManager.get().toggle();
            themeBtn.setText(ThemeManager.get().isDark() ? "☀ Light" : "🌙 Dark");
        });
        HBox topRow = new HBox(themeBtn);
        topRow.setAlignment(Pos.CENTER_RIGHT);
        topRow.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(topRow, title, subtitle, new Region(),
                modeRow,
                new Label("Username"), userField,
                new Label("Password"), passField,
                signIn, status);

        VBox.setMargin(userField, new Insets(-6, 0, 0, 0));
        VBox.setMargin(passField, new Insets(-6, 0, 0, 0));

        root.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        Scene scene = new Scene(root, 1100, 760);
        ThemeManager.get().register(scene);
        stage.setScene(scene);
        stage.setTitle("IPOS-SA - Sign in");
        stage.show();

        javafx.application.Platform.runLater(userField::requestFocus);
    }
}
