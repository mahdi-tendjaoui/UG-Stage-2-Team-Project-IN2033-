package com.example.ipossa;

import java.sql.*;

public class MyJDBC {
    public static void main (String[] args) {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/ipos_sa_db",
                    "root",
                    "Creeper7?"
            );

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM catalogue");

            while(resultSet.next()) {
                System.out.println(resultSet.getString("catalogue_ID"));
                System.out.println(resultSet.getString("description"));
                System.out.println(resultSet.getString("package_type"));
                System.out.println(resultSet.getString("unit"));
                System.out.println(resultSet.getInt("units_in_a_pack"));
                System.out.println(resultSet.getBigDecimal("package_cost"));
                System.out.println(resultSet.getInt("availability"));
                System.out.println(resultSet.getInt("stock_limit"));
                System.out.println(resultSet.getInt("category_ID"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
