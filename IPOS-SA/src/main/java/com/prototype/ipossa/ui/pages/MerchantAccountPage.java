package com.prototype.ipossa.ui.pages;

import com.prototype.ipossa.MyJDBC;
import com.prototype.ipossa.systems.ACC.MerchantAccount;
import com.prototype.ipossa.ui.UIUtil;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class MerchantAccountPage {

    private final MerchantAccount merchant;
    public MerchantAccountPage(MerchantAccount m) { this.merchant = m; }

    public Node build() {
        VBox root = new VBox(14);

        // Account details (editable)
        VBox info = new VBox(10);
        info.getStyleClass().add("card");
        info.getChildren().add(UIUtil.h2("Contact details"));

        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8);
        TextField name = new TextField(merchant.getAccountHolderName()); name.setDisable(true);
        TextField acc  = new TextField(merchant.getAccountNumber()); acc.setDisable(true);
        TextField contact = new TextField(merchant.getContactName());
        TextField address = new TextField(merchant.getAddress());
        TextField phone   = new TextField(merchant.getPhoneNumber());

        int r = 0;
        g.add(new Label("Holder:"), 0, r); g.add(name, 1, r++);
        g.add(new Label("Account #:"), 0, r); g.add(acc, 1, r++);
        g.add(new Label("Contact:"), 0, r); g.add(contact, 1, r++);
        g.add(new Label("Address:"), 0, r); g.add(address, 1, r++);
        g.add(new Label("Phone:"), 0, r); g.add(phone, 1, r++);

        Button save = new Button("Save changes");
        save.getStyleClass().addAll("button", "button-primary");
        save.setOnAction(e -> {
            try (Connection conn = MyJDBC.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE merchants SET contact_name=?, address=?, phone_number=? WHERE merchant_ID=?")) {
                ps.setString(1, contact.getText().trim());
                ps.setString(2, address.getText().trim());
                ps.setString(3, phone.getText().trim());
                ps.setInt(4, merchant.getMerchantID());
                ps.executeUpdate();
                merchant.setContactName(contact.getText().trim());
                merchant.setAddress(address.getText().trim());
                merchant.setPhoneNumber(phone.getText().trim());
                UIUtil.info("Saved", "Your details have been updated.");
            } catch (Exception ex) { UIUtil.error("Error", ex.getMessage()); }
        });
        info.getChildren().addAll(g, save);

        // Password change
        VBox pw = new VBox(10);
        pw.getStyleClass().add("card");
        pw.getChildren().add(UIUtil.h2("Change password"));
        GridPane pg = new GridPane(); pg.setHgap(10); pg.setVgap(8);
        PasswordField oldP = new PasswordField();
        PasswordField newP = new PasswordField();
        PasswordField confP = new PasswordField();
        int r2 = 0;
        pg.add(new Label("Current:"), 0, r2); pg.add(oldP, 1, r2++);
        pg.add(new Label("New:"), 0, r2); pg.add(newP, 1, r2++);
        pg.add(new Label("Confirm:"), 0, r2); pg.add(confP, 1, r2++);
        Button pwBtn = new Button("Change password");
        pwBtn.getStyleClass().addAll("button", "button-primary");
        pwBtn.setOnAction(e -> {
            if (!oldP.getText().equals(merchant.getPassword())) {
                UIUtil.error("Wrong password", "Current password is incorrect."); return;
            }
            if (!newP.getText().equals(confP.getText())) {
                UIUtil.warn("Mismatch", "New passwords do not match."); return;
            }
            if (newP.getText().isBlank()) {
                UIUtil.warn("Empty", "Password cannot be blank."); return;
            }
            try (Connection conn = MyJDBC.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE merchants SET password=? WHERE merchant_ID=?")) {
                ps.setString(1, newP.getText());
                ps.setInt(2, merchant.getMerchantID());
                ps.executeUpdate();
                merchant.setPassword(newP.getText());
                oldP.clear(); newP.clear(); confP.clear();
                UIUtil.info("Updated", "Password changed.");
            } catch (Exception ex) { UIUtil.error("Error", ex.getMessage()); }
        });
        pw.getChildren().addAll(pg, pwBtn);

        root.getChildren().addAll(info, pw);
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setPadding(new Insets(0));
        return sp;
    }
}
