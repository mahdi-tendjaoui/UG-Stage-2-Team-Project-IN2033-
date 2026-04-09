package com.example.ipossa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles catalogue database operations.
 */
public class Catalogue {

    private final String host = "mysql-75ba1ad-ipos-sa-db.g.aivencloud.com";
    private final String port = "12995";
    private final String database = "defaultdb";
    private final String username = "avnadmin";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + database + "?sslmode=require",
                username,
                password
        );
    }

    public List<CatalogueItem> viewCatalogue() {
        List<CatalogueItem> items = new ArrayList<>();
        String query = "SELECT * FROM catalogue";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                CatalogueItem item = new CatalogueItem(
                        rs.getString("item_ID"),
                        rs.getString("description"),
                        rs.getString("package_type"),
                        rs.getString("unit"),
                        rs.getInt("units_in_a_pack"),
                        rs.getDouble("package_cost"),
                        rs.getInt("availability"),
                        rs.getInt("stock_limit")
                );
                items.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    public List<CatalogueItem> searchCatalogue(String keyword) {
        List<CatalogueItem> results = new ArrayList<>();
        String query = "SELECT * FROM catalogue WHERE item_ID LIKE ? OR description LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchTerm = "%" + keyword + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CatalogueItem item = new CatalogueItem(
                        rs.getString("item_ID"),
                        rs.getString("description"),
                        rs.getString("package_type"),
                        rs.getString("unit"),
                        rs.getInt("units_in_a_pack"),
                        rs.getDouble("package_cost"),
                        rs.getInt("availability"),
                        rs.getInt("stock_limit")
                );
                results.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public void deleteItem(String itemId) {
        String query = "DELETE FROM catalogue WHERE item_ID = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, itemId);
            stmt.executeUpdate();
            System.out.println("Deleted: " + itemId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addStock(String itemId, int amount) {
        String query = "UPDATE catalogue SET availability = availability + ? WHERE item_ID = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, amount);
            stmt.setString(2, itemId);
            stmt.executeUpdate();
            System.out.println("Stock updated");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<CatalogueItem> getLowStockItems() {
        List<CatalogueItem> lowStock = new ArrayList<>();
        String query = "SELECT * FROM catalogue WHERE availability < stock_limit";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                CatalogueItem item = new CatalogueItem(
                        rs.getString("item_ID"),
                        rs.getString("description"),
                        rs.getString("package_type"),
                        rs.getString("unit"),
                        rs.getInt("units_in_a_pack"),
                        rs.getDouble("package_cost"),
                        rs.getInt("availability"),
                        rs.getInt("stock_limit")
                );
                lowStock.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lowStock;
    }
}