package com.prototype.ipossa.systems.Catalogue;
import com.prototype.ipossa.MyJDBC;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
public class CatalogueService {
    // GET ALL ITEMS
    public List<CatalogueItem> getAllItems() {

        List<CatalogueItem> items = new ArrayList<>();
        String query = "SELECT * FROM catalogue";

        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(new CatalogueItem(
                        rs.getString("item_ID"),
                        rs.getString("description"),
                        rs.getString("package_type"),
                        rs.getString("unit"),
                        rs.getInt("units_in_a_pack"),
                        rs.getDouble("package_cost"),
                        rs.getInt("availability"),
                        rs.getInt("stock_limit")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }


    // SEARCH

    public List<CatalogueItem> searchItems(String keyword) {

        List<CatalogueItem> items = new ArrayList<>();
        String query = "SELECT * FROM catalogue WHERE item_ID LIKE ? OR description LIKE ?";

        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                items.add(new CatalogueItem(
                        rs.getString("item_ID"),
                        rs.getString("description"),
                        rs.getString("package_type"),
                        rs.getString("unit"),
                        rs.getInt("units_in_a_pack"),
                        rs.getDouble("package_cost"),
                        rs.getInt("availability"),
                        rs.getInt("stock_limit")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }


    // LOW STOCK

    public List<CatalogueItem> getLowStockItems() {

        List<CatalogueItem> items = new ArrayList<>();
        String query = "SELECT * FROM catalogue WHERE availability < stock_limit";

        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(new CatalogueItem(
                        rs.getString("item_ID"),
                        rs.getString("description"),
                        rs.getString("package_type"),
                        rs.getString("unit"),
                        rs.getInt("units_in_a_pack"),
                        rs.getDouble("package_cost"),
                        rs.getInt("availability"),
                        rs.getInt("stock_limit")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }


    // ADD PRODUCT

    public boolean addProduct(CatalogueItem item) {

        String query = "INSERT INTO catalogue (item_ID, description, package_type, unit, units_in_a_pack, package_cost, availability, stock_limit) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, item.getItemId());
            ps.setString(2, item.getDescription());
            ps.setString(3, item.getPackageType());
            ps.setString(4, item.getUnit());
            ps.setInt(5, item.getUnitsInPack());
            ps.setDouble(6, item.getPackageCost());
            ps.setInt(7, item.getAvailability());
            ps.setInt(8, item.getStockLimit());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // UPDATE ITEM

    public boolean updateItem(CatalogueItem item) {

        String query = "UPDATE catalogue SET description = ?, package_type = ?, unit = ?, units_in_a_pack = ?, package_cost = ?, availability = ?, stock_limit = ? WHERE item_ID = ?";

        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, item.getDescription());
            ps.setString(2, item.getPackageType());
            ps.setString(3, item.getUnit());
            ps.setInt(4, item.getUnitsInPack());
            ps.setDouble(5, item.getPackageCost());
            ps.setInt(6, item.getAvailability());
            ps.setInt(7, item.getStockLimit());
            ps.setString(8, item.getItemId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // ADD STOCK

    public boolean addStock(String itemId, int quantity) {

        String query = "UPDATE catalogue SET availability = availability + ? WHERE item_ID = ?";

        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, quantity);
            ps.setString(2, itemId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // SET STOCK LIMIT

    public boolean setStockLimit(String itemId, int stockLimit) {

        String query = "UPDATE catalogue SET stock_limit = ? WHERE item_ID = ?";

        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, stockLimit);
            ps.setString(2, itemId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // DELETE ITEM

    public boolean deleteItem(String itemId) {

        String query = "DELETE FROM catalogue WHERE item_ID = ?";

        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, itemId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}