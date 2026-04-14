package com.prototype.ipossa.systems;

import com.prototype.ipossa.systems.Order.Order;

public interface ISupplierAPI {

	//ProductList[] getProductCatalogue();

	/**
	 * hello
	 * @param order
	 */
	boolean submitPurchaseOrder(Order order);

	/**
	 * 
	 * @param orderID
	 */
	String getDeliveryStatus(String orderID);

	//Invoice[] getOutstandingInvoices();

}