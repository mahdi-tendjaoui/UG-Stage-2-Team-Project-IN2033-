/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.prototype.ipossa.systems.ACC;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author arkz17
 */
public class AccountServiceTest {

    public AccountServiceTest() {
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
     * Test of shouldShowPaymentReminder method, of class AccountService.
     */
    @Test
    public void testShouldShowPaymentReminder_OneDayLate() {
        System.out.println("shouldShowPaymentReminder - 1 day late");
        AccountService instance = new AccountService();
        LocalDate paymentDueDate = LocalDate.now().minusDays(1);
        boolean expResult = true;
        boolean result = instance.shouldShowPaymentReminder(paymentDueDate);
        assertEquals(expResult, result);
    }

    /**
     * Test of shouldShowPaymentReminder method, of class AccountService.
     */
    @Test
    public void testShouldShowPaymentReminder_FifteenDaysLate() {
        System.out.println("shouldShowPaymentReminder - 15 days late");
        AccountService instance = new AccountService();
        LocalDate paymentDueDate = LocalDate.now().minusDays(15);
        boolean expResult = true;
        boolean result = instance.shouldShowPaymentReminder(paymentDueDate);
        assertEquals(expResult, result);
    }

    /**
     * Test of shouldShowPaymentReminder method, of class AccountService.
     */
    @Test
    public void testShouldShowPaymentReminder_SixteenDaysLate() {
        System.out.println("shouldShowPaymentReminder - 16 days late (outside reminder window)");
        AccountService instance = new AccountService();
        LocalDate paymentDueDate = LocalDate.now().minusDays(16);
        boolean expResult = false;
        boolean result = instance.shouldShowPaymentReminder(paymentDueDate);
        assertEquals(expResult, result);
    }

    /**
     * Test of shouldShowPaymentReminder method, of class AccountService.
     */
    @Test
    public void testShouldShowPaymentReminder_NotLate() {
        System.out.println("shouldShowPaymentReminder - payment not yet due");
        AccountService instance = new AccountService();
        LocalDate paymentDueDate = LocalDate.now().plusDays(5);
        boolean expResult = false;
        boolean result = instance.shouldShowPaymentReminder(paymentDueDate);
        assertEquals(expResult, result);
    }

    /**
     * Test of loginStaff method, of class AccountService.
     * Input:    conn=null, username="", password=""
     * Expected: null — invalid credentials with no connection returns null
     */
    @Test
    public void testLoginStaff() {
        System.out.println("loginStaff");
        java.sql.Connection conn = null;
        String username = "";
        String password = "";
        AccountService instance = new AccountService();
        UserAccount expResult = null;
        UserAccount result = null;
        try {
            result = instance.loginStaff(conn, username, password);
        } catch (Exception e) {
        }
        assertEquals(expResult, result);
    }

    /**
     * Test of loginMerchant method, of class AccountService.
     */
    @Test
    public void testLoginMerchant() {
        System.out.println("loginMerchant");
        java.sql.Connection conn = null;
        String login = "";
        String password = "";
        AccountService instance = new AccountService();
        MerchantAccount expResult = null;
        MerchantAccount result = null;
        try {
            result = instance.loginMerchant(conn, login, password);
        } catch (Exception e) {
        }
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createUserAccount method, of class AccountService.
     */
    @Test
    public void testCreateUserAccount() {
        System.out.println("createUserAccount");
        java.sql.Connection conn = null;
        String username = "";
        String password = "";
        String role = "";
        AccountService instance = new AccountService();
        try {
            instance.createUserAccount(conn, username, password, role);
        } catch (Exception e) {
        }
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteUserAccount method, of class AccountService.
     */
    @Test
    public void testDeleteUserAccount() {
        System.out.println("deleteUserAccount");
        java.sql.Connection conn = null;
        String username = "";
        AccountService instance = new AccountService();
        try {
            instance.deleteUserAccount(conn, username);
        } catch (Exception e) {
        }
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of changeUserRole method, of class AccountService.
     */
    @Test
    public void testChangeUserRole() {
        System.out.println("changeUserRole");
        java.sql.Connection conn = null;
        String username = "";
        String newRole = "";
        AccountService instance = new AccountService();
        try {
            instance.changeUserRole(conn, username, newRole);
        } catch (Exception e) {
        }
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllStaffAccounts method, of class AccountService.
     */
    @Test
    public void testGetAllStaffAccounts() {
        System.out.println("getAllStaffAccounts");
        java.sql.Connection conn = null;
        AccountService instance = new AccountService();
        List<UserAccount> expResult = null;
        List<UserAccount> result = null;
        try {
            result = instance.getAllStaffAccounts(conn);
        } catch (Exception e) {
        }
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMerchant method, of class AccountService.
     */
    @Test
    public void testGetMerchant() {
        System.out.println("getMerchant");
        java.sql.Connection conn = null;
        int merchantID = 0;
        AccountService instance = new AccountService();
        MerchantAccount expResult = null;
        MerchantAccount result = null;
        try {
            result = instance.getMerchant(conn, merchantID);
        } catch (Exception e) {
        }
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllMerchants method, of class AccountService.
     */
    @Test
    public void testGetAllMerchants() {
        System.out.println("getAllMerchants");
        java.sql.Connection conn = null;
        AccountService instance = new AccountService();
        List<MerchantAccount> expResult = null;
        List<MerchantAccount> result = null;
        try {
            result = instance.getAllMerchants(conn);
        } catch (Exception e) {
        }
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createMerchantAccount method, of class AccountService.
     */
    @Test
    public void testCreateMerchantAccount() {
        System.out.println("createMerchantAccount");
        java.sql.Connection conn = null;
        String accountHolderName = "";
        String accountNumber = "";
        String contactName = "";
        String address = "";
        String phoneNumber = "";
        double creditLimit = 0.0;
        String agreedDiscount = "";
        String login = "";
        String password = "";
        AccountService instance = new AccountService();
        try {
            instance.createMerchantAccount(conn, accountHolderName, accountNumber,
                    contactName, address, phoneNumber, creditLimit, agreedDiscount, login, password);
        } catch (Exception e) {
        }
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateMerchantDetails method, of class AccountService.
     */
    @Test
    public void testUpdateMerchantDetails() {
        System.out.println("updateMerchantDetails");
        java.sql.Connection conn = null;
        int merchantID = 0;
        String contactName = "";
        String address = "";
        String phoneNumber = "";
        AccountService instance = new AccountService();
        try {
            instance.updateMerchantDetails(conn, merchantID, contactName, address, phoneNumber);
        } catch (Exception e) {
        }
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteMerchantAccount method, of class AccountService.
     */
    @Test
    public void testDeleteMerchantAccount() {
        System.out.println("deleteMerchantAccount");
        java.sql.Connection conn = null;
        int merchantID = 0;
        AccountService instance = new AccountService();
        try {
            instance.deleteMerchantAccount(conn, merchantID);
        } catch (Exception e) {
        }
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCreditLimit method, of class AccountService.
     */
    @Test
    public void testSetCreditLimit() {
        System.out.println("setCreditLimit");
        java.sql.Connection conn = null;
        int merchantID = 0;
        double creditLimit = 0.0;
        AccountService instance = new AccountService();
        try {
            instance.setCreditLimit(conn, merchantID, creditLimit);
        } catch (Exception e) {
        }
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDiscountPlan method, of class AccountService.
     */
    @Test
    public void testSetDiscountPlan() {
        System.out.println("setDiscountPlan");
        java.sql.Connection conn = null;
        int merchantID = 0;
        List<DiscountTier> tiers = null;
        AccountService instance = new AccountService();
        try {
            instance.setDiscountPlan(conn, merchantID, tiers);
        } catch (Exception e) {
        }
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteDiscountPlan method, of class AccountService.
     */
    @Test
    public void testDeleteDiscountPlan() {
        System.out.println("deleteDiscountPlan");
        java.sql.Connection conn = null;
        int merchantID = 0;
        AccountService instance = new AccountService();
        try {
            instance.deleteDiscountPlan(conn, merchantID);
        } catch (Exception e) {
        }
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of reactivateDefaultAccount method, of class AccountService.
     */
    @Test
    public void testReactivateDefaultAccount() {
        System.out.println("reactivateDefaultAccount");
        java.sql.Connection conn = null;
        int merchantID = 0;
        AccountService instance = new AccountService();
        try {
            instance.reactivateDefaultAccount(conn, merchantID);
        } catch (Exception e) {
        }
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of handlePaymentReceived method, of class AccountService.
     */
    @Test
    public void testHandlePaymentReceived() {
        System.out.println("handlePaymentReceived");
        java.sql.Connection conn = null;
        int merchantID = 0;
        boolean balanceCleared = false;
        AccountService instance = new AccountService();
        try {
            instance.handlePaymentReceived(conn, merchantID, balanceCleared);
        } catch (Exception e) {
        }
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}