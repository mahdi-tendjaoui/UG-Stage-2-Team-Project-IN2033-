package iposSA;

import java.util.ArrayList;
import java.util.List;

/**
 * class represents an order made by a merchant.
 * stores basic info order ID,items in the order,
 * total cost, and the current status of the order.
 */
public class Order {

    private String orderId;
    private String merchantId;
    private List<OrderLine> items;
    private double totalAmount;
    private String status;

    /**
     * Default constructor.
     *
     */
    public Order() {
        this.items = new ArrayList<>();
        this.totalAmount = 0.0;
        this.status = "PENDING";
    }

    /**
     * Creates an order with given details.
     * If no items are passed, it just creates an empty list.
     */
    public Order(String orderId, String merchantId, List<OrderLine> items) {
        this.orderId = orderId;
        this.merchantId = merchantId;
        this.items = (items != null) ? items : new ArrayList<>();
        this.totalAmount = calculateTotalAmount();
        this.status = "PENDING";
    }

    public String getOrderId() {
        return orderId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public List<OrderLine> getItems() {
        return items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Updates the order status PENDING, APPROVED, SHIPPED)
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Adds a new item to the order and updates the total price.
     */
    public void addItem(OrderLine item) {
        if (item != null) {
            this.items.add(item);
            this.totalAmount = calculateTotalAmount();
        }
    }

    /**
     * Loops through all items and calculates the total cost.
     */
    private double calculateTotalAmount() {
        double total = 0.0;
        for (OrderLine line : items) {
            total += line.getLineTotal();
        }
        return total;
    }
}