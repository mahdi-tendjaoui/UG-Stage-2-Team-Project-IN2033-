/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.prototype.ipossa.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import com.prototype.ipossa.MyJDBC;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author noelhabtu
 */
public class MerchantStateUpdaterTest {
    
    public MerchantStateUpdaterTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of refreshAll method, of class MerchantStateUpdater.
     */
    
    @Test
    public void testRefreshOne() throws Exception {
        System.out.println("refreshOne");

        int merchantId = -1;
        String orderId = null;

        try (Connection conn = MyJDBC.getConnection()) {

            PreparedStatement insMerchant = conn.prepareStatement(
                    "INSERT INTO merchants (account_holder_name, account_number, contact_name, address, phone_number, credit_limit, login, password, account_state) VALUES (?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            insMerchant.setString(1, "fake merchantcity");
            insMerchant.setString(2, "ACC001");
            insMerchant.setString(3, "city");
            insMerchant.setString(4, "23 beststreet");
            insMerchant.setString(5, "01234567890");
            insMerchant.setDouble(6, 5000.00);
            insMerchant.setString(7, "junit_" + System.currentTimeMillis());
            insMerchant.setString(8, "testpassword");
            insMerchant.setString(9, "normal");
            insMerchant.executeUpdate();

            ResultSet merchantKeys = insMerchant.getGeneratedKeys();
            if (merchantKeys.next()) {
                merchantId = merchantKeys.getInt(1);
            }

            ResultSet maxRs = conn.prepareStatement(
                    "SELECT COALESCE(MAX(CAST(order_ID AS UNSIGNED)),0)+1 FROM orders")
                    .executeQuery();
            if (maxRs.next()) {
                orderId = String.valueOf(maxRs.getLong(1));
            }

            LocalDate oldDate = LocalDate.now().minusMonths(6);

            PreparedStatement insOrder = conn.prepareStatement(
                    "INSERT INTO orders (order_ID, merchant_ID, order_date, status, subtotal, discount, total_amount) VALUES (?,?,?,?,?,?,?)");
            insOrder.setString(1, orderId);
            insOrder.setInt(2, merchantId);
            insOrder.setDate(3, java.sql.Date.valueOf(oldDate));
            insOrder.setString(4, "delivered");
            insOrder.setDouble(5, 200.00);
            insOrder.setDouble(6, 0.00);
            insOrder.setDouble(7, 200.00);
            insOrder.executeUpdate();
        }

        MerchantStateUpdater.refreshOne(merchantId);

        try (Connection conn = MyJDBC.getConnection()) {
            PreparedStatement check = conn.prepareStatement(
                    "SELECT account_state FROM merchants WHERE merchant_ID = ?");
            check.setInt(1, merchantId);
            ResultSet rs = check.executeQuery();

            assertTrue(rs.next());
            assertEquals("in_default", rs.getString("account_state"));
        }

        try (Connection conn = MyJDBC.getConnection()) {
            PreparedStatement delOrder = conn.prepareStatement(
                    "DELETE FROM orders WHERE order_ID = ?");
            delOrder.setString(1, orderId);
            delOrder.executeUpdate();

            PreparedStatement delMerchant = conn.prepareStatement(
                    "DELETE FROM merchants WHERE merchant_ID = ?");
            delMerchant.setInt(1, merchantId);
            delMerchant.executeUpdate();
        }
    }

    /**
     * Test of daysLate method, of class MerchantStateUpdater.
     */
   
    
}
