/*
Website URL: https://aiven.io/
Email: sanjeebilly05@gmail.com
Password: Creeper7?

Access the one server called: mysql-75ba1ad
Click on the name and click on three dots. Power on the service and wait for the server to be "running"
Then you can access the database
 */
package com.example.ipossa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MyJDBC {
    public static void main (String[] args) throws ClassNotFoundException {
        String host = "mysql-75ba1ad-ipos-sa-db.g.aivencloud.com";
        String port = "12995";
        String databaseName = "defaultdb";
        String username = "avnadmin";
        String password = "AVNS_RYEO3o9oYDlqbowxGy-";

        Class.forName("com.mysql.cj.jdbc.Driver");

        try (
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require", username, password
                );
                Statement statement = connection.createStatement();
                ) {

            //Code to run from the database
            ResultSet versionResult = statement.executeQuery("SELECT version() AS version");
            while (versionResult.next()) {
                System.out.println("Version: " + versionResult.getString("version"));
            }
            versionResult.close();

            ResultSet loginResult = statement.executeQuery("SELECT * FROM logins");
            while (loginResult.next()) {
                System.out.println(
                        "Username: " + loginResult.getString("username") + ", " +
                        "Password: " + loginResult.getString("password") + ", " +
                        "Role: " + loginResult.getString("role")
                );
            }
            loginResult.close();

            ResultSet catalogueResult = statement.executeQuery("SELECT * FROM catalogue");
            while (catalogueResult.next()) {
                System.out.println(
                        "Item ID: " + catalogueResult.getString("item_ID") + ", " +
                        "Description: " + catalogueResult.getString("description") + ", " +
                        "Package Type: " + catalogueResult.getString("package_type") + ", " +
                        "Unit: " + catalogueResult.getString("unit") + ", " +
                        "Units in a pack: " + catalogueResult.getInt("units_in_a_pack") + ", " +
                        "Package Cost: " + catalogueResult.getDouble("package_cost") + ", " +
                        "Availability: " + catalogueResult.getInt("availability") + ", " +
                        "Stock Limit: " + catalogueResult.getInt("stock_limit")
                );
            }
            catalogueResult.close();

        } catch (SQLException e) {
            System.out.println("Connection failure");
            e.printStackTrace();
        }
    }
}
