public interface I_SupplierAPI {

	ProductList[] getProductCatalogue();

	/**
	 * 
	 * @param order
	 */
	boolean submitPurchaseOrder(Order order);

	/**
	 * 
	 * @param orderID
	 */
	string getDeliveryStatus(string orderID);

	Invoice[] getOutstandingInvoices();

}