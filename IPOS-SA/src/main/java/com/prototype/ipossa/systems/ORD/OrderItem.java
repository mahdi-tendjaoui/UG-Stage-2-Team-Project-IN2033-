package com.prototype.ipossa.systems.ORD;

/**
 * The class Order item.
 */
public class OrderItem {
    private String productID;
    private String productName;
    private int quantity;
    private double unitPrice;

    /**
     * Instantiates a new Order item.
     *
     * @param productID   the product id
     * @param productName the product name
     * @param quantity    the quantity
     * @param unitPrice   the unit price
     */
    public OrderItem(String productID, String productName, int quantity, double unitPrice) {
        this.productID = productID;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /**
     * Gets total price.
     *
     * @return the total price
     */
    public double getTotalPrice() {
        return unitPrice * quantity;
    }

    /**
     * Gets product id.
     *
     * @return the product id
     */
    public String getProductID() {
        return productID;
    }

    /**
     * Gets product name.
     *
     * @return the product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Gets quantity.
     *
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gets unit price.
     *
     * @return the unit price
     */
    public double getUnitPrice() {
        return unitPrice;
    }
}
