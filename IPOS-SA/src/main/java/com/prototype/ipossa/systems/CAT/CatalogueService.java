package com.prototype.ipossa.systems.CAT;

import com.prototype.ipossa.MyJDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Backend layer for the catalogue.
 * All SQL lives here. CataloguePage calls these methods and gets CatalogueItem objects back.
 */
public class CatalogueService {

    /** Returns every item in the catalogue table. */
    public List<CatalogueItem> getAllItems() {
        List<CatalogueItem> items = new ArrayList<>();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT * FROM catalogue");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (Exception e) {
            System.err.println("CatalogueService.getAllItems: " + e.getMessage());
        }
        return items;
    }

    /** Returns items whose item_ID, description, or package_type match the keyword. */
    public List<CatalogueItem> searchItems(String keyword) {
        List<CatalogueItem> items = new ArrayList<>();
        String like = "%" + keyword + "%";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "SELECT * FROM catalogue WHERE item_ID LIKE ? OR description LIKE ? OR package_type LIKE ?")) {
            st.setString(1, like);
            st.setString(2, like);
            st.setString(3, like);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("CatalogueService.searchItems: " + e.getMessage());
        }
        return items;
    }

    /** Returns items where availability is below stock_limit. */
    public List<CatalogueItem> getLowStockItems() {
        List<CatalogueItem> items = new ArrayList<>();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "SELECT * FROM catalogue WHERE availability < stock_limit");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (Exception e) {
            System.err.println("CatalogueService.getLowStockItems: " + e.getMessage());
        }
        return items;
    }

    /** Inserts a new product into the catalogue. Returns true on success. */
    public boolean addProduct(CatalogueItem item) {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement("""
                     INSERT INTO catalogue (item_ID, description, package_type, unit,
                         units_in_a_pack, package_cost, availability, stock_limit)
                     VALUES (?,?,?,?,?,?,?,?)
                     """)) {
            st.setString(1, item.getItemId());
            st.setString(2, item.getDescription());
            st.setString(3, item.getPackageType());
            st.setString(4, item.getUnit());
            st.setInt(5, item.getUnitsInPack());
            st.setDouble(6, item.getPackageCost());
            st.setInt(7, item.getAvailability());
            st.setInt(8, item.getStockLimit());
            st.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("CatalogueService.addProduct: " + e.getMessage());
            return false;
        }
    }

    /** Updates all fields of an existing item (matched by item_ID). Returns true on success. */
    public boolean updateItem(CatalogueItem item) {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement("""
                     UPDATE catalogue SET description=?, package_type=?, unit=?,
                         units_in_a_pack=?, package_cost=?, availability=?, stock_limit=?
                     WHERE item_ID=?
                     """)) {
            st.setString(1, item.getDescription());
            st.setString(2, item.getPackageType());
            st.setString(3, item.getUnit());
            st.setInt(4, item.getUnitsInPack());
            st.setDouble(5, item.getPackageCost());
            st.setInt(6, item.getAvailability());
            st.setInt(7, item.getStockLimit());
            st.setString(8, item.getItemId());
            st.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("CatalogueService.updateItem: " + e.getMessage());
            return false;
        }
    }

    /** Adds the given quantity on top of existing stock (availability += quantity). */
    public boolean addStock(String itemId, int quantity) {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "UPDATE catalogue SET availability = availability + ? WHERE item_ID=?")) {
            st.setInt(1, quantity);
            st.setString(2, itemId);
            st.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("CatalogueService.addStock: " + e.getMessage());
            return false;
        }
    }

    /** Sets availability to an exact new value (used by the stock dialog). */
    public boolean updateStock(String itemId, int newQuantity) {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "UPDATE catalogue SET availability=? WHERE item_ID=?")) {
            st.setInt(1, newQuantity);
            st.setString(2, itemId);
            st.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("CatalogueService.updateStock: " + e.getMessage());
            return false;
        }
    }

    /** Updates the minimum stock threshold for an item. */
    public boolean setStockLimit(String itemId, int stockLimit) {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "UPDATE catalogue SET stock_limit=? WHERE item_ID=?")) {
            st.setInt(1, stockLimit);
            st.setString(2, itemId);
            st.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("CatalogueService.setStockLimit: " + e.getMessage());
            return false;
        }
    }

    /** Removes an item from the catalogue by its item_ID. */
    public boolean deleteItem(String itemId) {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "DELETE FROM catalogue WHERE item_ID=?")) {
            st.setString(1, itemId);
            st.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("CatalogueService.deleteItem: " + e.getMessage());
            return false;
        }
    }

    /** Converts one DB row into a CatalogueItem. */
    private CatalogueItem mapRow(ResultSet rs) throws Exception {
        return new CatalogueItem(
                rs.getString("item_ID"),
                rs.getString("description"),
                rs.getString("package_type"),
                rs.getString("unit"),
                rs.getInt("units_in_a_pack"),
                rs.getDouble("package_cost"),
                rs.getInt("availability"),
                rs.getInt("stock_limit")
        );
    }
}
