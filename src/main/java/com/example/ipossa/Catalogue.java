package com.example.ipossa;
import com.example.ipossa.CatalogueItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Catalogue {

    private final String url = "jdbc:mysql://localhost:3306/ipos_sa_db";
    private final String user = "root";
    private final String password = "sillywillyroot";


    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }


    public List<CatalogueItem> viewCatalogue() {
        List<CatalogueItem> items = new ArrayList<>();

        String query = "SELECT * FROM catalogue";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                CatalogueItem item = new CatalogueItem(
                        rs.getString("catalogue_ID"),
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

        String query = "SELECT * FROM catalogue WHERE description LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + keyword + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CatalogueItem item = new CatalogueItem(
                        rs.getString("catalogue_ID"),
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

    /**
     */
    public void addItem(CatalogueItem item) {
        String query = "INSERT INTO catalogue VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, item.getCatalogueId());
            stmt.setString(2, item.getDescription());
            stmt.setString(3, item.getPackageType());
            stmt.setString(4, item.getUnit());
            stmt.setInt(5, item.getUnitsInPack());
            stmt.setDouble(6, item.getPackageCost());
            stmt.setInt(7, item.getAvailability());
            stmt.setInt(8, item.getStockLimit());

            stmt.executeUpdate();

            System.out.println("✅ Item added");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *  UPDATE ITEM by ID
     *  
     */
    public void updateItem(CatalogueItem item) {
        String query = "UPDATE catalogue SET description=?, package_type=?, unit=?, units_in_a_pack=?, package_cost=?, availability=?, stock_limit=? WHERE catalogue_ID=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, item.getDescription());
            stmt.setString(2, item.getPackageType());
            stmt.setString(3, item.getUnit());
            stmt.setInt(4, item.getUnitsInPack());
            stmt.setDouble(5, item.getPackageCost());
            stmt.setInt(6, item.getAvailability());
            stmt.setInt(7, item.getStockLimit());
            stmt.setString(8, item.getCatalogueId());

            stmt.executeUpdate();

            System.out.println("✅ Item updated");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * DELETE ITEM
     */
    public void deleteItem(String catalogueId) {
        String query = "DELETE FROM catalogue WHERE catalogue_ID=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, catalogueId);
            stmt.executeUpdate();

            System.out.println("✅ Item deleted");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * LOW STOCK WAR
     */
    public List<CatalogueItem> getLowStockItems() {
        List<CatalogueItem> lowStock = new ArrayList<>();

        String query = "SELECT * FROM catalogue WHERE availability < stock_limit";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                CatalogueItem item = new CatalogueItem(
                        rs.getString("catalogue_ID"),
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