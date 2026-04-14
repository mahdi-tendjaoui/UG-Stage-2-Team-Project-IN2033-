package com.prototype.ipossa;

import com.prototype.ipossa.systems.Accounts.AccountSQL;
import com.prototype.ipossa.systems.Accounts.MerchantAccount;
import com.prototype.ipossa.systems.Accounts.UserAccount;
import javafx.application.Application;

import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

public class Launcher extends Application {

    //Screen width and height
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public static void main(String[] args) throws Exception {
        launch(args);
        //Testing the classes
        // ── DB TEST CODE ──────────────────────────────────────────────────────
        Connection conn = MyJDBC.getConnection();
        System.out.println("=== Connected to DB ===\n");

        // --- STAFF LOGINS ---
        System.out.println("--- Staff Accounts ---");
        List<UserAccount> staff = AccountSQL.getAllStaff(conn);
        for (UserAccount u : staff) {
            System.out.println("Username: " + u.getUsername() +
                    " | Role: "    + u.getRole());
        }

        // --- STAFF LOGIN TEST ---
        System.out.println("\n--- Staff Login Test (Sysdba) ---");
        UserAccount admin = AccountSQL.loginStaff(conn, "Sysdba", "London_weighting");
        if (admin != null) {
            System.out.println("Logged in as: " + admin.getUsername() +
                    " | Role: "   + admin.getRole() +
                    " | isAdmin: " + admin.isAdmin());
        } else {
            System.out.println("Login failed.");
        }

        // --- MERCHANTS ---
        System.out.println("\n--- Merchant Accounts ---");
        List<MerchantAccount> merchants = AccountSQL.getAllMerchants(conn);
        for (MerchantAccount m : merchants) {
            System.out.println("Account No: "  + m.getAccountNumber() +
                    " | Name: "     + m.getAccountHolderName() +
                    " | Contact: "  + m.getContactName() +
                    " | Phone: "    + m.getPhoneNumber() +
                    " | Credit: £"  + m.getCreditLimit() +
                    " | Discount: " + m.getAgreedDiscount() +
                    " | Status: "   + m.getAccountState());
        }

        // --- MERCHANT LOGIN TEST ---
        System.out.println("\n--- Merchant Login Test (city) ---");
        MerchantAccount merchant = AccountSQL.loginMerchant(conn, "city", "northampton");
        if (merchant != null) {
            System.out.println("Logged in as: " + merchant.getAccountHolderName() +
                    " | State: "      + merchant.getAccountState() +
                    " | Can order: "  + merchant.canPlaceOrders());
        } else {
            System.out.println("Merchant login failed.");
        }

        // --- MERCHANT DISCOUNT TIERS ---
        System.out.println("\n--- Merchant Discount Tiers ---");
        for (MerchantAccount m : merchants) {
            System.out.println("Tiers for: " + m.getAccountHolderName());
            ResultSet discounts = AccountSQL.getDiscountTiers(conn, m.getMerchantID());
            while (discounts.next()) {
                System.out.println("  Min: £"   + discounts.getString("min_order_value") +
                        " | Max: £"  + discounts.getString("max_order_value") +
                        " | Rate: "  + discounts.getString("discount_rate") + "%");
            }
        }

        conn.close();
        System.out.println("\n=== Done ===");
        System.out.println("\n=== Done ===");
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/prototype/ipossa/AccountView.fxml"));
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setTitle("IPOS-SA");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
