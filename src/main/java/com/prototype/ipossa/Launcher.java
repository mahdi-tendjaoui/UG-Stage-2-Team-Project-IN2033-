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
        // Uncomment the block below to test DB connectivity and AccountSQL methods
        // without launching the JavaFX UI. Run main() directly (not as JavaFX app).
        Connection conn = MyJDBC.getConnection();
        System.out.println("=== Connected to DB ===\n");

        // --- STAFF ACCOUNTS ---
        System.out.println("--- Staff Accounts ---");
        List<UserAccount> staff = AccountSQL.getAllStaff(conn);
        for (UserAccount u : staff) {
            System.out.println("Username: " + u.getUsername() +
                    " | Role: "     + u.getRole() +
                    " | isAdmin: "  + u.isAdmin() +
                    " | isManager: "+ u.isManager());
        }

        // --- STAFF LOGIN TEST ---
        System.out.println("\n--- Staff Login Test (Sysdba) ---");
        UserAccount admin = AccountSQL.loginStaff(conn, "Sysdba", "London_weighting");
        if (admin != null) {
            System.out.println("Logged in as: " + admin.getUsername() +
                    " | Role: "    + admin.getRole() +
                    " | isAdmin: " + admin.isAdmin());
        } else {
            System.out.println("Login failed.");
        }

        // --- MERCHANT ACCOUNTS ---
        System.out.println("\n--- Merchant Accounts ---");
        List<MerchantAccount> merchants = AccountSQL.getAllMerchants(conn);
        for (MerchantAccount m : merchants) {
            System.out.println("Account No: "   + m.getAccountNumber() +
                    " | Name: "       + m.getAccountHolderName() +
                    " | Contact: "    + m.getContactName() +
                    " | Phone: "      + m.getPhoneNumber() +
                    " | Credit: £"    + m.getCreditLimit() +
                    " | Discount: "   + m.getDiscountType() +
                    " | State: "      + m.getAccountState() +
                    " | Can order: "  + m.canPlaceOrders());
        }

        // --- MERCHANT LOGIN TEST ---
        System.out.println("\n--- Merchant Login Test (city / northampton) ---");
        MerchantAccount merchant = AccountSQL.loginMerchant(conn, "city", "northampton");
        if (merchant != null) {
            System.out.println("Logged in as: "  + merchant.getAccountHolderName() +
                    " | Discount type: " + merchant.getDiscountType() +
                    " | State: "         + merchant.getAccountState() +
                    " | Can order: "     + merchant.canPlaceOrders() +
                    " | isNormal: "      + merchant.isNormal() +
                    " | isSuspended: "   + merchant.isSuspended() +
                    " | isInDefault: "   + merchant.isInDefault());
        } else {
            System.out.println("Merchant login failed.");
        }

        // --- MERCHANT DISCOUNT TIERS ---
        System.out.println("\n--- Merchant Discount Tiers ---");
        for (MerchantAccount m : merchants) {
            System.out.println("Tiers for: " + m.getAccountHolderName()
                    + " (" + m.getDiscountType().getDbValue() + " plan)");
            ResultSet tiers = AccountSQL.getDiscountTiers(conn, m.getMerchantID());
            while (tiers.next()) {
                System.out.println("  Min: £"  + tiers.getString("min_order_value") +
                        " | Max: £" + tiers.getString("max_order_value") +
                        " | Rate: " + tiers.getString("discount_rate") + "%");
            }
        }

        conn.close();
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
