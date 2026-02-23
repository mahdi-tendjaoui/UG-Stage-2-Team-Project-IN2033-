public interface I_EmailServiceAPI {

	/**
	 * 
	 * @param recipient
	 * @param subject
	 * @param message
	 */
	boolean sendEmail(string recipient, string subject, string message);

	/**
	 * 
	 * @param messageID
	 */
	string getDeliveryStatus(string messageID);

	void optin();

	void optout();

}