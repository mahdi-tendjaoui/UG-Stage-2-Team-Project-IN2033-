public interface I_PaymentServiceAPI {

	/**
	 * 
	 * @param emailAddress
	 * @param cardNumber
	 */
	void startTransaction(string emailAddress, int cardNumber);

	/**
	 * 
	 * @param transaction
	 */
	boolean validateDetails(transaction transaction);

	void completeTransaction();

}