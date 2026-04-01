public class Order {

    private String orderId;
    private String merchantId;
    private double totalAmount;
    private String status;

    /**
     * Default constructor
     */
    public Order() {
    }

    /**
     * Constructor with the main order details
     */
    public Order(String orderId, String merchantId, double totalAmount, String status) {
        this.orderId = orderId;
        this.merchantId = merchantId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}