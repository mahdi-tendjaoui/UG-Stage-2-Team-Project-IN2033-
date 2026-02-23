public interface I_InventoryAPI {

	/**
	 * 
	 * @param criteria
	 */
	Product[] searchStock(string criteria);

	/**
	 * 
	 * @param itemID
	 */
	int getStockLevel(int itemID);

	/**
	 * 
	 * @param itemID
	 */
	float getRetailPrice(int itemID);

}