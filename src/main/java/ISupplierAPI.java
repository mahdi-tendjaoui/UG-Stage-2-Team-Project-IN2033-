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