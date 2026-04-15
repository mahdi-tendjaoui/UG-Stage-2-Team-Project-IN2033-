package com.prototype.ipossa.systems.Accounts;

public class MerchantAccount {

    // Enums

    // The three account states
    public enum AccountState {
        NORMAL("normal"),
        SUSPENDED("suspended"),
        IN_DEFAULT("in_default");

        private final String dbValue;

        AccountState(String dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue;
        }

        // Converts the raw db string back to the enum constant
        public static AccountState fromDbValue(String value) {
            for (AccountState state : values()) {
                if (state.dbValue.equals(value)) return state;
            }
            return NORMAL;
        }
    }

    // The two discount plan types
    public enum DiscountType {
        FIXED("Fixed"),
        VARIABLE("Variable");

        private final String dbValue;

        DiscountType(String dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue;
        }

        // Converts the raw DB string back to the enum constant
        public static DiscountType fromDbValue(String value) {
            for (DiscountType type : values()) {
                if (type.dbValue.equals(value)) return type;
            }
            return FIXED; // default if unrecognised
        }
    }

    private int merchantID;
    private String accountHolderName;
    private String accountNumber;
    private String contactName;
    private String address;
    private String phoneNumber;
    private double creditLimit;
    private DiscountType discountType;   // FIXED or VARIABLE
    private String login;
    private String password;
    private AccountState accountState;  // NORMAL, SUSPENDED, or IN_DEFAULT

    public MerchantAccount(int merchantID, String accountHolderName, String accountNumber, String contactName, String address, String phoneNumber, double creditLimit, String discountTypeStr, String login, String password, String accountStateStr) {
        this.merchantID = merchantID;
        this.accountHolderName = accountHolderName;
        this.accountNumber = accountNumber;
        this.contactName = contactName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.creditLimit = creditLimit;
        this.discountType = DiscountType.fromDbValue(discountTypeStr);
        this.login = login;
        this.password = password;
        this.accountState = AccountState.fromDbValue(accountStateStr);
    }

    // Account state checks
    // Merchant can only place orders if account is normal
    public boolean isNormal() { return accountState == AccountState.NORMAL; }
    public boolean isSuspended() { return accountState == AccountState.SUSPENDED; }
    public boolean isInDefault() { return accountState == AccountState.IN_DEFAULT; }
    public boolean canPlaceOrders() { return isNormal(); }

    // Getters & setters

    public int getMerchantID() { return merchantID; }
    public void setMerchantID(int id)  { this.merchantID = id; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String name) { this.accountHolderName = name; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String number) { this.accountNumber = number; }

    public String getContactName() { return contactName; }
    public void setContactName(String name) { this.contactName = name; }

    public String getAddress() { return address; }
    public void setAddress(String addr) { this.address = addr; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phone) { this.phoneNumber = phone; }

    public double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(double limit) { this.creditLimit = limit; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType t) { this.discountType = t; }

    // Returns the discount type as a raw DB string e.g. "Fixed" or "Variable"
    public String getDiscountTypeDbValue() { return discountType.getDbValue(); }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String pw)  { this.password = pw; }

    public AccountState getAccountState() { return accountState; }
    public void setAccountState(AccountState s){ this.accountState = s; }

    // Returns the account state as a raw DB string e.g. "normal", "suspended", "in_default"
    public String getAccountStateDbValue() { return accountState.getDbValue(); }

    @Override
    public String toString() {
        return accountHolderName + " (" + accountNumber + ") - " + accountState.getDbValue();
    }
}