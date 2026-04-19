package com.prototype.ipossa.ui;

import com.prototype.ipossa.MyJDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * The type Schema init.
 */
public class SchemaInit {

    /**
     * Ensure tables.
     */
    public static void ensureTables() {
        try (Connection conn = MyJDBC.getConnection(); Statement st = conn.createStatement()) {

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS orders (
                    order_ID INT AUTO_INCREMENT PRIMARY KEY,
                    merchant_ID INT NOT NULL,
                    order_date DATE NOT NULL,
                    status VARCHAR(32) NOT NULL DEFAULT 'accepted',
                    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
                    invoice_ID VARCHAR(32) NULL,
                    INDEX (merchant_ID)
                )
                """);
            addColumnIfMissing(conn, "orders", "invoice_ID",   "VARCHAR(32) NULL");
            addColumnIfMissing(conn, "orders", "status",       "VARCHAR(32) NOT NULL DEFAULT 'accepted'");
            addColumnIfMissing(conn, "orders", "total_amount", "DECIMAL(12,2) NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "orders", "subtotal",     "DECIMAL(12,2) NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "orders", "discount",     "DECIMAL(12,2) NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "orders", "order_date",   "DATE NULL");
            addColumnIfMissing(conn, "orders", "merchant_ID",  "INT NULL");
            addColumnIfMissing(conn, "orders", "created_at",   "TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP");
            ensureOrderIdAutoIncrement(conn);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS order_items (
                    order_item_ID INT AUTO_INCREMENT PRIMARY KEY,
                    order_ID INT NOT NULL,
                    item_ID VARCHAR(32) NOT NULL,
                    description VARCHAR(255),
                    quantity INT NOT NULL,
                    unit_cost DECIMAL(10,2) NOT NULL,
                    INDEX (order_ID)
                )
                """);
            addColumnIfMissing(conn, "order_items", "description", "VARCHAR(255) NULL");
            addColumnIfMissing(conn, "order_items", "quantity",    "INT NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "order_items", "unit_cost",   "DECIMAL(10,2) NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "order_items", "unit_price",  "DECIMAL(10,2) NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "order_items", "subtotal",    "DECIMAL(12,2) NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "order_items", "line_total",  "DECIMAL(12,2) NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "order_items", "total",       "DECIMAL(12,2) NOT NULL DEFAULT 0");

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS payments (
                    payment_ID INT AUTO_INCREMENT PRIMARY KEY,
                    merchant_ID INT NOT NULL,
                    amount DECIMAL(12,2) NOT NULL,
                    payment_date DATE NOT NULL,
                    method VARCHAR(32) NOT NULL,
                    notes VARCHAR(255),
                    INDEX (merchant_ID)
                )
                """);
            addColumnIfMissing(conn, "payments", "method",         "VARCHAR(32) NOT NULL DEFAULT 'Bank transfer'");
            addColumnIfMissing(conn, "payments", "payment_method", "VARCHAR(32) NOT NULL DEFAULT 'Bank transfer'");
            addColumnIfMissing(conn, "payments", "notes",          "VARCHAR(255) NULL");
            addColumnIfMissing(conn, "payments", "payment_date",   "DATE NULL");

            forceColumnDefault(conn, "payments", "payment_method");
            forceColumnDefault(conn, "payments", "method");

            addColumnIfMissing(conn, "merchants", "login",            "VARCHAR(64) NULL");
            addColumnIfMissing(conn, "merchants", "password",         "VARCHAR(64) NULL");
            addColumnIfMissing(conn, "merchants", "account_state",    "VARCHAR(32) NOT NULL DEFAULT 'normal'");
            addColumnIfMissing(conn, "merchants", "agreed_discount",  "VARCHAR(32) NULL");
            addColumnIfMissing(conn, "merchants", "credit_limit",     "DECIMAL(12,2) NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "merchants", "last_payment_due", "DATE NULL");

            seedMerchantLogins(conn);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS applications (
                    application_ID INT AUTO_INCREMENT PRIMARY KEY,
                    applicant_name VARCHAR(255),
                    email VARCHAR(255),
                    address VARCHAR(512),
                    app_type VARCHAR(32) NOT NULL DEFAULT 'non-commercial',
                    status VARCHAR(32) NOT NULL DEFAULT 'pending',
                    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    notes VARCHAR(512)
                )
                """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS user_emails (
                    username VARCHAR(64) PRIMARY KEY,
                    email VARCHAR(255)
                )
                """);
        } catch (Exception e) {
            System.err.println("[SchemaInit] " + e.getMessage());
        }
    }

    /**
     * addColumnIfMissing
     * @param conn
     * @param table
     * @param column
     * @param definition
     */
    private static void addColumnIfMissing(Connection conn, String table, String column, String definition) {
        try (var rs = conn.getMetaData().getColumns(null, null, table, column)) {
            if (rs.next()) return;
        } catch (Exception ignored) {}
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            System.out.println("[SchemaInit] added " + table + "." + column);
        } catch (Exception e) {
            System.err.println("[SchemaInit] could not add " + table + "." + column + ": " + e.getMessage());
        }
    }

    /**
     * forceColumnDefault
     * @param conn
     * @param table
     * @param column
     */
    private static void forceColumnDefault(Connection conn, String table, String column) {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN " + column +
                    " VARCHAR(64) NOT NULL DEFAULT 'Bank transfer'");
            System.out.println("[SchemaInit] forced default on " + table + "." + column);
        } catch (Exception ignored) {

        }
    }

    /**
     * ensureOrderIdAutoIncrement
     * @param conn
     */
    private static void ensureOrderIdAutoIncrement(Connection conn) {
        try (var rs = conn.getMetaData().getColumns(null, null, "orders", "order_ID")) {
            if (!rs.next()) return;
            String auto = "";
            try { auto = rs.getString("IS_AUTOINCREMENT"); } catch (Exception ignored) {}
            if ("YES".equalsIgnoreCase(auto)) return;
        } catch (Exception ignored) { return; }
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("ALTER TABLE orders MODIFY COLUMN order_ID INT NOT NULL AUTO_INCREMENT");
            System.out.println("[SchemaInit] promoted orders.order_ID to AUTO_INCREMENT");
        } catch (Exception e) {
            System.err.println("[SchemaInit] could not promote orders.order_ID to AUTO_INCREMENT: "
                    + e.getMessage() + "  (new orders will use a manual MAX+1 fallback)");
        }
    }

    /**
     * seedMerchantLogins
     * @param conn
     */
    private static void seedMerchantLogins(Connection conn) {
        String[][] seeds = {
                {"CityPharmacy",  "city",    "northampton"},
                {"Cosymed Ltd",   "cosymed", "bondstreet"},
                {"HelloPharmacy", "hello",   "there"},
        };
        for (String[] s : seeds) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE merchants SET login=?, password=? " +
                    "WHERE TRIM(REPLACE(account_holder_name,'.','')) = TRIM(REPLACE(?,'.','')) " +
                    "  AND (login IS NULL OR TRIM(login)='' OR password IS NULL OR TRIM(password)='')")) {
                ps.setString(1, s[1]);
                ps.setString(2, s[2]);
                ps.setString(3, s[0]);
                int n = ps.executeUpdate();
                if (n > 0) System.out.println("[SchemaInit] seeded login for " + s[0]);
            } catch (Exception e) {
                System.err.println("[SchemaInit] seed failed for " + s[0] + ": " + e.getMessage());
            }
        }
    }
}
