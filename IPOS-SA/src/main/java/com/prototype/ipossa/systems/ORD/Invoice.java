package com.prototype.ipossa.systems.ORD;

/**
 * The type Invoice.
 */
public class Invoice {
    private String invoiceID;
    private String orderID;
    private String merchantID;
    private double amount;
    private String invoiceDate;

    /**
     * Instantiates a new Invoice.
     *
     * @param invoiceID   the invoice id
     * @param orderID     the order id
     * @param merchantID  the merchant id
     * @param amount      the amount
     * @param invoiceDate the invoice date
     */
    public Invoice(String invoiceID, String orderID, String merchantID, double amount, String invoiceDate) {
        this.invoiceID = invoiceID;
        this.orderID = orderID;
        this.merchantID = merchantID;
        this.amount = amount;
        this.invoiceDate = invoiceDate;
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
     * Gets order id.
     *
     * @return the order id
     */
    public String getOrderID() {
        return orderID;
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
     * Gets amount.
     *
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Gets invoice date.
     *
     * @return the invoice date
     */
    public String getInvoiceDate() {
        return invoiceDate;
    }
}
