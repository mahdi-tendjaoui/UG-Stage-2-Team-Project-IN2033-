package com.prototype.ipossa.systems.ORD;

public class OrderItem {
    private String productID;
    private String productName;
    private int quantity;
    private double unitPrice;

    public OrderItem(String productID, String productName, int quantity, double unitPrice) {
        this.productID = productID;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    public double getTotalPrice() {
        return unitPrice * quantity;
    }
    public String getProductID() {
        return productID;
    }
    public String getProductName() {
        return productName;
    }
    public int getQuantity() {
        return quantity;
    }
    public double getUnitPrice() {
        return unitPrice;
    }
}
