public interface I_ApplicationAPI {

	/**
	 * 
	 * @param companyRegNumber
	 * @param businessType
	 * @param emailAddress
	 */
	string submitComercialApplication(int companyRegNumber, string businessType, string emailAddress);

	validMember[] retrieveValidMembers();

}