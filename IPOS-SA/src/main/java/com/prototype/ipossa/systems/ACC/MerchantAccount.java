package com.prototype.ipossa.systems.ACC;

import java.util.ArrayList;
import java.util.List;

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
    public enum AccountState {
        NORMAL("normal"),
        SUSPENDED("suspended"),
        IN_DEFAULT("in_default");

        private final String dbValue;
        AccountState(String dbValue) { this.dbValue = dbValue; }
        public String getDbValue() { return dbValue; }

        public static AccountState fromDbValue(String value) {
            if (value == null) return NORMAL;
            for (AccountState s : values()) {
                if (s.dbValue.equalsIgnoreCase(value.trim())) return s;
            }
            return NORMAL;
        }
    }

    public enum DiscountType {
        FIXED("Fixed"),
        VARIABLE("Variable");

        private final String dbValue;
        DiscountType(String dbValue) { this.dbValue = dbValue; }
        public String getDbValue() { return dbValue; }

        public static DiscountType fromDbValue(String value) {
            if (value != null && value.equalsIgnoreCase("Variable")) return VARIABLE;
            return FIXED;
        }
    }

    public boolean canPlaceOrders() {
        return accountState == AccountState.NORMAL;
    }

    public double calculateDiscount(double orderSubtotal) {
        for (DiscountTier tier : discountTiers) {
            if (tier.appliesTo(orderSubtotal)) {
                return tier.calculateDiscount(orderSubtotal);
            }
        }
        return 0.0;
    }

    public boolean wouldExceedCreditLimit(double currentOutstandingBalance, double newOrderAmount) {
        return (currentOutstandingBalance + newOrderAmount) > creditLimit;
    }

    public int getMerchantID() { return merchantID; }
    public void setMerchantID(int id) { this.merchantID = id; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String name) { this.accountHolderName = name; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String n) { this.accountNumber = n; }

    public String getContactName() { return contactName; }
    public void setContactName(String n) { this.contactName = n; }

    public String getAddress() { return address; }
    public void setAddress(String a)   { this.address = a; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String p) { this.phoneNumber = p; }

    public double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(double l) { this.creditLimit = l; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType t) { this.discountType = t; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String pw) { this.password = pw; }

    public AccountState getAccountState() { return accountState; }
    public void setAccountState(AccountState s) { this.accountState = s; }

    public List<DiscountTier> getDiscountTiers() { return discountTiers; }
    public void setDiscountTiers(List<DiscountTier> tiers) { this.discountTiers = tiers; }
    public void addDiscountTier(DiscountTier tier) { discountTiers.add(tier); }

    public boolean canRecordPayments()          { return role.canRecordPayments(); }
    public boolean canGenerateReports()         { return role.canGenerateReports(); }

    @Override
    public String toString() {
        return String.format("MerchantAccount{id=%d, name='%s', account='%s', state=%s}",
                merchantID, accountHolderName, accountNumber, accountState);
    }
}
