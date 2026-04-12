package main;

import java.util.ArrayList;
import java.util.List;

public class Order {

    private String orderID;
    private String merchantID;
    private String merchantName;
    private double totalAmount;
    private OrderStatus status;
    private String invoiceID;
    private String orderDate;
    private List<OrderItem> items;

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
    public double calculateTotalAmount() {
        double totalAmount = 0;
        for (OrderItem item : items) {
            totalAmount+=item.getTotalPrice();
        }
        return totalAmount;
    }
    public void removeItem(String productID) {
        items.removeIf(item -> item.getProductID().equals(productID));
        totalAmount = calculateTotalAmount();
    }
    public void addItem(OrderItem item) {
        items.add(item);
        totalAmount = calculateTotalAmount();
    }
    public List<OrderItem> getItems() {
        return items;
    }
    public enum OrderStatus {
        ACCEPTED,
        READY_TO_DISPATCH,
        DISPATCHED,
        DELIVERED
    }
    public OrderStatus getStatus() {
        return status;
    }
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    public String getOrderID() {
        return orderID;
    }
    public void setOrderId(String orderID) {
        this.orderID = orderID;
    }
    public String getMerchantID() {
        return merchantID;
    }
    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }
    public String getMerchantName() {
        return merchantName;
    }
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
    public double getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    public String getInvoiceID() {
        return invoiceID;
    }
    public void setInvoiceID(String invoiceID) {
        this.invoiceID = invoiceID;
    }
    public String getOrderDate() {
        return orderDate;
    }
    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
}