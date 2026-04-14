package com.prototype.ipossa.systems;

import com.prototype.ipossa.systems.Order.Order;

import java.util.ArrayList;
import java.util.List;

public class InvoiceService {
    private List<Invoice> invoices;

    public InvoiceService() {
        invoices = new ArrayList<>();
    }
    public Invoice generateInvoices(String invoiceID, Order order, String invoiceDate) {
        if (invoiceID == null || order == null || invoiceDate == null) {
            return null;
        }
        Invoice invoice = new Invoice(invoiceID, order.getOrderID(), order.getMerchantID(), order.getTotalAmount(), invoiceDate);
        invoices.add(invoice);
        order.setInvoiceID(invoiceID);
        return invoice;
    }
    public Invoice searchInvoice(String invoiceID) {
        for (Invoice invoice : invoices) {
            if (invoice.getInvoiceID().equals(invoiceID)) {
                return invoice;
            }
        }
        return null;
    }
    public List<Invoice> getInvoices() {
        return invoices;
    }
}
