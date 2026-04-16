package com.prototype.ipossa.systems.ORD;

public class Invoice {
    private String invoiceID;
    private String orderID;
    private String merchantID;
    private double amount;
    private String invoiceDate;

    public Invoice(String invoiceID, String orderID, String merchantID, double amount, String invoiceDate) {
        this.invoiceID = invoiceID;
        this.orderID = orderID;
        this.merchantID = merchantID;
        this.amount = amount;
        this.invoiceDate = invoiceDate;
    }
    public String getInvoiceID() {
        return invoiceID;
    }
    public String getOrderID() {
        return orderID;
    }
    public String getMerchantID() {
        return merchantID;
    }
    public double getAmount() {
        return amount;
    }
    public String getInvoiceDate() {
        return invoiceDate;
    }
}
