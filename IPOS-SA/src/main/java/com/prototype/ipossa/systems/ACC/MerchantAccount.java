package com.prototype.ipossa.systems.ACC;

import java.util.ArrayList;
import java.util.List;

/**
 * The class Merchant account.
 */
public class MerchantAccount {
    private int merchantID;
    private String accountHolderName;
    private String accountNumber;
    private String contactName;
    private String address;
    private String phoneNumber;
    private double creditLimit;
    private DiscountType discountType;
    private String login;
    private String password;
    private AccountState accountState;
    private Role role;

    private List<DiscountTier> discountTiers = new ArrayList<>();

    /**
     * Instantiates a new Merchant account.
     *
     * @param merchantID        the merchant id
     * @param accountHolderName the account holder name
     * @param accountNumber     the account number
     * @param contactName       the contact name
     * @param address           the address
     * @param phoneNumber       the phone number
     * @param creditLimit       the credit limit
     * @param discountTypeStr   the discount type str
     * @param login             the login
     * @param password          the password
     * @param accountStateStr   the account state str
     */
    public MerchantAccount(int merchantID, String accountHolderName, String accountNumber,
                           String contactName, String address, String phoneNumber,
                           double creditLimit, String discountTypeStr, String login,
                           String password, String accountStateStr) {
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
        this.role = Role.MERCHANT;
    }

    /**
     * Instantiates a new Merchant account.
     *
     * @param accountHolderName the account holder name
     * @param accountNumber     the account number
     * @param contactName       the contact name
     * @param address           the address
     * @param phoneNumber       the phone number
     * @param creditLimit       the credit limit
     * @param discountType      the discount type
     * @param login             the login
     * @param password          the password
     */
    public MerchantAccount(String accountHolderName, String accountNumber,
                           String contactName, String address, String phoneNumber,
                           double creditLimit, DiscountType discountType,
                           String login, String password) {
        this.merchantID = 0;
        this.accountHolderName = accountHolderName;
        this.accountNumber = accountNumber;
        this.contactName = contactName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.creditLimit = creditLimit;
        this.discountType = discountType;
        this.login = login;
        this.password = password;
        this.accountState = AccountState.NORMAL;
    }

    /**
     * The enum Account state.
     */
    public enum AccountState {
        /**
         * Normal account state.
         */
        NORMAL("normal"),
        /**
         * Suspended account state.
         */
        SUSPENDED("suspended"),
        /**
         * In default account state.
         */
        IN_DEFAULT("in_default");

        private final String dbValue;
        AccountState(String dbValue) { this.dbValue = dbValue; }

        /**
         * Gets db value.
         *
         * @return the db value
         */
        public String getDbValue() { return dbValue; }

        /**
         * From db value account state.
         *
         * @param value the value
         * @return the account state
         */
        public static AccountState fromDbValue(String value) {
            if (value == null) return NORMAL;
            for (AccountState s : values()) {
                if (s.dbValue.equalsIgnoreCase(value.trim())) return s;
            }
            return NORMAL;
        }
    }

    /**
     * The enum Discount type.
     */
    public enum DiscountType {
        /**
         * Fixed discount type.
         */
        FIXED("Fixed"),
        /**
         * Variable discount type.
         */
        VARIABLE("Variable");

        private final String dbValue;
        DiscountType(String dbValue) { this.dbValue = dbValue; }

        /**
         * Gets db value.
         *
         * @return the db value
         */
        public String getDbValue() { return dbValue; }

        /**
         * From db value discount type.
         *
         * @param value the value
         * @return the discount type
         */
        public static DiscountType fromDbValue(String value) {
            if (value != null && value.equalsIgnoreCase("Variable")) return VARIABLE;
            return FIXED;
        }
    }

    /**
     * Can place orders boolean.
     *
     * @return the boolean
     */
    public boolean canPlaceOrders() {
        return accountState == AccountState.NORMAL;
    }

    /**
     * Calculate discount double.
     *
     * @param orderSubtotal the order subtotal
     * @return the double
     */
    public double calculateDiscount(double orderSubtotal) {
        for (DiscountTier tier : discountTiers) {
            if (tier.appliesTo(orderSubtotal)) {
                return tier.calculateDiscount(orderSubtotal);
            }
        }
        return 0.0;
    }

    /**
     * Would exceed credit limit boolean.
     *
     * @param currentOutstandingBalance the current outstanding balance
     * @param newOrderAmount            the new order amount
     * @return the boolean
     */
    public boolean wouldExceedCreditLimit(double currentOutstandingBalance, double newOrderAmount) {
        return (currentOutstandingBalance + newOrderAmount) > creditLimit;
    }

    /**
     * Gets merchant id.
     *
     * @return the merchant id
     */
    public int getMerchantID() { return merchantID; }

    /**
     * Sets merchant id.
     *
     * @param id the id
     */
    public void setMerchantID(int id) { this.merchantID = id; }

    /**
     * Gets account holder name.
     *
     * @return the account holder name
     */
    public String getAccountHolderName() { return accountHolderName; }

    /**
     * Sets account holder name.
     *
     * @param name the name
     */
    public void setAccountHolderName(String name) { this.accountHolderName = name; }

    /**
     * Gets account number.
     *
     * @return the account number
     */
    public String getAccountNumber() { return accountNumber; }

    /**
     * Sets account number.
     *
     * @param n the account number
     */
    public void setAccountNumber(String n) { this.accountNumber = n; }

    /**
     * Gets contact name.
     *
     * @return the contact name
     */
    public String getContactName() { return contactName; }

    /**
     * Sets contact name.
     *
     * @param n the name
     */
    public void setContactName(String n) { this.contactName = n; }

    /**
     * Gets address.
     *
     * @return the address
     */
    public String getAddress() { return address; }

    /**
     * Sets address.
     *
     * @param a the address
     */
    public void setAddress(String a)   { this.address = a; }

    /**
     * Gets phone number.
     *
     * @return the phone number
     */
    public String getPhoneNumber() { return phoneNumber; }

    /**
     * Sets phone number.
     *
     * @param p the phone number
     */
    public void setPhoneNumber(String p) { this.phoneNumber = p; }

    /**
     * Gets credit limit.
     *
     * @return the credit limit
     */
    public double getCreditLimit() { return creditLimit; }

    /**
     * Sets credit limit.
     *
     * @param l the credit limit
     */
    public void setCreditLimit(double l) { this.creditLimit = l; }

    /**
     * Gets discount type.
     *
     * @return the discount type
     */
    public DiscountType getDiscountType() { return discountType; }

    /**
     * Sets discount type.
     *
     * @param t the discount type
     */
    public void setDiscountType(DiscountType t) { this.discountType = t; }

    /**
     * Gets login.
     *
     * @return the login
     */
    public String getLogin() { return login; }

    /**
     * Sets login.
     *
     * @param login the login
     */
    public void setLogin(String login) { this.login = login; }

    /**
     * Gets password.
     *
     * @return the password
     */
    public String getPassword() { return password; }

    /**
     * Sets password.
     *
     * @param pw the password
     */
    public void setPassword(String pw) { this.password = pw; }

    /**
     * Gets account state.
     *
     * @return the account state
     */
    public AccountState getAccountState() { return accountState; }

    /**
     * Sets account state.
     *
     * @param s the account state
     */
    public void setAccountState(AccountState s) { this.accountState = s; }

    /**
     * Gets discount tiers.
     *
     * @return the discount tiers
     */
    public List<DiscountTier> getDiscountTiers() { return discountTiers; }

    /**
     * Sets discount tiers.
     *
     * @param tiers the tiers
     */
    public void setDiscountTiers(List<DiscountTier> tiers) { this.discountTiers = tiers; }

    /**
     * Add discount tier.
     *
     * @param tier the tier
     */
    public void addDiscountTier(DiscountTier tier) { discountTiers.add(tier); }

    /**
     * Can record payments boolean.
     *
     * @return the boolean
     */
    public boolean canRecordPayments()          { return role.canRecordPayments(); }

    /**
     * Can generate reports boolean.
     *
     * @return the boolean
     */
    public boolean canGenerateReports()         { return role.canGenerateReports(); }

    /**
     * toString
     * @return
     */
    @Override
    public String toString() {
        return String.format("MerchantAccount{id=%d, name='%s', account='%s', state=%s}",
                merchantID, accountHolderName, accountNumber, accountState);
    }
}
