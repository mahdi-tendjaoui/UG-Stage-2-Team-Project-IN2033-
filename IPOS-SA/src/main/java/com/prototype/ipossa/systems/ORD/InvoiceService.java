package com.prototype.ipossa.systems.ORD;

import java.util.ArrayList;
import java.util.List;

/**
 * The class Invoice service.
 */
public class InvoiceService {
    private List<Invoice> invoices;

    /**
     * Instantiates a new Invoice service.
     */
    public InvoiceService() {
        invoices = new ArrayList<>();
    }

    /**
     * Generate invoices.
     *
     * @param invoiceID   the invoice id
     * @param order       the order
     * @param invoiceDate the invoice date
     * @return the invoice
     */
    public Invoice generateInvoices(String invoiceID, Order order, String invoiceDate) {
        if (invoiceID == null || order == null || invoiceDate == null) {
            return null;
        }
        Invoice invoice = new Invoice(invoiceID, order.getOrderID(), order.getMerchantID(), order.getTotalAmount(), invoiceDate);
        invoices.add(invoice);
        order.setInvoiceID(invoiceID);
        return invoice;
    }

    /**
     * Search invoice.
     *
     * @param invoiceID the invoice id
     * @return the invoice
     */
    public Invoice searchInvoice(String invoiceID) {
        for (Invoice invoice : invoices) {
            if (invoice.getInvoiceID().equals(invoiceID)) {
                return invoice;
            }
        }
        return null;
    }

    /**
     * Gets invoices.
     *
     * @return the invoices
     */
    public List<Invoice> getInvoices() {
        return invoices;
    }
}
