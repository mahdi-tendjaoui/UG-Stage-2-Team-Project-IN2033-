package iposSA;

public interface ISupplierAPI {

	ProductList[] getProductCatalogue();

	/**
	 * hello 
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
