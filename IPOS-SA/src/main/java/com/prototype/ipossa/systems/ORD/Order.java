package com.prototype.ipossa.systems.ORD;

import java.util.ArrayList;
import java.util.List;

/**
 * The class Order.
 */
public class Order {

    private String orderID;
    private String merchantID;
    private String merchantName;
    private double totalAmount;
    private OrderStatus status;
    private String invoiceID;
    private String orderDate;
    private List<OrderItem> items;

    /**
     * Instantiates a new Order.
     *
     * @param orderID      the order id
     * @param merchantID   the merchant id
     * @param merchantName the merchant name
     * @param totalAmount  the total amount
     * @param invoiceID    the invoice id
     * @param orderDate    the order date
     */
    public Order(String orderID, String merchantID, String merchantName, double totalAmount, String invoiceID, String orderDate) {
        this.orderID = orderID;
        this.merchantID = merchantID;
        this.merchantName = merchantName;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.ACCEPTED;
        this.invoiceID = invoiceID;
        this.orderDate = orderDate;
        this.items = new ArrayList<>();
    }

    /**
     * Calculate total amount double.
     *
     * @return the double
     */
    public double calculateTotalAmount() {
        double totalAmount = 0;
        for (OrderItem item : items) {
            totalAmount+=item.getTotalPrice();
        }
        return totalAmount;
    }

    /**
     * Remove item.
     *
     * @param productID the product id
     */
    public void removeItem(String productID) {
        items.removeIf(item -> item.getProductID().equals(productID));
        totalAmount = calculateTotalAmount();
    }

    /**
     * Add item.
     *
     * @param item the item
     */
    public void addItem(OrderItem item) {
        items.add(item);
        totalAmount = calculateTotalAmount();
    }

    /**
     * Gets items.
     *
     * @return the items
     */
    public List<OrderItem> getItems() {
        return items;
    }

    /**
     * The enum Order status.
     */
    public enum OrderStatus {
        /**
         * Accepted order status.
         */
        ACCEPTED,
        /**
         * Ready to dispatch order status.
         */
        READY_TO_DISPATCH,
        /**
         * Dispatched order status.
         */
        DISPATCHED,
        /**
         * Delivered order status.
         */
        DELIVERED
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    /**
     * Gets order id.
     *
     * @return the order id
     */
    public String getOrderID() {
        return orderID;
    }

    /**
     * Sets order id.
     *
     * @param orderID the order id
     */
    public void setOrderId(String orderID) {
        this.orderID = orderID;
    }

    /**
     * Gets merchant id.
     *
     * @return the merchant id
     */
    public String getMerchantID() {
        return merchantID;
    }

    /**
     * Sets merchant id.
     *
     * @param merchantID the merchant id
     */
    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }

    /**
     * Gets merchant name.
     *
     * @return the merchant name
     */
    public String getMerchantName() {
        return merchantName;
    }

    /**
     * Sets merchant name.
     *
     * @param merchantName the merchant name
     */
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    /**
     * Gets total amount.
     *
     * @return the total amount
     */
    public double getTotalAmount() {
        return totalAmount;
    }

    /**
     * Sets total amount.
     *
     * @param totalAmount the total amount
     */
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * Gets invoice id.
     *
     * @return the invoice id
     */
    public String getInvoiceID() {
        return invoiceID;
    }

    /**
     * Sets invoice id.
     *
     * @param invoiceID the invoice id
     */
    public void setInvoiceID(String invoiceID) {
        this.invoiceID = invoiceID;
    }

    /**
     * Gets order date.
     *
     * @return the order date
     */
    public String getOrderDate() {
        return orderDate;
    }

    /**
     * Sets order date.
     *
     * @param orderDate the order date
     */
    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
}
