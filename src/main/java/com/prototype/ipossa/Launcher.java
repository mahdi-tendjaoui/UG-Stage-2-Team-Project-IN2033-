package com.prototype.ipossa;

import com.prototype.ipossa.systems.Accounts.AccountSQL;
import javafx.application.Application;

import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.sql.Connection;
import java.sql.ResultSet;

public class Launcher extends Application {

    //Screen width and height
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public static void main(String[] args) throws Exception {
        launch(args);

        //Testing the classes
//        Connection conn = MyJDBC.getConnection();
//        System.out.println("=== Connected to DB ===\n");
//
//        // --- LOGINS ---
//        System.out.println("--- Staff Logins ---");
//        ResultSet users = AccountSQL.getAllUsers(conn);
//        while (users.next()) {
//            System.out.println("Username: " + users.getString("username") +
//                    " | Role: "     + users.getString("role"));
//        }
//
//        // --- CATALOGUE ---
//        System.out.println("\n--- Catalogue ---");
//        ResultSet catalogue = conn.prepareStatement("SELECT * FROM catalogue").executeQuery();
//        while (catalogue.next()) {
//            System.out.println("ID: "          + catalogue.getString("item_ID") +
//                    " | Name: "     + catalogue.getString("description") +
//                    " | Cost: £"    + catalogue.getString("package_cost") +
//                    " | Stock: "    + catalogue.getString("availability") +
//                    " | Limit: "    + catalogue.getString("stock_limit"));
//        }
//
//        // --- MERCHANTS ---
//        System.out.println("\n--- Merchants ---");
//        ResultSet merchants = AccountSQL.getAllMerchants(conn);
//        while (merchants.next()) {
//            System.out.println("Account No: "  + merchants.getString("account_number") +
//                    " | Name: "     + merchants.getString("account_holder_name") +
//                    " | Contact: "  + merchants.getString("contact_name") +
//                    " | Phone: "    + merchants.getString("phone_number") +
//                    " | Credit: £"  + merchants.getString("credit_limit") +
//                    " | Discount: " + merchants.getString("agreed_discount") +
//                    " | Status: "   + merchants.getString("account_state"));
//        }
//
//        // --- MERCHANT DISCOUNTS ---
//        System.out.println("\n--- Merchant Discount Tiers ---");
//        ResultSet discounts = conn.prepareStatement("SELECT m.account_holder_name, d.min_order_value, d.max_order_value, d.discount_rate " +
//                "FROM merchants_discounts d " +
//                "JOIN merchants m ON d.merchant_ID = m.merchant_ID").executeQuery();
//        while (discounts.next()) {
//            System.out.println("Merchant: "    + discounts.getString("account_holder_name") +
//                    " | Min: £"     + discounts.getString("min_order_value") +
//                    " | Max: £"     + discounts.getString("max_order_value") +
//                    " | Rate: "     + discounts.getString("discount_rate") + "%");
//        }
//
//        conn.close();
//        System.out.println("\n=== Done ===");
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
