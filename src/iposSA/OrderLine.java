package iposSA;

/**
 * This class represents item inside an order.
 * It stores which product it is, how many were ordered,and the price per unit.
 */
public class OrderLine {

    private String itemId;
    private int quantity;
    private double unitPrice;

    /**
     * Creates an order line for a specific product.
     */
    public OrderLine(String itemId, int quantity, double unitPrice) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    /**
     * Calculates total price for this item (qty × price).
     */
    public double getLineTotal() {
        return quantity * unitPrice;
    }
}