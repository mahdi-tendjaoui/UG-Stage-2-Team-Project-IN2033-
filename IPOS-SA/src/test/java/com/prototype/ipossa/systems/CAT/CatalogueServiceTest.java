/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.prototype.ipossa.systems.CAT;

import java.util.List;
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
public class CatalogueServiceTest {
    
    public CatalogueServiceTest() {
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
    private CatalogueItem createTestItem(String itemId) {
        return new CatalogueItem(
                itemId,
                "JUnit Test Item",
                "Box",
                "Caps",
                10,
                2.50,
                5,
                2
        );
    }

    private CatalogueItem findItemById(List<CatalogueItem> items, String itemId) {
        for (CatalogueItem item : items) {
            if (item.getItemId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Test of getAllItems method, of class CatalogueService.
     */
    @Test
    public void testGetAllItems() {
        System.out.println("getAllItems");
        CatalogueService instance = new CatalogueService();
        List<CatalogueItem> result = instance.getAllItems();
        assertNotNull(result);

        assertFalse(result.isEmpty(), "Catalogue should be populated");
        assertNotNull(result.get(0).getItemId(), "First item has item ID");
        assertNotNull(result.get(0).getDescription(), "First item has description");

}

    /**
     * Test of searchItems method, of class CatalogueService.
     */
    @Test
    public void testSearchItems() {
        System.out.println("searchItems");
        String keyword = "para";
        CatalogueService instance = new CatalogueService();
        List<CatalogueItem> result = instance.searchItems(keyword);
        boolean allMatch = true;
        for (CatalogueItem i : result) {
            if (!i.getItemId().toLowerCase().contains("para")
                    && !i.getDescription().toLowerCase().contains("para")) {
                allMatch = false;
                break;
            }

        }

        assertTrue(allMatch);
    }

    /**
     * Test of getLowStockItems method, of class CatalogueService.
     */
    @Test
    public void testGetLowStockItems() {
        System.out.println("getLowStockItems");
        CatalogueService instance = new CatalogueService();
        List<CatalogueItem> result = instance.getLowStockItems();
        assertNotNull(result);
        boolean allLowStock = true;
        for (CatalogueItem i : result) {
            if (!(i.getAvailability() < i.getStockLimit())) {
                allLowStock = false;
                break;
            }

        }

        assertTrue(allLowStock, "the returned items should have availability less then the stock limit");

    }

    /**
     * Test of addProduct method, of class CatalogueService.
     */
    @Test
    public void testAddProduct() {
        System.out.println("addProduct");
        CatalogueService instance = new CatalogueService();

        String itemId = "10001 1999 " + String.valueOf(System.currentTimeMillis()).substring(7);
        CatalogueItem item = createTestItem(itemId);
        boolean result = instance.addProduct(item);
        assertTrue(result, "Item should be added ");
        List<CatalogueItem> items = instance.searchItems(itemId);
        CatalogueItem found = findItemById(items, itemId);
        assertNotNull(found, "check if added item is ofund");
        instance.deleteItem(itemId);

    }

    /**
     * Test of updateItem method, of class CatalogueService.
     */
    @Test
    public void testUpdateItem() {

        System.out.println("updateItem");

        CatalogueService instance = new CatalogueService();

        String itemId = "10001 998 " + String.valueOf(System.currentTimeMillis()).substring(7);
        CatalogueItem originalItem = createTestItem(itemId);
        instance.addProduct(originalItem);
        CatalogueItem updatedItem = new CatalogueItem(
                itemId,
                "Updated Test Item",
                "Bottle",
                "Caps",
                20,
                4.99,
                9,
                3
        );
        boolean result = instance.updateItem(updatedItem);

        assertTrue(result, "should be updated");
        List<CatalogueItem> items = instance.searchItems(itemId);
        CatalogueItem found = findItemById(items, itemId);
        assertNotNull(found, "Updated item should be found");
        assertEquals("Updated Test Item", found.getDescription());
        assertEquals("Bottle", found.getPackageType());
        assertEquals("Caps", found.getUnit());
        assertEquals(20, found.getUnitsInPack());
        assertEquals(4.99, found.getPackageCost(), 0.001);
        assertEquals(9, found.getAvailability());
        assertEquals(3, found.getStockLimit());
        instance.deleteItem(itemId);
    }
}