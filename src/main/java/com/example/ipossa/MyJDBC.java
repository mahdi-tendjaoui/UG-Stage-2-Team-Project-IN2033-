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
import java.util.Locale;

public class MyJDBC {
    public static void main (String[] args) throws ClassNotFoundException {
        String host, port, database, username, password;
        host = port = database = username = password = null;

        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i].toLowerCase(Locale.ROOT)) {
                case "-host":
                    host = args[++i];
                    break;
                case "-port":
                    port = args[++i];
                    break;
                case "-database":
                    database = args[++i];
                    break;
                case "-username":
                    username = args[++i];
                    break;
                case "-password":
                    password = args[++i];
                    break;
            }
        }
        if (host == null || port == null || database == null) {
            System.out.println("Host, port, database is null");
            return;
        }

        Class.forName("com.mysql.cj.jdbc.Driver");

        try (
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://" + host + ":" + port + "/" + database + "?sslmode=require", username, password
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
